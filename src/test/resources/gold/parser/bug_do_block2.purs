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

module Quasar.Mount.Common where

import Prelude

import Data.Either (Either(..))
import Data.Maybe (Maybe(..))
import Data.Newtype (class Newtype)
import Data.String as Str
import Data.Tuple (Tuple)
import Data.URI as URI

type Host = Tuple URI.Host (Maybe URI.Port)

extractHost ∷ Maybe URI.Authority → Either String Host
extractHost (Just (URI.Authority _ hs)) =
  case hs of
    [h] → Right h
    [] → Left "No host specified"
    _ → Left "Multiple hosts specified"
extractHost _ = Left "No host specified"

newtype Credentials = Credentials { user ∷ String, password ∷ String }

derive instance newtypeCredentials ∷ Newtype Credentials _
derive instance eqCredentials ∷ Eq Credentials
derive instance ordCredentials ∷ Ord Credentials

instance showCredentials ∷ Show Credentials where
  show (Credentials { user, password }) =
    "(Credentials { user: " <> show user <> ", password: " <> show password <> " })"

combineCredentials ∷ Credentials → String
combineCredentials (Credentials { user, password })
  | Str.null password = user
  | otherwise = user <> ":" <> password

extractCredentials ∷ Maybe URI.Authority → Maybe Credentials
extractCredentials auth = do
  userInfo ← (\(URI.Authority mui _) → mui) =<< auth
  pure $ Credentials $
    case Str.indexOf (Str.Pattern ":") userInfo of
      Nothing →
        { user: userInfo
        , password: ""
        }
      Just ix →
        { user: Str.take ix userInfo
        , password: Str.drop (ix + 1) userInfo
        }
