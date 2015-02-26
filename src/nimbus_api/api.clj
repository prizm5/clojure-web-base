(ns nimbus-api.api
  (:require [clojure.data.json :as json]
            [compojure.core :refer :all]
            [nimbus-api.configs :as configs]
            [nimbus-api.read.ref-data :as ref-data]
            [ring.util.response :refer [resource-response response redirect content-type]]
            [nimbus-api.read-db :as read-db]
            [clojure.tools.trace :refer [trace]]
            [clojure.tools.logging :as log]
            [nimbus-api.event-store :as events]
            [clj-http.client :as client]
            [nimbus-api.read.countries :as countries]))

;; (ref-data/get-ref-data "seasoning" @read-db/db-read)  

(defn refdata-routes [type]
  (routes
   (GET (str "/refdata/" type) req
        (ref-data/get-ref-data @read-db/db-read type))
   (GET (str "/refdata/test" type) req (ref-data/get-ref-data @read-db/db-read type))
   (GET (str "/refdata/" type "/:name") [name] (ref-data/get-ref-data type @read-db/db-read type name))
   (POST (str "/refdata/" type) req
         (let [params (:params req)]
           (log/info "Create refdata event: " params)
           (events/insert-ref-data (:id params) type (:value params)))
         {:status 200 :body "success"})))

(defroutes misc-routes
  (GET "/refdata/country" req
       (json/write-str (countries/get-country-docs @read-db/db-read)))
  (GET "/refdata/country/:name" [name]
       (json/write-str (first (countries/get-country-docs @read-db/db-read name))))
  (POST "/user/register" req (let [params (:params req)]
                               (log/info "Register user event: " params)
                               (events/register-user (:id params) (:email params))
                               "user registered"))
  (POST "/user/invite" req  (let [params (:params req)]
                              (log/info "Invite user event: " params)
                              (events/invite-user (:email params)
                                                  (:org params))
                              "User invited")))

(def basic-ref-names ["agreement-status-detail" "bond-purpose" "cmo-abs-asset-class" "collateral-type"
                      "coupon-type" "exchange" "fund-source" "industry-group" "industry-sector"
                      "instrument-underlying-sector" "issuer-type" "municipal-region" "municipal-state" "place-of-settlement"
                      "rating-watch" "seasoning" "stock-index"])

(def api-routes (routes misc-routes
                        (apply routes (map refdata-routes basic-ref-names))))
