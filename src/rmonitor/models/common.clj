(ns rmonitor.models.common
 (:refer-clojure :exclude [sort find])
  (:require [noir.server :as server]
            [monger.core :as mg]
            [monger.collection :as mc])
  (:import [org.bson.types ObjectId]
           [com.mongodb DB WriteConcern])
  (:use monger.query))

(def mongo-uri (get (System/getenv) "MONGO_URL_RMONITOR"))

(mg/connect-via-uri! mongo-uri)

(defn index-businesses [page per-page]
  (with-collection "biz"
    (find {})
    (paginate :page page :per-page per-page)))

(defn index-reviews [business]
  (with-collection "review"
    (find {:_business_id (business :_id)})))

(defn save-business [business]
  (if-let [_id (business :_id)]
    (mc/update "biz" {:_id _id} business)
    (mc/insert "biz" business)))

(defn save-review [review]
  (if-let [stored-review 
            (mc/find-one-as-map "review" {:digest (review :digest)})]
    (mc/update "review" {:digest (review :digest)} review)
    ; TODO email everyone watching this biz
    (mc/insert "review" review)))