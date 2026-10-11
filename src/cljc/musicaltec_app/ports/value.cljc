(ns musicaltec-app.ports.value
  (:require [clojure.string :as str]
            [schema.core :as s]))

(defn phone-valid?
  "Canonical Brazilian phone number: 10 (landline) or 11 (mobile) digits."
  [phone]
  (and (string? phone)
       (boolean (re-matches #"\d{10,11}" phone))))

(defn- digit-value
  "Numeric value (0-9) of a digit char."
  [c]
  #?(:clj  (Character/digit c 10)
     :cljs (js/parseInt (str c) 10)))

(defn- alnum-char-value
  "Value of an alphanumeric char in a CNPJ: 0-9 -> 0-9, A -> 17 ... Z -> 42."
  [c]
  (let [code #?(:clj  (int (Character/toUpperCase c))
                :cljs (.charCodeAt (str/upper-case c) 0))]
    (- code 48)))

(defn mod-11-dv
  "Mod 11 check digit, given a sequence of values and their weights.
  Public to allow generating valid tax-ids (e.g. seed/generators)."
  [values weights]
  (let [rem (mod (reduce + (map * values weights)) 11)]
    (if (< rem 2) 0 (- 11 rem))))

(defn person-tax-id-valid?
  "Valid canonical CPF (11 digits + check digits)."
  [cpf]
  (and (string? cpf)
       (boolean (re-matches #"\d{11}" cpf))
       (not (apply = cpf))
       (let [digits (map digit-value cpf)]
         (and (= (mod-11-dv (take 9 digits) (range 10 1 -1)) (nth digits 9))
              (= (mod-11-dv (take 10 digits) (range 11 1 -1)) (nth digits 10))))))

(defn company-tax-id-valid?
  "Valid canonical CNPJ (12 alphanumeric + 2 check digits)."
  [cnpj]
  (and (string? cnpj)
       (let [cnpj (-> cnpj str/upper-case (str/replace #"[^0-9A-Z]" ""))]
         (and (boolean (re-matches #"[A-Z0-9]{12}\d{2}" cnpj))
              (let [chars (seq cnpj)
                    base  (take 12 chars)
                    dv1   (nth chars 12)
                    dv2   (nth chars 13)]
                (and (= (mod-11-dv (map alnum-char-value base) [5 4 3 2 9 8 7 6 5 4 3 2])
                        (digit-value dv1))
                     (= (mod-11-dv (map alnum-char-value (concat base [dv1]))
                                   [6 5 4 3 2 9 8 7 6 5 4 3 2])
                        (digit-value dv2))))))))

(defn tax-id-matches-kind?
  "Canonical tax-id is valid for the kind (CPF for :person, CNPJ for :company)."
  [kind tax-id]
  (case kind
    :person  (person-tax-id-valid? tax-id)
    :company (company-tax-id-valid? tax-id)
    false))

(defn non-negative-int?
  "Non-negative integer (>= 0)."
  [x]
  (not (neg? x)))

(defn not-future?
  "True if the instant is not in the future."
  [x]
  (<= (.getTime x)
      #?(:clj  (System/currentTimeMillis)
         :cljs (.getTime (js/Date.)))))

(def Phone
  (s/constrained s/Str phone-valid? 'phone))

(def TaxId
  (s/constrained s/Str #(or (person-tax-id-valid? %)
                            (company-tax-id-valid? %))
                 'tax-id))

(def NonNegativeInt
  (s/constrained s/Int non-negative-int? 'non-negative-int))

(def NotFutureDate
  (s/constrained s/Inst not-future? 'not-future-date))
