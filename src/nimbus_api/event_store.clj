(ns nimbus-api.event-store
  (:require [cemerick.url :as url]
            [clojure.core.async :as async]
            [nimbus-api.events :as e]
            [nimbus-api.read.users :as users]
            [nimbus-api.read.ref-data :as ref-data]
            [nimbus-api.read-db :as read]
            [clojure.tools.trace :refer [trace]]
            [clj-jwt.core  :refer :all]
            [clj-jwt.key   :refer [private-key]]
            [clj-time.core :refer [after? now plus days seconds]]
            [clj-time.coerce :refer [to-long from-long]]
            [clojure.tools.logging :as log]
            [com.ashafa.clutch :as clutch]))

(def event-db (atom nil))
(def db_watch (atom nil))
(def run-async? (atom true))

(defn send-to-read [doc]
  (let [doc (assoc doc :event (keyword (:event doc)))]
    (read/write-to-read doc)))

(defn publish-event [doc]
  (clutch/put-document @event-db doc)
  (when (not @run-async?)
    (send-to-read doc)))

(defn invite-user [email org]
  (publish-event (users/user-invited email org "keith")))

(defn register-user [id email]
  (publish-event (users/user-registered id email)))

;; (insert-ref-data "test" "tthis" "that")  
(defn insert-ref-data [name reftype value]
  (publish-event (ref-data/ref-data-added name reftype value)))

(def db-watch-fn
  (fn [key agent previous-change change]
    (if change
      (let [seq (:seq change) 
            doc (:doc change)]
        (if (= (:record-type doc) "event")
          (do (log/info "Sent " doc " to read")
              (send-to-read (assoc doc :event-seq seq))))))))

(def init-memo (atom nil))

(defn- keep-connection-alive []
  (async/go-loop []
    (Thread/sleep 5000)
    (if (nil? @@db_watch)
      (do (trace "_changes watch connection lost: attempting to start")
          (@init-memo)));; start listening again
    (recur)))

(defn initialize-db [name url async?]
  (swap! init-memo
         (fn [_]
           (fn []
             (swap! event-db (fn [old-val] (url/url url (if (nil? name) "nimbus-events" name))))
             (swap! run-async? (fn [_] async?))
             (when async?
               (trace "starting changes")
               (swap! db_watch (fn [_] (clutch/change-agent @event-db :include_docs true
                                                            :since (:seq (read/get-last-update-seq) 0))))
               (clutch/start-changes @db_watch)
               (trace "changes started" @db_watch)
               (clutch/changes-running? @db_watch)
               (add-watch @db_watch :echo #'db-watch-fn)))))
  ;; only start the connection polling once... on init
  
  )
