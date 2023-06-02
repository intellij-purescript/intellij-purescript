module VisibleTypeApplications where

def :: forall @a. a -> a
def x = x

app = def @Int 1