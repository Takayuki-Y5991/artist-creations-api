(ns artist-connections.core
  (:require [integrant.core :as ig]
            [artist-connections.system :as system]))

(defn -main []
  (let [system (ig/init system/system-config)]
    (.addShutdownHook (Runtime/getRuntime)
                      (Thread. #(ig/halt! system)))))