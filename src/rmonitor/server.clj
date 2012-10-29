(ns rmonitor.server
  (:require [noir.server :as server]
            [taoensso.carmine :as car]))

(server/load-views-ns 'rmonitor.views)

(defn -main [& m]
  (let [mode (keyword (or (first m) :dev))
        port (Integer. (get (System/getenv) "PORT" "8080"))]
    (server/start port {:mode mode
                        :ns 'rmonitor})))

