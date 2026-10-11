(ns musicaltec-app.unit.customers.logic-test
  (:require [cljs.test :refer-macros [deftest is testing]]
            [musicaltec-app.customers.db :as db]
            [musicaltec-app.customers.logic :as logic.customers]
            [musicaltec-app.labels :as labels]))

(def valid-cpf "11144477735")

(deftest form->body-test
  (testing "trims, formats phones and tax-id, omits blank email"
    (is (= {:name "João da Silva"
            :phones ["11999990001"]
            :kind :person
            :tax-id valid-cpf}
           (logic.customers/form->body
            {:name "  João da Silva  "
             :phones ["(11) 99999-0001"]
             :email "   "
             :kind :person
             :tax-id "111.444.777-35"}))))
  (testing "keeps a non-blank email"
    (is (= "joao@example.com"
           (:email (logic.customers/form->body
                    {:name "João" :phones ["11999990001"] :email " joao@example.com "
                     :kind :person :tax-id valid-cpf}))))))

(deftest explain-errors-test
  (testing "valid dto has no errors"
    (is (nil? (logic.customers/explain-errors
               {:name "João" :phones ["11999990001"] :kind :person :tax-id valid-cpf}))))
  (testing "blank name reports the name error"
    (is (= [(labels/label :customers/name-required)]
           (logic.customers/explain-errors
            {:name "" :phones ["11999990001"] :kind :person :tax-id valid-cpf}))))
  (testing "kind/tax-id mismatch reports the mismatch error"
    (is (= [(labels/label :customers/tax-id-mismatch-company)]
           (logic.customers/explain-errors
            {:name "João" :phones ["11999990001"] :kind :company :tax-id valid-cpf})))))

(deftest remove-phone-test
  (testing "keeps a single empty phone when removing the last one"
    (is (= [""] (get-in (logic.customers/remove-phone db/default-db 0)
                        [:customers :drawer :form :phones]))))
  (testing "removes the phone at the given index"
    (let [db (assoc-in db/default-db [:customers :drawer :form :phones] ["11" "22" "33"])
          result (logic.customers/remove-phone db 1)]
      (is (= ["11" "33"] (get-in result [:customers :drawer :form :phones]))))))

(deftest save-failure-test
  (testing "maps a known error code"
    (is (= [(labels/label :customer/tax-id-taken)]
           (get-in (logic.customers/save-failure db/default-db :customer/tax-id-taken)
                   [:customers :drawer :error]))))
  (testing "falls back to a generic message"
    (is (= [(labels/label :base/save-failed)]
           (get-in (logic.customers/save-failure db/default-db :unknown/code)
                   [:customers :drawer :error])))))
