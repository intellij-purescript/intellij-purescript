module Test.Main where

import Prelude

import Effect (Effect)
import Effect.Class.Console (log)
import PureScript.CST.Lexer
import PureScript.CST.TokenStream
import Debug (traceM)
import Data.Maybe
import Data.Array

firstToken tokens = case step tokens of
    TokenCons r _ next _ ->  r.value : firstToken next
    _ -> []


src =
    """main = do
      let t1 = test1
      let t2 = test2
      log "Done"
    """

typeSrc = """
module Main where
test1 :: forall r . S { foo :: String | r } Unit
test1 = state $ \o -> o { foo = o.foo <> "!" }
"""

main :: Effect Unit
main = do
  let tokens = lex typeSrc
  log typeSrc
  traceM $ firstToken tokens
