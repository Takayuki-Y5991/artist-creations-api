(ns artist-connections.port.inbound.health)

(defprotocol HealthInboundPort
  (check-health [_] "Check the health of the system"))
