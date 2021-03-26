module Main where

import Prelude

type Bar = (x :: Number, y :: Number, z :: Number)
type Baz = { w :: Number | Bar }
type Quux r = (q :: Number | r)

wildcard :: { w :: Number | _ } -> Baz
wildcard { w: w } = { x: w, y: w, z: w, w: w }

wildcard' :: { | Quux _ } -> Number
wildcard' { q: q } = q
