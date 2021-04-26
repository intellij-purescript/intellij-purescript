module NewTypeWithRowTypeType where

newtype Empty (r :: Row Type -> Type) = Empty r
