(ns rmonitor.notifier
  (:use [clj-aws.core :only [credentials]]
        [clj-aws.ses :only [client message destination send-email]]))

(def aws-creds (credentials 
              (get (System/getenv) "AWS_KEY")
              (get (System/getenv) "AWS_SECRET")))

(def email-client (client aws-creds))
(defn email-message [business review]
  (message (str "[rmonitor]New Review for " (:name business))
                       (str "New review from " (:author review) ":\n"
                          (:body review)
                          "Rating: " (:rating review) "(out of 10)")))
(defn email-destination [business] (destination (vec (:admin-emails business))))
(def email-sender "andymeredith@gmail.com")

(defn send-notification-for [review business]
    (send-email email-client email-sender (email-destination business) (email-message business review)))