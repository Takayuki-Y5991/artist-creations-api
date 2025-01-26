(ns artist-connections.adapter.outbound.datomic
  (:require [artist-connections.adapter.outbound.datomic :as datomic-port]
            [datomic.client.api :as d]))

(defrecord DatomicAdapter [connection]
  datomic-port/DatomicOutboundPort
  (query [_ q args]
    (d/q q (d/db connection) args)))
