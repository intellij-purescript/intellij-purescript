module Main where

import Prelude
import Control.Monad.Eff
import Control.Monad.Eff.Console (log)
import Test.Assert

f 1 = 1
f _ = 0

main = do
  assert $ f 1 == 1
  assert $ f 0 == 0
  log "Done"
