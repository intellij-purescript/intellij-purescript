module Main where

import Data.Function as DF

infixr 0 DF.apply as />

main = log /> "Done" ?! true
