module Main where

fromURI ∷ URI.AbsoluteURI → Either String Config
fromURI (URI.AbsoluteURI scheme (URI.HierarchicalPart auth path) query) = do
  unless (scheme == Just uriScheme) $ Left "Expected 'xcc' URL scheme"
  host ← extractHost auth
  let
    credentials = extractCredentials auth
    props = maybe SM.empty (\(URI.Query qs) → SM.fromFoldable qs) query
  format ← case join $ SM.lookup "format" props of
    Nothing → pure XML
    Just "xml" → pure XML
    Just "json" → pure JSON
    Just f → Left $ "Unexpected format: " <> f
  pure { host, path, credentials, format}

uriScheme ∷ URI.URIScheme
uriScheme = URI.URIScheme "xcc"