(ns musicaltec-app.labels
  (:require [clojure.string :as str]
            [musicaltec-app.auth.labels :as auth.labels]
            [musicaltec-app.customers.labels :as customers.labels]
            [musicaltec-app.stock.labels :as stock.labels]))

(def base
  #:base{:search            "Buscar"
         :save              "Salvar"
         :saving            "Salvando..."
         :edit              "Editar"
         :delete            "Excluir"
         :actions           "Ações"
         :close             "Fechar"
         :name              "Nome"
         :loading           "Carregando..."
         :pagination-aria   "Paginação"
         :prev-page         "Anterior"
         :next-page         "Próxima"
         :showing           "Mostrando "
         :showing-of        " de "
         :toggle-theme      "Alternar modo claro/escuro"
         :date-placeholder  "dd/mm/aaaa"
         :money-placeholder "R$ 0,00"
         :save-failed       "Não foi possível salvar."
         :page-not-found    "Página não encontrada."
         :nav-home          "Início"
         :nav-os            "OS"
         :nav-sales         "Vendas"
         :nav-stock         "Estoque"
         :nav-finances      "Financeiro"
         :nav-customers     "Clientes"
         :welcome           "Bem-vindo ao Musical Tec."})

(def datepicker-locale
  {:days        ["Domingo" "Segunda" "Terça" "Quarta" "Quinta" "Sexta" "Sábado"]
   :daysShort   ["Dom" "Seg" "Ter" "Qua" "Qui" "Sex" "Sáb"]
   :daysMin     ["Do" "Se" "Te" "Qu" "Qu" "Se" "Sa"]
   :months      ["Janeiro" "Fevereiro" "Março" "Abril" "Maio" "Junho"
                 "Julho" "Agosto" "Setembro" "Outubro" "Novembro" "Dezembro"]
   :monthsShort ["Jan" "Fev" "Mar" "Abr" "Mai" "Jun"
                 "Jul" "Ago" "Set" "Out" "Nov" "Dez"]
   :today       "Hoje"
   :clear       "Limpar"})

(def labels
  (merge base
         stock.labels/labels
         customers.labels/labels
         auth.labels/labels))

(defn label
  ([k] (get labels k))
  ([k subs]
   (str/replace (get labels k)
                #"\{(\w+)\}"
                (fn [[_ m]] (str (get subs (keyword m)))))))
