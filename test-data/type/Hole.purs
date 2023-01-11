module Hole where

import Prelude

data X a = X a

instance Show a => Show (X a) where
  show :: ?t
  show = ""