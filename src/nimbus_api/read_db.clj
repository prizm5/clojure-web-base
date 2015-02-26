(ns nimbus-api.read-db
  (:require [cemerick.url :as url]
            [clojure.core.async :as async]

            [nimbus-api.events :refer :all]
            [nimbus-api.configs :as configs]
            [nimbus-api.read.countries :refer :all]
            [nimbus-api.read.users :refer :all]
            [nimbus-api.helpers :refer [try-times]]

            [clojure.java.shell :refer [sh]]
            [clojure.tools.trace :refer [trace deftrace]]
            [clojure.tools.logging :as log]


            [com.ashafa.clutch :as clutch]))

(def db-read (atom nil))

(defn get-document [doc-id]
  (clutch/get-document @db-read doc-id))

(defn get-last-update-seq []
  (clutch/get-document @db-read "last-update-seq"))

(defn put-doc-and-update-seq [doc event-seq]
  (let [seq-doc {:_id "last-update-seq" :seq event-seq}
        seq-last  (log/with-logs 'nimbus-api.read-db (time (get-last-update-seq)))
        seq-doc (if seq-last (assoc seq-doc :_rev (:_rev seq-last)) seq-doc)]
    (log/with-logs 'nimbus-api.read-db
      (clutch/put-document @db-read  seq-doc)
      (clutch/put-document @db-read doc))))

;; TODO - Unused?
(defn make-a-user [email]
  (if (nil? (get-user-by-email email @db-read))
    (clutch/put-document @db-read (make-user "test" email))))

;; TODO - Unused?
(defn make-admin-user []
  (make-a-user "sashogre@us.ibm.com")
  (make-a-user "admin"))

(defn write-to-read [doc]
  (log/info (str "Persisting" doc))
  (let [event-seq (:event-seq doc)]
    (try-times 3 (put-doc-and-update-seq (persist doc @db-read) event-seq))))

(deftrace initialize-db [name url]
  (swap! db-read (fn [old-val] (url/url url (if (nil? name) "nimbus-auth" name)))))

(defn recreate-db [db]
  (sh "gulp" "deleteDb" "--db" db)
  (sh "gulp" "createDb" "--db" db)
  (sh "gulp" "createViews" "--db" db))

;; (setup-dbs nil nil true)  
;; (setup-dbs nil nil)  
(defn setup-dbs
  ([] (setup-dbs nil nil false))
  ([event read] (setup-dbs event read false))
  ([event read seed]
     (let [event (or event configs/event-db)
           read (or read configs/read-db)]
       (trace "Creating event db" event)
       (recreate-db event)
       (trace "Creating read db" read)
       (recreate-db read)
       (when seed
         (trace "publishing admins")
         (sh "gulp" "publishAdmins" "--db" event)
         (trace "publishing registered")
         (sh "gulp" "publishRegistered" "--db" event)
         (trace "publishing countries")
         (sh "gulp" "publishCountries" "--db" event)))))




