(ns rmonitor.views.businesses
  (:require [rmonitor.views.common :as common]
            [noir.content.getting-started]
            [noir.validation :as vali]
            [rmonitor.models.common :as m]
            [rmonitor.apis.google-places :as goog]
            [rmonitor.apis.citygrid :as cg])
  (:use [noir.core]
        [hiccup.page]
        [hiccup.form]))

(defn valid? [{:keys [name address city state zip]}]
  (vali/rule (vali/has-value? name)
    [:name "Business name must be present"])
  (vali/rule (vali/has-value? address)
    [:address "Business address must be present"])
  (vali/rule (vali/has-value? city)
    [:city "Business city must be present"])
  (vali/rule (vali/has-value? state)
    [:state "Business state must be present"])
  (vali/rule (vali/has-value? zip)
    [:zip "Business zip must be present"]))

(defpartial business-fields [{:keys [name address city state zip]}]
  (label "name" "Business Name: ")
  (text-field "name" name)
  (label "address" "Address: ")
  (text-field "address" address)
  (label "city" "City: ")
  (text-field "city" city)
  (label "state" "State: ")
  (text-field "state" state)
  (label "zip" "Zip Code: ")
  (text-field "zip" zip))

(defpartial review-info [{:keys [_id title body link author author-link source rating] :as review}]
  [:article {:class "article-block" :id (str "article-" _id)}
    (if title [:h2 title])
    (if body [:p body])
    [:p "By " author]
    [:h3 "Rating: " rating " out of 10"]])

(defpartial business-info [{:keys [_id name address city state zip] :as business}]
  [:article {:class "business-block" :id (str "business-" _id)}
    [:h2 name]
    [:p address [:br] city ", " state " " zip]
    (for [review (m/index-reviews business)]
      (review-info review))])

(defpage "/businesses" []
 (common/layout
 	[:section
    (for [biz (m/index-businesses 1 50)]
      (business-info biz))]))

(defpage "/businesses/add" {:as business}
 (common/layout
  (form-to [:post "/businesses/add"]
    (business-fields business)
    (submit-button "Add business"))))

(defpage [:post "/businesses/add"] {:as business}
  (if (valid? business)
    (do 
      (m/save-business business)
      (common/layout
        [:p "Business added!"]))
    (render "/businesses/add")))