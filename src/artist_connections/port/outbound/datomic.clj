(ns artist-connections.port.outbound.datomic)

(defprotocol DatomicOutboundPort
  (query [_ q args] "Run a query on Datomic"))