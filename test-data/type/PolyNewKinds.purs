module PolyNewKinds where

newtype ReaderT :: forall k. Type -> (k -> Type) -> k -> Type
