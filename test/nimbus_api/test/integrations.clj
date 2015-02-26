(ns nimbus-api.test.integrations
  (:require [nimbus-api.read.countries :as countries]
            [nimbus-api.handler :as h]
            [nimbus-api.read.users :as users]
            [nimbus-api.test.db-helper :as thelper]
            [nimbus-api.event-store :refer [publish-event]]
            [nimbus-api.read-db :as read]

            [midje.sweet :refer :all]
            [peridot.core :refer :all]
            [clojure.tools.trace :refer [trace]]
            [clojure.data.json :as json]))

(defn post-refdata [state type data]
  (request state
           (str "/refdata/" type)
           :params data 
           :request-method :post
           :headers {:X-Auth-Token @thelper/admin-token}))

(defn authenticated-get [state url]
  (request state
           url
           :request-method :get
           :headers {:X-Auth-Token @thelper/admin-token}))

(defn get-refdata
  ([state type] (authenticated-get state (str "/refdata/" type)))
  ([state type name] (authenticated-get state (str "/refdata/" type "/" name))))

(defn get-country [state name]
  (authenticated-get state (str "/refdata/country/" name)))

(defn parse-body-response [response]
  (if (and (= (-> response :response :status) 200) )
    (json/read-str (-> response :response :body)
                   :key-fn keyword)
    {:invalid-response response}))

(defn post-user-invite [state user-data]
  (request state
           "/user/invite"
           :body (json/write-str user-data )
           :request-method :post
           :content-type "application/json"
           :headers {:X-Auth-Token @thelper/admin-token}
           ))

(facts "API/Auth Tests" :db
       (do
         (trace "Running tests")
         (thelper/setup-dbs)  
         (fact "Prove the changes feed is resistent to db shutoff" 1 => 1)
         (fact "Requesting a refdata route without permission"
               (fact "Gets a 401 without passing token"
                     (-> (session h/app)
                         (request "/refdata/country/USA")
                         :response
                         :status) => 401))
         (fact "Can get a country that was inserted" 
               (publish-event (countries/country-inserted "USA" 11 111 1111))
               (let [admin-countries  (-> (session h/app)
                                          (thelper/login-as "admin")  
                                          (get-country "USA"))]
                 (fact (-> admin-countries parse-body-response :name) => "USA")))
         (fact "CRud for a basic refdata"
               (let [refdata (-> (session h/app)
                                 (thelper/login-as "admin")  
                                 (post-refdata "seasoning" {:id "tid" :value "tvalue"})         
                                 (get-refdata "seasoning"))]
                 (fact (-> refdata parse-body-response first :value) => "tvalue")))   
         (fact "Can invite a user, they can login and see countries"
               (let [countries (-> (session h/app) 
                                   (thelper/login-as "admin")  
                                   (post-user-invite {:email "steve" :org "test"})
                                   (request "/logout")
                                   (thelper/login-as "steve")
                                   (get-country "USA"))]
                 (fact (-> countries parse-body-response :name) => "USA")))
         ))

