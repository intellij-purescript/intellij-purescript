module Main where

type ALL = forall a m. Semigroup m => (a -> m) -> Array a -> m

type Woop = forall a m. ((Cooli) => String) -> (a -> m) -> Cooli => String