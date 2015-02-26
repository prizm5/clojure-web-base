(ns nimbus-api.cloudant
  (:require [clojure.tools.trace :as tr]
            [nimbus-api.helpers :as help]
            [clj-http.client :as client]
            [clojure.data.json :as json]))

(defn get-view 
  ([view-name db] (get-view view-name db nil))
  ([view-name db key]
     (let [view-url (str db "/_design/" "views" "/_view/" view-name "?include_docs=true&reduce=false")
           view-url (if key (str view-url "&keys=[%22" key "%22]") view-url)]
       (get (json/read-str (:body (client/get view-url))
                           :key-fn keyword) :rows))))
