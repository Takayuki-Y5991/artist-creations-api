(ns artist-connections.port.outbound.grpc.config
    (:require [artist-connections.rop.core :refer [ok error railway->]]
              [integrant.core :as ig]))

(defprotocol GrpcConfig
  (get-channel [this])
  (get-interceptors [this])
  (get-stub-config [this]))

(defn validate-grpc-config [config]
  (cond
    (nil? (:host config))
    (error "Missing GPRC host configuration")

    (nil? (:port config))
    (error "Missing GPRC port configuration")
    
    :else
    (ok config)))

(defn create-managed-channel [config]
  (railway->
   (validate-grpc-config config)
   (fn [{:keys [host port]}]
     (try
       (ok {:channel {str host ":" port}
            :interceptors []})
       (catch Exception e
         (error (str "Failed to create gRPC channel: " (.getMessage e))))))))


(defrecord GrpcClientConfig [channel interceptors]
  GrpcConfig
  (get-channel [_] channel)
  (get-interceptors [_] interceptors)
  (get-stub-config [_]
                   {:channel channel
                    :interceptors interceptors}))

(defmethod ig/init-key :artist-connections.port.outbound.grpc/config [_ config]
  (railway->
    (create-managed-channel config)
    (fn [{:keys [channel interceptors]}]
      (ok (->GrpcClientConfig channel interceptors)))))

(defmethod ig/init-key! :artist-connections.adapter.outbound/config[_ {:keys [channel]}]
  ())