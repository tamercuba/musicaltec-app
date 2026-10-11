(ns musicaltec-app.unit.auth.logic-test
  (:require [cljs.test :refer-macros [deftest is testing]]
            [musicaltec-app.auth.db :as db]
            [musicaltec-app.auth.logic :as logic.auth]
            [musicaltec-app.labels :as labels]))

(deftest login-test
  (testing "dispatches a login request with the password"
    (let [result (logic.auth/login db/default-db "secret")]
      (is (= "/api/login" (get-in result [:http :url])))
      (is (= "secret" (get-in result [:http :basic-password])))
      (is (get-in result [:db :auth :logging-in?])))))

(deftest login-success-test
  (testing "persists the csrf token and navigates"
    (let [result (logic.auth/login-success db/default-db {:csrf-token "tok"})]
      (is (= "tok" (get-in result [:db :auth :csrf-token])))
      (is (get-in result [:db :auth :logged-in?]))
      (is (= [:csrf-token "tok"] (:storage/put! result)))
      (is (= :customers (:navigate result))))))

(deftest login-failure-test
  (testing "maps invalid credentials and falls back for unknown codes"
    (is (= (labels/label :auth/invalid-credentials)
           (get-in (logic.auth/login-failure db/default-db :auth/invalid-credentials)
                   [:auth :login-error])))
    (is (= (labels/label :auth/login-failed)
           (get-in (logic.auth/login-failure db/default-db :unknown/code)
                   [:auth :login-error])))))

(deftest logout-test
  (testing "clears auth and navigates to login"
    (let [result (logic.auth/logout (assoc-in db/default-db [:auth :logged-in?] true))]
      (is (false? (get-in result [:db :auth :logged-in?])))
      (is (= :csrf-token (:storage/del! result)))
      (is (= :login (:navigate result))))))
