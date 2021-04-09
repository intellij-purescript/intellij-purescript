module Main where

import Prelude

patternDoWithNamedBinder = unsafePartial do
  return true

data List a = Nil | Cons a (List a)

infixr 6 Cons as :

instance eqList :: Eq a => Eq (List a) where
  eq xs ys = true