(ns nimbus-api.read.ref-data
  (:require [nimbus-api.events :refer :all]
            [nimbus-api.helpers :as help]
            [cemerick.url :as url]
            [clj-jwt.core  :refer :all]
            [clojure.tools.trace :as trace]
            [com.ashafa.clutch :as clutch]))

(comment
  (def db (url/url  "https://steveshogren:mycloudant@steveshogren.cloudant.com/" "koopa-test-read"))
  (get-ref-data db "collateral-type")
  (get-ref-data db "collateral-type" "some id")
  )

(defn get-ref-data
  ([db type name] (map :value (clutch/get-view db "views" "refdata_name" {:keys [[type name]]})))
  ([db type] (map :value (clutch/get-view db "views" "refdata" {:keys [type]}))))

(defmethod persist :ref-data-added [refdata-event db]
  (let [refdata-doc (:doc refdata-event)
        existing-data (first (get-ref-data db (:reftype refdata-doc) (:name refdata-doc)))
        refdata-doc (merge existing-data refdata-doc)]
    (assoc refdata-doc :type :refdata)))

(defn ref-data-added [name reftype value]
  (create-event :ref-data-added {:name name
                                 :reftype reftype
                                 :value value}
                nil))
