(ns musicaltec-app.unit.domain.logic.pagination-test
  (:require [clojure.test :refer [deftest is]]
            [musicaltec-app.domain.logic.pagination :as logic.pagination]))

(deftest total-pages
  (is (= 1 (logic.pagination/total-pages 0 10)))
  (is (= 3 (logic.pagination/total-pages 25 10))))

(deftest clamp-page
  (is (= 1 (logic.pagination/clamp-page 0 3)))
  (is (= 3 (logic.pagination/clamp-page 99 3))))

(deftest offset
  (is (= 0 (logic.pagination/offset 1 10)))
  (is (= 10 (logic.pagination/offset 2 10))))
