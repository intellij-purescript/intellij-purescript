module AkwardNames where

foo (Directory components₀) (Directory components₁) =
  Directory (components₁ <> components₀)