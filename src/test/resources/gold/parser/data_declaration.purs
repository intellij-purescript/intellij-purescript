module A where

data User1
data User2 a
data User3 = NoOp2
data User4 a = NoOp3 a
data User5 a b c = A a
       | B a
       | Email HttpError b
       | Boo c