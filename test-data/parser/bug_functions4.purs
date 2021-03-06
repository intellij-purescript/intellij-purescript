module Main where

fromURI ∷ URI.AbsoluteURI → Either String Config
fromURI (URI.AbsoluteURI scheme (URI.HierarchicalPart auth _) query) = do
  unless (scheme == Just sparkURIScheme) $ Left "Expected `spark` URL scheme"
  sparkHost ← extractHost auth
  let props = maybe SM.empty (\(URI.Query qs) → SM.fromFoldable qs) query

  Tuple hdfsHost props' ← case SM.pop "hdfsUrl" props of
    Just (Tuple (Just value) rest) → do
      value' ← extractHost' hdfsURIScheme $ decodeURIComponent value
      pure (Tuple value' rest)
    _ → Left "Expected `hdfsUrl` query parameter"

  Tuple path props'' ← case SM.pop "rootPath" props' of
    Just (Tuple (Just value) rest) → do
      value' ← lmap show $ runParser parseURIPathAbs value
      dirPath ← case value' of
        Left dp → pure dp
        Right _ → Left "Expected `rootPath` to be a directory path"
      pure (Tuple dirPath rest)
    _ → Left "Expected `rootPath` query parameter"

  pure { sparkHost, hdfsHost, path, props: props'' }

mkURI ∷ URI.URIScheme → Host → Maybe URI.Query → URI.AbsoluteURI
mkURI scheme host params =
  URI.AbsoluteURI
    (Just scheme)
    (URI.HierarchicalPart (Just (URI.Authority Nothing (pure host))) Nothing)
    params

extractHost' ∷ URI.URIScheme → String → Either String Host
extractHost' scheme@(URI.URIScheme name) uri = do
  URI.AbsoluteURI scheme' (URI.HierarchicalPart auth _) _ ←
    lmap show $ URI.runParseAbsoluteURI uri
  unless (scheme' == Just scheme) $ Left $ "Expected '" <> name <> "' URL scheme"
  extractHost auth

sparkURIScheme ∷ URI.URIScheme
sparkURIScheme = URI.URIScheme "spark"

hdfsURIScheme ∷ URI.URIScheme
hdfsURIScheme = URI.URIScheme "hdfs"