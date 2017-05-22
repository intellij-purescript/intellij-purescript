module A where

data User1
data User2 a
data User3 = NoOp2
data User4 a = NoOp3 a
data User5 a b c = A a
       | B a
       | Email HttpError b
       | Boo c
data User6 = User6 { name :: String }
data User7 a = User7 { name :: String, isEnabled :: Boolean }