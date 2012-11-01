(ns rmonitor.apis.google-places
  (:require [http.async.client :as http]
            [cheshire.core :as json]
            [rmonitor.models.common :as model]))

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
    (json/parse-string (query-search :api-key api-key :query search-str))))

(defn place-ref-for [business]
  (let [place-ref (business :place-ref)]
    (if place-ref
      place-ref
      (let [new-ref (get-in (search-for business) ["results" 0 "reference"])]
        (do (model/save-business (assoc business :place-ref new-ref))
        new-ref)))))

(defn details-for [business]
  (let [reference (place-ref-for business)]
    (json/parse-string (query-details :api-key api-key :reference reference))))

(defn average-ratings [ratings-coll]
  (Math/round (* 3.3
    (/
      (reduce + (map #(get % "rating") ratings-coll))
      (count ratings-coll)))))

(defn md5
  "Generate a md5 checksum for the given string"
  [token]
  (let [hash-bytes
         (doto (java.security.MessageDigest/getInstance "MD5")
               (.reset)
               (.update (.getBytes token)))]
       (.toString
         (new java.math.BigInteger 1 (.digest hash-bytes)) ; Positive and the size of the number
         16))) ; Use base16 i.e. hex

(defn adapt-review [review]
  {:body (get review "text")
   :author (get review "author_name")
   :author-link (get review "author_url")
   :rating (average-ratings (get review "aspects"))
   :source "Google Places"
   :digest (md5 (get review "text"))})

(defn get-reviews [business]
  (let [reviews (get-in (details-for business) ["result" "reviews"])]
    (map adapt-review reviews)))
