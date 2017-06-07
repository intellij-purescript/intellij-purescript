{-
Copyright 2017 SlamData, Inc.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
-}

module Quasar.QuasarF.Interpreter.Affjax
  ( M
  , eval
  , module Quasar.QuasarF.Interpreter.Config
  )
  where

import Prelude

import Control.Monad.Eff.Exception (Error, error)
import Control.Monad.Free (Free)

import Data.Argonaut (Json, JObject, jsonEmptyObject, (:=), (~>))
import Data.Array (catMaybes)
import Data.Bifunctor (lmap)
import Data.Either (Either(..), either)
import Data.Foldable (class Foldable, foldl)
import Data.Functor.Coproduct (Coproduct)
import Data.HTTP.Method (Method(..))
import Data.List (List(..), (:))
import Data.Maybe (Maybe(..), maybe)
import Data.MediaType.Common (applicationJSON)
import Data.Path.Pathy (printPath, runFileName, runDirName, rootDir, peel)
import Data.String as Str
import Data.StrMap as SM
import Data.Tuple (Tuple(..), fst, snd)

import Network.HTTP.Affjax.Request (RequestContent, toRequest)
import Network.HTTP.AffjaxF as AXF
import Network.HTTP.RequestHeader as Req

import Quasar.ConfigF as CF
import Quasar.Data.JSONMode as JSONMode
import Quasar.FS.DirMetadata as DirMetadata
import Quasar.Mount as Mount
import Quasar.Paths as Paths
import Quasar.QuasarF (QuasarF(..), DirPath)
import Quasar.QuasarF.Interpreter.Config (Config)
import Quasar.QuasarF.Interpreter.Internal (mkURL, delete, unitResult, mkRequest, defaultRequest, get, jsonResult, put, toPageParams, strResult, toVarParams, ask)
import Quasar.Query.OutputMeta as QueryOutputMeta
import Quasar.ServerInfo as ServerInfo
import Quasar.Types as QT

type M r = Free (Coproduct (CF.ConfigF (Config r)) (AXF.AffjaxFP RequestContent String))

