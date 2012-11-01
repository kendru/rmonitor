(ns rmonitor.worker
	(:require [rmonitor.apis.google-places :as goog]
			  [rmonitor.apis.citygrid :as cg]
			  [rmonitor.models.common :as m]))

; Support 50 businesses
(def all-businesses (m/index-businesses 1 50))

(defn review-getter [get-fn]
  (for [biz all-businesses]
    (let [reviews (map #(assoc % :_business_id (biz :_id)) (get-fn biz))]
      (doseq [review reviews]
        (m/save-review review)))))

(defn google-reviews []
  (review-getter goog/get-reviews))

(defn citygrid-reviews []
  (review-getter cg/get-reviews))

(defn -main [& m]
  (do
    (google-reviews)
    (citygrid-reviews)))
