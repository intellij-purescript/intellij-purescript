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

module Quasar.Mount.Couchbase.Gen where

import Prelude

import Control.Monad.Gen (class MonadGen)
import Control.Monad.Gen as Gen
import Control.Monad.Rec.Class (class MonadRec)
import Data.Maybe (Maybe(..))
import Quasar.Mount.Common.Gen (genAlphaNumericString, genHost)
import Quasar.Mount.Couchbase as CB

genConfig ∷ ∀ m. MonadGen m ⇒ MonadRec m ⇒ m CB.Config
genConfig = do
  host ← genHost
  withCreds ← Gen.chooseBool
  case withCreds of
    true → do
      user ← Just <$> genAlphaNumericString
      password ← Just <$> genAlphaNumericString
      pure { host, user, password }
    false →
      pure { host, user: Nothing, password: Nothing }
