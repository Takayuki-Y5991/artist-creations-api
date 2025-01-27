(ns artist-connections.port.outbound.grpc.oauth2
  (:require [artist-connections.rop.core :refer [ok error railway->]]
            [clojure.string :as str]))


(defprotocol OAuth2Endpoints
  (generate-authorize-url [this params])
  (exchage-token [this params])
  (refresh-token [this params]))

(defrecord OAuthGrpcEndpoints [config]
  OAuth2Endpoints
  (generate-authorize-url [_ {:keys [client-id scope state redirect-uri]}]
    (try
      (let [stub-config (config/get-stub-config config)
            scope-str (str/join " " scope)
            request {:client_id client-id :scope scope-str :state state :redirect_uri redirect-uri}
            response {:url (str "https://example.com/oauth/authorize?client_id=" client-id "&scope=" scope-str "&state=" state "&redirect_uri=" redirect-uri)}]
        (ok response))
      (catch Exception e
        (error (str "Failed to generate authorize url: " (.getMessage e))))))

  (exchange-token [_ {:keys [client-id client-secret code redirect-uri]}]
    (try
      (let [stub-config (config/get-stub-config config)
            request {:client-id client-id
                     :client-secret client-secret
                     :code code
                     :redirect-uri redirect-uri}
             ;; Replace with actual gRPC call
            response {:access-token "sample_token"
                      :refresh-token "refresh_token"
                      :expires-in 3600}]
        (ok response))
      (catch Exception e
        (error (str "Failed to exchange token: " (.getMessage e))))))

  (refresh-token [_ {:keys [client-id client-secret refresh-token]}]
    (try
      (let [stub-config (config/get-stub-config config)
            request {:client-id client-id
                     :client-secret client-secret
                     :refresh-token refresh-token}
             ;; Replace with actual gRPC call
            response {:access-token "new_token"
                      :refresh-token "new_refresh_token"
                      :expires-in 3600}]
        (ok response))
      (catch Exception e
        (error (str "Failed to refresh token: " (.getMessage e)))))))