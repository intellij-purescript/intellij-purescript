module RecordRowNamedType where

data Unit = Unit

type TypeRecord = { type :: String }

f :: TypeRecord -> String     
f r = ( case Unit of Unit -> r.type )
