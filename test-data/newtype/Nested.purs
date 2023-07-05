module Nested where

newtype And :: forall k. Type -> k -> Type
newtype And a b = And a

newtype Nesting hooks = Nesting
  ( And hooks
      ( And hooks
          ( And hooks
              ( And hooks
                  ( And hooks
                      (And hooks (And hooks (And { name :: hooks} { name :: hooks})))
                  )
              )
          )
      )
  )