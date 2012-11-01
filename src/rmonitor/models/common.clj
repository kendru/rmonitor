(ns rmonitor.models.common
 (:refer-clojure :exclude [sort find])
  (:require [noir.server :as server]
            [monger.core :as mg]
            [monger.collection :as mc]
            [rmonitor.notifier :as mailer])
  (:import [org.bson.types ObjectId]
           [com.mongodb DB WriteConcern])
  (:use [monger.query]))

(def mongo-uri (get (System/getenv) "MONGO_URL_RMONITOR"))

(mg/connect-via-uri! mongo-uri)

(defn business-for [review]
  (mc/find-one-as-map "biz" {:_id (:_business_id review)}))

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
    (do
      (println (str "Got review by " (if (review :author) (review :author) "Anonymous")))
      (mailer/send-notification-for review (business-for review))
      (mc/insert "review" review))))