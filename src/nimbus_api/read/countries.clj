(ns nimbus-api.read.countries
  (:require [nimbus-api.events :refer :all]
            [cemerick.url :as url]
            [clojure.tools.trace :as tr]
            [nimbus-api.helpers :as help]
            [clj-jwt.core  :refer :all]
            [clj-http.client :as client]
            [nimbus-api.cloudant :as cloudant]
            [clojure.data.json :as json]
            [com.ashafa.clutch :as clutch]))

(comment
  (get-country-docs db "Austria")
  (get-country-docs db "test test")
  (def db (url/url  "https://steveshogren:mycloudant@steveshogren.cloudant.com/" "koopa-read"))
  ) 

(defn get-country-docs 
  ([db] (get-country-docs db nil))
  ([db key]
     (let [key (if key (java.net.URLEncoder/encode key) nil)]
       (map :doc (cloudant/get-view "countries" db  key))))) 

;;{"name":"American Samoa","two-digit":"AS","three-digit":"ASM","code":"016"}
(defmethod persist :insert-country [country-event db]
  (let [country-event (:doc country-event)
        country (first (get-country-docs db (:name country-event)))
        country-event (merge country country-event)]
    (assoc country-event :type :country)))

(defn country-inserted [name two-digit three-digit code]
  (create-event :insert-country {:name name :two-digit two-digit :three-digit three-digit :code code } nil))

