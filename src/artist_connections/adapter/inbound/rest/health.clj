(ns artist-connections.adapter.inbound.rest.health
  (:require [ring.util.response :as response]
            [artist-connections.port.inbound.health :as health-port]))

(defn health-check-handler [health-service]
  (fn [_]
    (let [result (health-port/check-health health-service)]
      (response/response result))))
