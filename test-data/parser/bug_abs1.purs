module Main where

localSearch ::forall eff. Int -> Ref State -> Eff (LocalEff (ref :: REF | eff)) Unit
localSearch port modulesState = selectListViewDynamic view (\(C.TypeInfo { identifier }) -> log identifier) Nothing (const "") search 50
  where
  search text = do
    state <- liftEff $ readRef modulesState
    modules <- getLoadedModules port
    let getQualifiedModule = (flip getQualModule) state
    getCompletion' (Just $ C.Flex text) [] port state.main Nothing modules getQualifiedModule

  view (C.TypeInfo {identifier, type', module'}) =
     "<li class='two-lines'>"
     <> "<div class='primary-line'>" <> identifier <> ": <span class='text-info'>" <> type' <> "</span></div>"
     <> "<div class='secondary-line'>" <> module' <> "</div>"
     <> "</li>"
