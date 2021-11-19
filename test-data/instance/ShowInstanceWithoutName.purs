module ShowInstanceWithoutName where

data A = A

instance Show A where
    show _ = "A"
