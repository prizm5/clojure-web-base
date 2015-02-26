(ns nimbus-api.read.test
  (:require [clojure.test :refer :all]))

;; Expand right C-)
;; Shorten right C-}
;; Eval top level sexpr C-A-x
;; Eval previous sexpr C-xC-e

(defn parse-card [card]
  (let [suit (keyword (str (last card)))
        face (Integer/parseInt ( .substring card 0 (- ( .length card) 1) ))]
    [suit face]))

(defn differ-by-1
  ([nums] (differ-by-1 nums 0))
  ([nums i]
     (if (= 1 (- (nums i) (nums (+ 1 i) )))
       (if (> (count nums) (+ i 2))
         (differ-by-1 nums (+ 1 i))
         true
         )
       false))
  )

(defn got? [arr item] (contains? (set 'arr)) item )

(defn reversed-deck [deck] ( vec (reverse (sort (map last deck)))))

(defn eval-flush-suit [deck]
  (let [deck (reversed-deck deck)]
    (if (differ-by-1 deck 0) :straight-flush :flush)))

(defn col-contains? [needle haystack]
  (boolean (some #{needle} haystack)))

(defn has-2 [arr item]
  (not (empty? (filter (fn [x] (and (= (last x) 2) (= (first x) item)))
                       (frequencies arr)))))

(defn has-dups [deck]
  (let [freq (frequencies (map last deck))]
    (map #(case % 
            2 :one-pair
            3 :three-of-a-kind
            4 :four-of-a-kind) (sort (filter #(> % 1)  (vals freq))))))

(defn find-hand [deck]
  (let [deck (map parse-card (.split deck " "))
        res (has-dups deck) ]
    (cond 
     (= 1 (count (group-by first deck ))) (eval-flush-suit deck)
     (differ-by-1 (reversed-deck deck) 0) :straight
     (has-2 res :one-pair) :two-pair
     (and (col-contains? :three-of-a-kind res) (col-contains? :one-pair res)) :full-house
     :else (first res))))

(find-hand "2H 2S 3H 3P")



;; flush all same suit
(testing "hand finding"
  (is (= (find-hand "3H 4H 5H 6H 7H") :straight-flush))
  (is (= (find-hand "2H 4H 5H 6H 3S") :straight))
  (is (= (find-hand "2H 4H 5H 9H 8H") :flush))
  (is (= (find-hand "2H 2S 2H 2H 8H") :four-of-a-kind))
  (is (= (find-hand "2H 2S 2H 5H 8H") :three-of-a-kind))
  (is (= (find-hand "2H 2S 5H 5H 5H") :full-house))
  (is (= (find-hand "2H 2S 5H 5H 8H") :two-pair))
  (is (= (find-hand "2H 2S 5H 9H 8H") :one-pair)))

