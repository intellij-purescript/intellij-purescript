module ImportData where

data TList :: forall (k :: Type). k -> Type
data TList a

foreign import data A :: forall (k :: Type). TList k