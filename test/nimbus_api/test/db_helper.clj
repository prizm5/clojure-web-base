(ns nimbus-api.test.db-helper
  (:require [nimbus-api.handler :as h]
            [nimbus-api.read.users :as users]
            [nimbus-api.read-db :as read]
            [nimbus-api.event-store :refer [publish-event]]
            [nimbus-api.configs :as config]

            [midje.sweet :refer :all]
            [peridot.core :refer :all]
            [clojure.tools.trace :refer [trace]]
            [clojure.data.json :as json]
            ))
(def admin-secret (atom nil))
(def admin-token (atom nil))

(defn reset-databases-without-seed []
  (let [event config/event-test-db-name 
        read config/read-test-db-name]
    (read/setup-dbs event read)))

(defn setup-dbs []
  (let [event config/event-test-db-name 
        read config/read-test-db-name]
    (trace "resetting dbs")
    (read/setup-dbs event read)
    (h/init event read false false)

    (publish-event (users/user-invited "admin" "admin" "admin"))
    (publish-event (users/user-registered "admin" "admin"))
    (let [s (:secret (users/get-user-by-email "admin" @read/db-read))]
      (swap! admin-secret (fn [x] s)))))

(defn- get-token [state id]
  (-> state
      (request "/authenticate"
               :request-method :post
               :content-type "application/xml"
               :headers {:content-type "application/xml"
                         :accept "application/json"}
               :body (json/write-str {:username id :password @admin-secret}))
      (-> :response :headers (get "X-Auth-Token"))))

(defn login-as [state id]
  (swap! admin-token (fn [_] (get-token (session h/app) id)))
  state)
