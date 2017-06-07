module Main where

eval :: Query ~> H.ParentDSL State Query ChildQuery ChildSlot Void m
eval (ReadStates next) = do
  a <- H.query' CP.cp1 unit (H.request CA.GetState)
  b <- H.query' CP.cp2 unit (H.request CB.GetCount)
  c <- H.query' CP.cp3 unit (H.request CC.GetValue)
  H.put { a, b, c }
  pure next