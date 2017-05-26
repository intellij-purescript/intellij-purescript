module Main where

f :: List { x :: Int, y :: Int } -> Int
f ( r@{ x } : _) = x + r.y
f _ = 0
