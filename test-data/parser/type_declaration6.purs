module Main where

type RandomConsoleEffects eff = ( random :: RANDOM, console :: CONSOLE | eff )


