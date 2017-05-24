module Main where

type ALL = forall a m. Semigroup m => (a -> m) -> Array a -> m