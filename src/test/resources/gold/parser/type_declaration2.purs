module Main where

type Woo a = forall a. {name :: a}
