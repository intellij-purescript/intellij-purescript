module PureSugar where

pure_sugar :: forall a b. (a -> b) -> a -> Box b
pure_sugar f a = ado
  in f a