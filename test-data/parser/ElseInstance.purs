module ElseInstance where

class Show a where
  show :: a -> String

instance showBoolean :: Show Boolean where
  show true = "true"
  show false = "false"
else instance showBooleanDiff :: Show Boolean where
   show true = "mytrue"
   show false = "myfalse"