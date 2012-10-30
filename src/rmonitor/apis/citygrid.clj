(ns rmonitor.apis.citygrid
  (:require [http.async.client :as http]
            [cheshire.core :as json]
            [rmonitor.models.common :as model]))

(def endpoint-search "http://api.citygridmedia.com/content/places/v2/search/where")
(def endpoint-reviews "http://api.citygridmedia.com/content/reviews/v2/search/where")
(def publisher-code "10000004337")

(defn query-search [& {:keys [publisher-code name zip]}]
  (with-open [client (http/create-client)]
    (let [params {:publisher publisher-code
                  :what name
                  :where zip
                  :format "json"}
          response (http/GET client endpoint-search :query params)]
      (-> response
          http/await
          http/string))))

(defn query-details [& {:keys [publisher-code listing-id]}]
  (with-open [client (http/create-client)]
    (let [params {:listing_id listing-id
                  :publisher publisher-code
                  :rpp 50
                  :sort "createdate"
                  :format "json"}
          response (http/GET client endpoint-reviews :query params)]
      (-> response
          http/await
          http/string))))

(defn search-for [business]
  (json/parse-string (query-search :publisher-code publisher-code
                                   :name (business :name)
                                   :zip (business :zip))))

(defn listing-id-for [business]
  (let [listing-id (business :citygrid-listing-id)]
    (if listing-id
      listing-id
      (let [new-id (get-in (search-for business) ["results" "locations" 0 "id"])]
        (model/save-business (assoc business :citygrid-listing-id new-id))
        new-id))))

(defn details-for [business]
  (let [listing-id (listing-id-for business)]
    (json/parse-string (query-details :publisher-code publisher-code :listing-id listing-id))))

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
  {:body (get review "review_text")
   :title (get review "review_title")
   :link (get review "review_url")
   :author (get review "review_author")
   :author-link (get review "review_author_url")
   :rating (get review "review_rating")
   :date (get review "review_date")
   :digest (md5 (get review "review_id"))
   :source (get review "source")})

(defn get-reviews [business]
  (let [reviews (get-in (details-for business) ["results" "reviews"])]
    (map adapt-review reviews)))