(ns nimbus-api.events
  (:require
   [clj-time.core :as t] 
   [clj-time.format :as f]
   [clojure.core.async :as async]))

(def events (async/chan))


(defmulti persist (fn [x _] (:event x)))

(defn create-event [event doc executor]
  {:record-type :event
   :event event
   :date (f/unparse (f/formatters :basic-date-time) (t/now)) 
   :executor executor
   :version 1  :doc doc})
