(ns rmonitor.models.business
  (:use [monger.core :only [connect! connect set-db! get-db]]
        [monger.collection :only [insert insert-batch]])
  (:import [org.bson.types ObjectId]
           [com.mongodb DB WriteConcern]))

(def mongo-uri (get (System/getenv) "MONGO_URL_RMONITOR"))
