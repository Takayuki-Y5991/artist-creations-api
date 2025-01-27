(ns artist-connections.adapter.outbound.grpc.oauth2
  (:require [artist-connections.rop.core :refer [ok error]]))

(defprotocol OAuth2URLGenerator
  (generate-url [this params]
    "Generate OAuth2 URL with given parameters
     params: {:scope [\"scope1\" \"scope2\"]
              :state string
              :redirect-uri string}
     returns: Railway result with URL string or error"))

(defprotocol OAuth2TokenExchanger
  (exchange-token [this code]
    "Exchange authorization code for tokens
     returns: Railway result with token response or error"))