eval ∷ ∀ r. QuasarF ~> M r
eval = case _ of

  ServerInfo k → do
    { basePath } ← ask
    let url = basePath <> Str.drop 1 (printPath Paths.serverInfo)
    k <$> mkRequest serverInfoResult (get url)

  FileMetadata path k → do
    url ← mkURL Paths.metadata (Right path) Nil
    k <$> mkRequest fileMetaResult (get url)

  DirMetadata path k → do
    url ← mkURL Paths.metadata (Left path) Nil
    k <$> mkRequest (resourcesResult path) (get url)

  ReadQuery mode path sql vars pagination k → do
    let params = Tuple "q" sql : toVarParams vars <> toPageParams pagination
    url ← mkURL Paths.query (Left path) params
    k <$> mkRequest jsonResult
      (AXF.affjax $ defaultRequest
        { url = url
        , headers = [Req.Accept $ JSONMode.decorateMode applicationJSON mode]
        })

  WriteQuery path file sql vars k → do
    let destHeader = Tuple "Destination" (printPath file)
    url ← mkURL Paths.query (Left path) (headerParams [destHeader] : toVarParams vars)
    k <$> mkRequest writeQueryResult
      (AXF.affjax $ defaultRequest
        { url = url
        , method = Left POST
        , content = Just $ snd (toRequest sql)
        })

  CompileQuery path sql vars k → do
    url ← mkURL Paths.compile (Left path) (Tuple "q" sql : toVarParams vars)
    k <$> mkRequest (lmap error <$> QT.compileResultFromString <=< strResult) (get url)

  ReadFile mode path pagination k → do
    url ← mkURL Paths.data_ (Right path) (toPageParams pagination)
    k <$> mkRequest jsonResult
      (AXF.affjax defaultRequest
        { url = url
        , headers = [Req.Accept $ JSONMode.decorateMode applicationJSON mode]
        })

  WriteFile path content k → do
    url ← mkURL Paths.data_ (Right path) Nil
    let reqSettings = toRequest content
    k <$> mkRequest unitResult
      (AXF.affjax defaultRequest
        { url = url
        , headers = catMaybes [Req.ContentType <$> fst reqSettings]
        , method = Left PUT
        , content = Just $ snd reqSettings
        })

  AppendFile path content k → do
    url ← mkURL Paths.data_ (Right path) Nil
    let reqSettings = toRequest content
    k <$> mkRequest unitResult
      (AXF.affjax defaultRequest
        { url = url
        , headers = catMaybes [Req.ContentType <$> fst reqSettings]
        , method = Left POST
        , content = Just $ snd (toRequest content)
        })

  InvokeFile mode path vars pagination k → do
    url ← mkURL Paths.invoke (Right path) (SM.toUnfoldable vars <> toPageParams pagination)
    k <$> mkRequest jsonResult
      (AXF.affjax defaultRequest
        { url = url
        , headers = [Req.Accept $ JSONMode.decorateMode applicationJSON mode]
        })

  DeleteData path k → do
    k <$> (mkRequest unitResult <<< delete =<< mkURL Paths.data_ path Nil)

  MoveData fromPath toPath k → do
    let destHeader = Tuple "Destination" (either printPath printPath toPath)
    url ← mkURL Paths.data_ fromPath (headerParams [destHeader] : Nil)
    k <$> mkRequest unitResult
      (AXF.affjax defaultRequest
        { url = url
        , method = Left MOVE
        })

  CreateMount path config k → do
    let pathParts = either peel peel path
        parentDir = maybe rootDir fst pathParts
        name = maybe "" (either runDirName runFileName <<< snd) pathParts
        filenameHeader = Tuple "X-File-Name" name
    url ← mkURL Paths.mount (Left parentDir) (headerParams [filenameHeader] : Nil)
    k <$> mkRequest unitResult
      (AXF.affjax defaultRequest
        { url = url
        , method = Left POST
        , content = Just $ snd (toRequest (Mount.toJSON config))
        })

  UpdateMount path config k → do
    url ← mkURL Paths.mount path Nil
    k <$> (mkRequest unitResult $ put url $ snd (toRequest (Mount.toJSON config)))

  GetMount path k →
    k <$> (mkRequest mountConfigResult <<< get =<< mkURL Paths.mount path Nil)

  MoveMount fromPath toPath k → do
    let destHeader = Tuple "Destination" (either printPath printPath toPath)
    url ← mkURL Paths.mount fromPath (headerParams [destHeader] : Nil)
    k <$> mkRequest unitResult
      (AXF.affjax defaultRequest
        { url = url
        , method = Left MOVE
        })

  DeleteMount path k →
    k <$> (mkRequest unitResult <<< delete =<< mkURL Paths.mount path Nil)

serverInfoResult ∷ String -> Either Error ServerInfo.ServerInfo
serverInfoResult = lmap error <$> ServerInfo.fromJSON <=< jsonResult

writeQueryResult ∷ String → Either Error QueryOutputMeta.OutputMeta
writeQueryResult = lmap error <$> QueryOutputMeta.fromJSON <=< jsonResult

resourcesResult ∷ DirPath → String → Either Error DirMetadata.DirMetadata
resourcesResult path = lmap error <$> DirMetadata.fromJSON path <=< jsonResult

mountConfigResult ∷ String → Either Error Mount.MountConfig
mountConfigResult = lmap error <$> Mount.fromJSON <=< jsonResult

fileMetaResult ∷ String → Either Error Unit
fileMetaResult = map (\(_ ∷ JObject) → unit) <<< jsonResult

headerParams ∷ ∀ f. Foldable f ⇒ f (Tuple String String) → Tuple String String
headerParams = Tuple "request-headers" <<< show <<< foldl go jsonEmptyObject
  where
  go ∷ Json → Tuple String String → Json
  go j (Tuple k v) = k := v ~> j
