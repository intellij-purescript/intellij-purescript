module WithQualifiedOperator where

import Prelude
import Data.List as L

x = case 1 L.: L.Nil of
    a L.: rest -> a
    _ -> 0