module Main where

type RandomConsoleEffects eff = ( random :: (Array User), console :: CONSOLE | eff )
type RandomConsoleEffects a eff = ( random :: (Array a), console :: CONSOLE | eff )
type RandomConsoleEffect a = a ()
type RandomConsoleEffect a = (RandomConsoleEffects User) ()