(ns musicaltec-app.format
  (:require [clojure.string :as str]))

(defn digits
  [s]
  (apply str (re-seq #"\d" (or s ""))))

(defn alphanumeric
  [s]
  (str/replace (str/upper-case (or s "")) #"[^0-9A-Z]" ""))

(defn format-phone
  [s]
  (let [d    (digits s)
        n    (min 11 (count d))
        d    (subs d 0 n)
        head (if (= n 11) 5 4)]
    (cond
      (zero? n) ""
      (<= n 2) (str "(" d ")")
      (<= n (+ 2 head)) (str "(" (subs d 0 2) ") " (subs d 2))
      :else (str "(" (subs d 0 2) ") " (subs d 2 (+ 2 head)) "-" (subs d (+ 2 head))))))

(defn format-cpf
  [s]
  (let [d (subs (digits s) 0 (min 11 (count (digits s))))
        n (count d)]
    (cond
      (zero? n) ""
      (<= n 3) d
      (<= n 6) (str (subs d 0 3) "." (subs d 3))
      (<= n 9) (str (subs d 0 3) "." (subs d 3 6) "." (subs d 6))
      :else (str (subs d 0 3) "." (subs d 3 6) "." (subs d 6 9) "-" (subs d 9)))))

(defn format-cnpj
  [s]
  (let [d (subs (alphanumeric s) 0 (min 14 (count (alphanumeric s))))
        n (count d)]
    (cond
      (zero? n) ""
      (<= n 2) d
      (<= n 5) (str (subs d 0 2) "." (subs d 2))
      (<= n 8) (str (subs d 0 2) "." (subs d 2 5) "." (subs d 5))
      (<= n 12) (str (subs d 0 2) "." (subs d 2 5) "." (subs d 5 8) "/" (subs d 8))
      :else (str (subs d 0 2) "." (subs d 2 5) "." (subs d 5 8) "/" (subs d 8 12) "-" (subs d 12)))))

(defn format-tax-id
  [kind s]
  (if (= kind :company)
    (format-cnpj s)
    (format-cpf s)))
