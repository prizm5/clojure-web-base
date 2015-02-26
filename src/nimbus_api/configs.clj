(ns nimbus-api.configs
  (:require [carica.core :refer [config]]))

(def jwt-secret (or (System/getenv "jwt_secret")  "kesist1GnGBlYpYE"))
(def event-db (or (System/getenv "event_db")
                  (config :event-db)
                  "nimbus-events"))
(def read-db (or (System/getenv "read_db")
                 (config :read-db)
                 "nimbus-events-read"))

(def event-test-db-name (or (System/getenv "event_db")
                            (config :event-test-db)))

(def read-test-db-name (or (System/getenv "read_db")
                           (config :read-test-db)))

(def cloudant-url (or (System/getenv "cloudant_url")
                      (config :cloudant-url)
                      "https://steveshogren:mycloudant@steveshogren.cloudant.com/"))

