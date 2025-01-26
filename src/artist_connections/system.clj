(ns artist-connections.system
  (:require [integrant.core :as ig]
            [artist-connections.adapter.inbound.rest.health :as health-adapter]
            [artist-connections.adapter.outbound.datomic :as datomic-adapter]
            [ring.adapter.jetty :as jetty]))

(defmethod ig/init-key :core/health [_ _]
  (reify artist-connections.port.inbound.health/HealthInboundPort
    (check-health [_]
      {:status :ok :message "System is healthy."})))

(defmethod ig/init-key :outbound/datomic [_ config]
  (datomic-adapter/->DatomicAdapter config))

(defmethod ig/init-key :server/http [_ {:keys [port health-service]}]
  (jetty/run-jetty (health-adapter/app health-service) {:port port :join? false}))

(defmethod ig/halt-key! :server/http [_ server]
  (.stop server))

(def system-config
  {:core/health {}
   :outbound/datomic {:uri "datomic:dev://localhost:4334/my-db"}
   :server/http {:port 8080
                 :health-service #ig/ref :core/health}})