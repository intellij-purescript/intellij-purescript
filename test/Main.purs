module Test.Main where

import Prelude

import Data.Array
import Data.Maybe
import Debug (traceM)
import Effect (Effect)
import Effect.Class.Console (log)
import PureScript.CST.Lexer
import PureScript.CST.TokenStream
import PureScript.CST.Types

import Node.Encoding (Encoding(..))
import Node.FS.Sync (readTextFile)

getTokens tokens = case step tokens of
    TokenCons r _ next _ ->  r.value : getTokens next
    _ -> []


src =
    """main = do
      let t1 = test1
      let t2 = test2
      log "Done"
    """

typeSrc = """module Main where
test1 :: forall . String
test1 = ?
"""

main :: Effect Unit
main = simpleMain

simpleMain :: Effect Unit
simpleMain = do
    let tokens = getTokens $ lex typeSrc
    traceM tokens


realMain :: Effect Unit
realMain = do
  src <- readTextFile UTF8 "test-data/row-and-record/RowPolyInstanceContext.purs"
  let tokens = getTokens $ lex src
  let normalTokens = filter notLayout tokens
  let noNames = filter notNames normalTokens
  traceM noNames
  log (show $ length $ noNames)

notLayout (TokLayoutStart _) = false
notLayout (TokLayoutSep _) = false
notLayout (TokLayoutEnd _) = false
notLayout _ = true

notNames (TokLowerName _ "do" ) = true
notNames (TokLowerName _ "data" ) = true
notNames (TokLowerName _ "instance" ) = true
notNames (TokLowerName _ "class" ) = true
notNames (TokLowerName _ "import" ) = true
notNames (TokLowerName _ "where" ) = true
notNames (TokLowerName _ "forall" ) = true
notNames (TokLowerName _ _) = false
notNames _ = true