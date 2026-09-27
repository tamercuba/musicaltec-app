(ns musicaltec-app.ports.value
  (:require [clojure.string :as str]
            [schema.core :as s]))

(defn phone-valid?
  "Telefone brasileiro canônico: 10 (fixo) ou 11 (celular) dígitos."
  [phone]
  (and (string? phone)
       (boolean (re-matches #"\d{10,11}" phone))))

(defn- digit-value
  "Valor numérico (0-9) de um char de dígito."
  [c]
  #?(:clj  (Character/digit c 10)
     :cljs (js/parseInt (str c) 10)))

(defn- alnum-char-value
  "Valor de um char alfanumérico no CNPJ: 0-9 -> 0-9, A -> 17 ... Z -> 42."
  [c]
  (let [code #?(:clj  (int (Character/toUpperCase c))
                :cljs (.charCodeAt (str/upper-case c) 0))]
    (- code 48)))

(defn mod-11-dv
  "Dígito verificador módulo 11, dado uma sequência de valores e seus pesos.
  Público para permitir gerar tax-ids válidos (ex.: seed/geradores)."
  [values weights]
  (let [rem (mod (reduce + (map * values weights)) 11)]
    (if (< rem 2) 0 (- 11 rem))))

(defn person-tax-id-valid?
  "CPF canônico válido (11 dígitos + dígitos verificadores)."
  [cpf]
  (and (string? cpf)
       (boolean (re-matches #"\d{11}" cpf))
       (not (apply = cpf))
       (let [digits (map digit-value cpf)]
         (and (= (mod-11-dv (take 9 digits) (range 10 1 -1)) (nth digits 9))
              (= (mod-11-dv (take 10 digits) (range 11 1 -1)) (nth digits 10))))))

(defn company-tax-id-valid?
  "CNPJ canônico válido (12 alfanuméricos + 2 dígitos verificadores)."
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
  "Tax-id canônico é válido para o kind (CPF para :person, CNPJ para :company)."
  [kind tax-id]
  (case kind
    :person  (person-tax-id-valid? tax-id)
    :company (company-tax-id-valid? tax-id)
    false))

(defn non-negative-int?
  "Non-negative integer (>= 0)."
  [x]
  (not (neg? x)))

(def Phone
  (s/constrained s/Str phone-valid? 'phone))

(def TaxId
  (s/constrained s/Str #(or (person-tax-id-valid? %)
                            (company-tax-id-valid? %))
                 'tax-id))

(def NonNegativeInt
  (s/constrained s/Int non-negative-int? 'non-negative-int))
