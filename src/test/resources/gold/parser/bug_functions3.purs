module Main where

fromURI ∷ URI.AbsoluteURI → Either String Config
fromURI (URI.AbsoluteURI scheme (URI.HierarchicalPart auth path) query) = do
  unless (scheme == Just uriScheme) $ Left "Expected 'mongodb' URL scheme"
  hosts ← extractHosts auth
  auth' ← case extractCredentials auth, path of
    Just credentials, Just p → pure $ Just (Auth { path: p, credentials })
    Nothing, Nothing → pure $ Nothing
    Just _, Nothing → Left "User credentials were specified, but no auth database"
    Nothing, Just _ → Left "An auth database was specified, but no user credentials"
  let props = maybe SM.empty (\(URI.Query qs) → SM.fromFoldable qs) query
  pure { hosts, auth: auth', props }

uriScheme ∷ URI.URIScheme
uriScheme = URI.URIScheme "mongodb"

extractHosts ∷ Maybe URI.Authority → Either String (NonEmpty Array Host)
extractHosts = maybe err Right <<< (toNonEmpty <=< map getHosts)
  where
  getHosts (URI.Authority _ hs) = hs
  toNonEmpty hs = NonEmpty <$> Arr.head hs <*> Arr.tail hs
  err = Left "Host list must not be empty"