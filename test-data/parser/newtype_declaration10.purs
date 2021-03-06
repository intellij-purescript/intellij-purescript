module A where

newtype Ta a = Ta (Eff (console :: CONSOLE) a)