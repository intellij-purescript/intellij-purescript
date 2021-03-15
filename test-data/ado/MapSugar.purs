module MapSugar where

map_sugar :: forall a b. (a -> b) -> Box a -> Box b
map_sugar f g = ado
  x <- g
  in f x