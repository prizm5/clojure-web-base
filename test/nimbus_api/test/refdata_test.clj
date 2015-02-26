(ns nimbus-api.test.refdata-test
  (:require [nimbus-api.api :as api]
            [nimbus-api.read.ref-data :as refdata]
            [nimbus-api.event-store :as events]
            [nimbus-api.read-db :as read]
            [nimbus-api.test.db-helper :as thelper]
            [midje.sweet :refer :all]
            ))

(facts "Refdata tests" 
       (fact "Refdata list stays the same"
             ;; while not testing the actual route logic, this is the most likely
             ;; to break part: someone accidentally messes one up
             api/basic-ref-names => ["agreement-status-detail" "bond-purpose" "cmo-abs-asset-class" "collateral-type"
                                     "coupon-type" "exchange" "fund-source" "industry-group" "industry-sector"
                                     "instrument-underlying-sector" "issuer-type" "municipal-region" "municipal-state" "place-of-settlement"
                                     "rating-watch" "seasoning" "stock-index"] 
             )
       (fact "Inserting duplicate country updates second" :db
             (thelper/setup-dbs)
             (events/insert-ref-data "some id" "collateral-type"  11)
             (events/insert-ref-data "some id" "collateral-type"  22)
             (let [collateral-types (refdata/get-ref-data @read/db-read "collateral-type" "some id")]
               (fact "Only one collateral-type found" (count collateral-types) => 1)
               (fact "Country Code Updated with second insert" (:value (first collateral-types)) => 22)))
       )

