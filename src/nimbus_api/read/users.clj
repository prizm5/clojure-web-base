(ns nimbus-api.read.users
  (:require [nimbus-api.events :refer :all]
            [nimbus-api.helpers :as help]
            [cemerick.url :as url]
            [clj-jwt.core  :refer :all]
            [cemerick.friend :as friend]
            (cemerick.friend [workflows :as workflows]
                             [credentials :as creds])
            [clojure.tools.trace :as trace]
            [nimbus-api.cloudant :as cloudant]
            [com.ashafa.clutch :as clutch]))

(def user-role ::user)

(defn get-user-by-identity [id db]
  (:doc (first (cloudant/get-view "user_by_identity" db id))))

;; (get-user-by-identity "admin" db)
;; (def db (url/url  "https://steveshogren:mycloudant@steveshogren.cloudant.com/" "nimbus-events-read")) 

(defn get-users [db]
  (map (fn [u] { (.toString (:email u)) {:username (:email u) :password (:secret u) :roles #{user-role}}}) 
       (map :doc (cloudant/get-view "user_by_email"))))

(defn get-user-by-email [email db]
  (if (not (empty? email))
    (:doc (first (cloudant/get-view "user_by_email" db email)))))

(defn get-token-user [db user]
  (when-let [u (get-user-by-email user db)]
    {:username (:email u) :password (creds/hash-bcrypt (:secret u)) :roles #{user-role}}))

;; (get-token-user db "admin")
;; (get-user-by-email "admin" db)
;; (def db (url/url  "https://steveshogren:mycloudant@steveshogren.cloudant.com/" "koopa-events-read")) 

(defmethod persist :user-registered [user-event db]
  (let [user-event (:doc user-event)]
    (when-let [doc (get-user-by-email (:email user-event) db)]
      (assoc doc :identity (:identity user-event)))))

(defn sanitize-login [u]
  (clojure.string/replace u #"\S+//(\S+)/(\S+)" "$1:$2" ))

(defn user-registered [id email]
  (create-event :user-registered  {:identity (sanitize-login id) :email email :version 1} nil))

;; user invited
(defn make-user [org email]
  {:org org 
   :email email 
   :secret (help/uuid)
   :type :user
   :identity nil})

(defmethod persist :user-invited [user-event db]
  (let [user-event (:doc user-event)]
    (if-let [user-doc (get-user-by-email (:email user-event) db)]
      (assoc user-doc :org (:org user-event))
      (make-user (:org user-event) (:email user-event)))))

(defn user-invited [email org executor]
  (create-event :user-invited {:email email :org org :version 1} executor))

;; orgs 
(defn get-orgs [db]
  (map :doc (clutch/get-view db "views" "org" {:include_docs true})))

(defn get-org-by-name [name db]
  (:doc (first (clutch/get-view db "views" "org" {:keys [name] :include_docs true}))))

;; (def db (url/url  "https://steveshogren:mycloudant@steveshogren.cloudant.com/" "nimbus-auth")) 

(defmethod persist :org-created [event db]
  (let [event (:doc event)]
    (when (not (get-org-by-name (:name event) db))
      {:name (:name event) :type :org})))


(defn org-created [name executor]
  (create-event :org-created {:identity (help/uuid) :name name :version 1} executor))

(org-created "ibm" "jim")
{:record-type :event, :event :org-created, :date "20150108T151142.171Z", :executor "jim", :version 1, :doc {:identity "e2481f62-30df-43ad-85d6-0feadc26eb27", :name "ibm", :version 1}}
