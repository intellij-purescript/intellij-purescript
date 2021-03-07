module Foo where

import <fold text='...'>Prelude

import Control.Applicative (class Applicative, pure, liftA1, unless, when)
import Control.Apply (class Apply, apply, (*>), (<*), (<*>))
import Control.Bind (class Bind, bind, class Discard, discard, ifM, join, (<=<), (=<<), (>=>), (>>=))
import Control.Category (class Category, identity)
import Control.Monad (class Monad, ap, liftM1, unlessM, whenM)
import Control.Semigroupoid (class Semigroupoid, compose, (<<<), (>>>))

import Data.Boolean (otherwise)
import Data.BooleanAlgebra (class BooleanAlgebra)
import Data.Bounded (class Bounded, bottom, top)</fold>
