(ns nimbus-api.test.countries-test
  (:require [nimbus-api.event-store :as events]
            [nimbus-api.read.countries :as countries]
            [nimbus-api.read-db :as read]
            [nimbus-api.configs :as config]
            [nimbus-api.event-store :refer [publish-event]]
            [nimbus-api.test.db-helper :as thelper]

            [midje.sweet :refer :all]
            [clojure.tools.trace :refer [trace]]
            [clojure.pprint :refer [pprint]]
            [clojure.data.json :as json]
            ))
;; (countries/get-country-docs @read/db-read "USA")
(facts "Country Tests" :db
         (fact "Inserting duplicate country updates second"
               (thelper/setup-dbs)
               (publish-event (countries/country-inserted "TEST" 11 111 1111))
               (publish-event (countries/country-inserted "TEST" 22 111 1111))
               (let [countries (countries/get-country-docs @read/db-read "TEST")]
                 (fact "Only one country found" (count countries) => 1)
                 (fact "Country Code Updated with second insert" (:two-digit (first countries)) => 22)))
         (fact "Can Get A Country with a space name"
               (publish-event (countries/country-inserted "TEST TEST" 11 111 1111))
               (let [countries (countries/get-country-docs @read/db-read "TEST TEST")]
                 (fact "Only one country found" (count countries) => 1)
                 (fact "Correct Name" (-> countries first :name) => "TEST TEST")))
         )
