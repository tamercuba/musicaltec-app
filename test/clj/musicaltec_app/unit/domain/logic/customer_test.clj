(ns musicaltec-app.unit.domain.logic.customer-test
  (:require [clojure.test :refer [deftest is]]
            [musicaltec-app.domain.logic.customer :as logic.customer]))

(defn- ->customer [name]
  {:customer/id     (random-uuid)
   :customer/name   name
   :customer/phones ["11999990001"]
   :customer/kind   :person
   :customer/tax-id "11144477735"})

(defn- ->query [q]
  {:query q :page 1 :per-page 10})

(deftest filter-matching-blank-query
  (is (= ["Ana" "Bruno"]
         (map :customer/name
              (logic.customer/filter-matching
               [(->customer "Ana") (->customer "Bruno")]
               (->query ""))))))

(deftest filter-matching-by-name
  (is (= ["João da Silva"]
         (map :customer/name
              (logic.customer/filter-matching
               [(->customer "João da Silva") (->customer "Maria")]
               (->query "joao")))))
  (is (= ["Bruno"]
         (map :customer/name
              (logic.customer/filter-matching
               [(->customer "Ana") (->customer "Bruno")]
               (->query "brun"))))))

(deftest filter-matching-no-match
  (is (= []
         (map :customer/name
              (logic.customer/filter-matching
               [(->customer "Ana") (->customer "Bruno")]
               (->query "xyz"))))))

(deftest sort-by-name
  (let [sorted (logic.customer/sort-by-name
                [(assoc (->customer "Bruno") :customer/id #uuid "00000000-0000-0000-0000-000000000002")
                 (assoc (->customer "Ana")   :customer/id #uuid "00000000-0000-0000-0000-000000000001")])]
    (is (= ["Ana" "Bruno"] (map :customer/name sorted)))))

(deftest ->paginated
  (let [result (logic.customer/->paginated
                [(->customer "Ana") (->customer "Bruno") (->customer "Carlos")]
                {:query "a" :page 2 :per-page 2})]
    (is (= "a" (:query result)))
    (is (= 2 (:page result)))
    (is (= 3 (:total result)))
    (is (= 2 (:total-pages result)))
    (is (= 2 (:per-page result)))
    (is (= ["Carlos"] (map :customer/name (:items result))))))
