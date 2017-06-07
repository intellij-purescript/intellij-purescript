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

module Test.Util.FS where

import Prelude

import Control.Monad.Aff (Aff, attempt, apathize)
import Control.Monad.Eff.Exception (error)
import Control.Monad.Error.Class (throwError)

import Data.Array as Arr
import Data.Either (Either(..))
import Data.Foldable (traverse_)
import Data.Maybe (Maybe(..))
import Data.String as Str

import Node.FS (FS)
import Node.FS.Stats as FSS
import Node.FS.Aff as FSA

mkdirRec ∷ ∀ eff. String → Aff (fs ∷ FS | eff) Unit
mkdirRec path = case Arr.uncons (Str.split (Str.Pattern "/") path) of
  Nothing → pure unit
  Just { head, tail } → do
    apathize $ FSA.mkdir head
    _ ← Arr.foldM mkSegment head tail
    success ← FSA.exists path
    unless success $ throwError $ error $ "Failed to create " <> path
  where
  mkSegment done next = do
    let acc = done <> "/" <> next
    apathize $ FSA.mkdir acc
    pure acc

rmRec ∷ ∀ eff. String → Aff (fs ∷ FS | eff) Unit
rmRec path = do
  stat ← attempt $ FSA.stat path
  case stat of
    Right s
      | FSS.isFile s → FSA.unlink path
      | FSS.isDirectory s → do
          traverse_ (\file → rmRec (path <> "/" <> file)) =<< FSA.readdir path
          FSA.rmdir path
    _ → pure unit
