module Main where

type Foo a b c = forall b c a. (a -> (Array User)) -> (b -> c)

