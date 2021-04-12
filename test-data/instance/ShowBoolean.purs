module ShowBoolean where

instance showBoolean :: Show Boolean where
  show :: Bolean -> String
  show true = "true"
  show false = "false"