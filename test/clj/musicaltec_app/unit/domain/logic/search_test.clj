(ns musicaltec-app.unit.domain.logic.search-test
  (:require [clojure.test :refer [deftest is]]
            [musicaltec-app.domain.logic.search :as logic.search]))

(deftest normalize
  (is (= "joao silva" (logic.search/normalize "  João Silva  ")))
  (is (= "" (logic.search/normalize nil)))
  (is (= "" (logic.search/normalize "   "))))
