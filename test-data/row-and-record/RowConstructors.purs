module Main where

import Prelude

type Foo = (x :: Number | (y :: Number | (z :: Number)))
type Bar = (x :: Number, y :: Number, z :: Number)
type Baz = { w :: Number | Bar }

foo :: { | Foo }
foo = { x: 0.0, y: 0.0, z: 0.0 }

bar :: { | Bar }
bar = { x: 0.0, y: 0.0, z: 0.0 }

id' :: Record Foo -> Record Bar
id' = id

foo' :: { | Foo }
foo' = id' foo

bar' :: { | Bar }
bar' = id' bar

baz :: Baz
baz = { x: 0.0, y: 0.0, z: 0.0, w: 0.0 }

type Quux r = (q :: Number | r)
type Norf r = (q' :: Number | Quux r)

quux :: { f :: { | Foo } | Quux Bar }
quux = { f: foo', x: 0.0, y: 0.0, z: 0.0, q: 0.0 }

quux' :: { | Norf Bar }
quux' = { x: 0.0, y: 0.0, z: 0.0, q: 0.0, q': 0.0 }
