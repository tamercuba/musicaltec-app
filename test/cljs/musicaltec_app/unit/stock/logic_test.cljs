(ns musicaltec-app.unit.stock.logic-test
  (:require [cljs.test :refer-macros [deftest is testing]]
            [musicaltec-app.labels :as labels]
            [musicaltec-app.stock.db :as db]
            [musicaltec-app.stock.logic :as logic.stock]))

(deftest ->instant-test
  (testing "turns an iso date into a UTC instant"
    (is (= "2026-10-11T00:00:00.000-00:00"
           (logic.stock/->instant "2026-10-11")))))

(deftest list-url-test
  (testing "encodes query and reflects tab availability"
    (is (= "/api/stock-items?q=flauta&page=2&per-page=10&available=false"
           (logic.stock/list-url {:query "flauta" :page 2 :per-page 10 :tab :sold-out})))
    (is (= "/api/stock-items?q=&page=1&per-page=10&available=true"
           (logic.stock/list-url {:query "" :page 1 :per-page 10 :tab :available})))))

(deftest item-form->body-test
  (testing "parses quantities and prices, keeps optional fields only when filled"
    (is (= {:name "Flauta" :quantity 2 :cost 15000 :default-price 30000 :brand "Yamaha" :serial "S1"}
           (logic.stock/item-form->body
            {:name "Flauta" :quantity "2" :cost "R$ 150,00" :default-price "R$ 300,00"
             :brand "Yamaha" :serial "S1" :notes ""})))
    (is (= {:name "Flauta" :quantity 1 :cost 0 :default-price 0}
           (logic.stock/item-form->body
            {:name "Flauta" :quantity "" :cost "" :default-price ""})))))

(deftest prefill-test
  (testing "builds a movement form from an existing item"
    (let [form (logic.stock/prefill {:id "abc" :name "Flauta" :quantity 5} 15000)]
      (is (= "abc" (:stock-item-id form)))
      (is (= "Flauta" (:name form)))
      (is (= 5 (:available-quantity form)))
      (is (= "R$ 150,00" (:unit-price form))))))

(deftest movement-form->body-test
  (testing "existing item movement"
    (is (= {:type "stock" :direction "out" :date "2026-10-11T00:00:00.000-00:00" :shared? true
            :items [{:stock-item-id "abc" :quantity 2 :unit-price 15000}]}
           (logic.stock/movement-form->body
            {:stock-item-id "abc" :quantity "2" :unit-price "R$ 150,00" :date "2026-10-11"}
            :out))))
  (testing "new item with installments and down payment"
    (is (= {:type "stock" :direction "in" :date "2026-10-11T00:00:00.000-00:00" :shared? true
            :items [{:name "Flauta" :quantity 2 :unit-price 15000 :brand "Yamaha"}]
            :installments-count 3
            :down-payment 5000}
           (logic.stock/movement-form->body
            {:name "Flauta" :quantity "2" :unit-price "R$ 150,00" :date "2026-10-11"
             :brand "Yamaha" :parcelado? true :installments-count "3"
             :has-down-payment? true :down-payment "R$ 50,00"}
            :in)))))

(deftest validate-movement-test
  (testing "a new item without a name is invalid"
    (is (= [(labels/label :stock/movement-name-required)]
           (logic.stock/validate-movement :sale {:stock-item-id nil :name "" :quantity "1"}))))
  (testing "selling more than available is invalid"
    (is (= [(labels/label :financial-movement/stock-unavailable)]
           (logic.stock/validate-movement :sale {:stock-item-id "abc" :name "Flauta" :quantity "3" :available-quantity 2}))))
  (testing "a valid movement returns nil"
    (is (nil? (logic.stock/validate-movement :sale {:stock-item-id "abc" :name "Flauta" :quantity "1" :available-quantity 2})))))

(deftest save-item-test
  (testing "blank name sets an error"
    (let [db (assoc-in db/default-db [:stock :item-drawer :form :name] "")
          result (logic.stock/save-item db)]
      (is (= [(labels/label :stock/name-required)]
             (get-in result [:db :stock :item-drawer :error])))))
  (testing "valid item dispatches a PUT"
    (let [db (assoc-in db/default-db [:stock :item-drawer]
                       {:id "abc" :form {:name "Flauta" :quantity "1" :cost "" :default-price ""}})
          result (logic.stock/save-item db)]
      (is (= "/api/stock-items/abc" (get-in result [:http :url])))
      (is (= :put (get-in result [:http :method])))
      (is (get-in result [:db :stock :item-drawer :saving?])))))

(deftest save-movement-test
  (testing "invalid movement sets an error and does not dispatch"
    (let [db (assoc-in db/default-db [:stock :movement-drawer]
                       {:mode :sale :form {:stock-item-id nil :name "" :quantity "1"}})
          result (logic.stock/save-movement db)]
      (is (get-in result [:db :stock :movement-drawer :error]))
      (is (nil? (:http result)))))
  (testing "valid movement dispatches a POST"
    (let [db (assoc-in db/default-db [:stock :movement-drawer]
                       {:mode :sale :form {:stock-item-id "abc" :quantity "1" :unit-price "R$ 10,00"
                                           :available-quantity 2 :date "2026-10-11"}})
          result (logic.stock/save-movement db)]
      (is (= "/api/financial-movements" (get-in result [:http :url])))
      (is (get-in result [:db :stock :movement-drawer :saving?])))))

(deftest save-movement-failure-test
  (testing "maps a known error code"
    (is (= [(labels/label :financial-movement/stock-unavailable)]
           (get-in (logic.stock/save-movement-failure db/default-db :financial-movement/stock-unavailable)
                   [:stock :movement-drawer :error]))))
  (testing "falls back to a generic message for unknown codes"
    (is (= [(labels/label :base/save-failed)]
           (get-in (logic.stock/save-movement-failure db/default-db :unknown/code)
                   [:stock :movement-drawer :error])))))

(deftest load-failure-test
  (testing "stops loading and sets the error"
    (let [result (logic.stock/load-failure (assoc-in db/default-db [:stock :loading?] true))]
      (is (false? (get-in result [:stock :loading?])))
      (is (= (labels/label :stock/load-failed) (get-in result [:stock :error]))))))
