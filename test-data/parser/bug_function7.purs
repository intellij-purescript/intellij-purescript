module Main where

addSuggestionImport :: forall r eff. Int -> Ref State -> { editor :: TextEditor, suggestion :: C.AtomSuggestion | r } -> Aff (AddModuleEff eff) Unit
addSuggestionImport port modulesState { editor, suggestion: { addImport: Just { mod, identifier, qualifier: Nothing } } } =
  addIdentImport' port modulesState (Just mod) identifier editor
addSuggestionImport _ _ _ = pure unit