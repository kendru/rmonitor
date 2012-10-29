(ns rmonitor.apis.citygrid
  (:require [http.async.client :as http]
            [clojure.data.json :as json]))

(def endpoint-search "https://maps.googleapis.com/maps/api/place/textsearch/json")
(def endpoint-details "https://maps.googleapis.com/maps/api/place/details/json")
(def api-key "AIzaSyCDSQFBhmB2qkNJnda4kSdQSTrFqavYDpA")

(defn query-search [& {:keys [api-key query]}]
  (with-open [client (http/create-client)]
    (let [params {:query query :key api-key :sensor "false"}
          response (http/GET client endpoint-search :query params)]
      (-> response
          http/await
          http/string))))

(defn query-details [& {:keys [api-key reference]}]
  (with-open [client (http/create-client)]
    (let [params {:reference reference :key api-key :sensor "false"}
          response (http/GET client endpoint-details :query params)]
      (-> response
          http/await
          http/string))))

(defn search-for [business]
  (let [search-str (apply str
                     (interpose ","
                       (map business [:name :city :state :zip])))]
    (json/read-json (query-search :api-key api-key :query search-str))))

(defn details-for [business]
  (let [reference (place-ref-for business)]
    (json/read-json (query-details :api-key api-key :reference reference))))

(defn place-ref-for [business]
  (let [place-ref (business :place-ref)]
    (if place-ref
      place-ref
      (get-in (search-for business) [:results 0 :reference]))))

(defn get-reviews [business]
  (get-in (details-for business) [:result :reviews]))
