module Formatting where

import Prelude

import Prelude ((<>)) as P
import Effect.Console (log)
import Effect
  ( Effect
  )

main :: Effect Unit
main = do
  log ("Hello world" P.<> "!")
  x <- pure (5 * 7)
  log $ show x