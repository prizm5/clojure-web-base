(ns nimbus-api.trace
  (:require [clojure.tools.trace :refer [trace]]))

;; (defn trace [x] (do (.println System/out (str "Trace: " x)) x))

(defn wrap-args-with-trace [[symb val]]
  [symb (list `trace (str "let-" symb) val)])

(defmacro tracelet [args & body]
  (let [arg-pairs (partition 2 args)
        new-bindings (vec (mapcat wrap-args-with-trace arg-pairs))]
    `(let ~new-bindings ~@body)))
