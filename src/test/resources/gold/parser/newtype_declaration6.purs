module A where

newtype User9 a b = User9 a {
  name :: a -> b
}