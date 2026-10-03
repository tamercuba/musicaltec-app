(ns musicaltec-app.core.format
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

(defn mask-brl
  "Input mask: strips leading zeros, formats as \"R$ X,YY\".
  Truly empty -> \"\"; all zeros -> \"R$ 0,00\"."
  [s]
  (let [raw (digits s)]
    (if (seq raw)
      (let [d (str/replace raw #"^0+" "")
            n (count d)]
        (cond
          (zero? n) "R$ 0,00"
          (= 1 n) (str "R$ 0,0" d)
          (= 2 n) (str "R$ 0," d)
          :else (str "R$ " (subs d 0 (- n 2)) "," (subs d (- n 2)))))
      "")))

(defn cents->brl
  "Converts integer cents to a BRL string (e.g. 123456 -> \"R$ 1234,56\")."
  [cents]
  (when (number? cents)
    (mask-brl (str cents))))

(defn brl->cents
  "Parses a BRL amount string (e.g. \"1.234,56\" or \"1234,56\") to integer cents."
  [s]
  (when (and s (seq s))
    (let [v (js/parseFloat (-> s (str/replace #"[^\d,]" "") (str/replace #"," ".")))]
      (when-not (js/isNaN v)
        (js/Math.round (* v 100))))))
