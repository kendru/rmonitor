(ns rmonitor.worker
	(:require [rmonitor.apis.google-places :as goog]
			  [rmonitor.apis.citygrid :as cg]
			  [rmonitor.models.common :as m]))

; Support 50 businesses
(def all-businesses (m/index-businesses 1 50))

(defn google-reviews []
  (for [biz all-businesses]
    (let [reviews (map #(assoc % :_business_id (biz :_id)) (goog/get-reviews biz))]
      (doseq [review reviews]
        (m/save-review review)))))

(defn citygrid-reviews []
  (for [biz all-businesses]
    (let [reviews (map #(assoc % :_business_id (biz :_id)) (cg/get-reviews biz))]
      (doseq [review reviews]
        (m/save-review review)))))

(defn -main [& m]
  (do
    (google-reviews)
    (citygrid-reviews)))
