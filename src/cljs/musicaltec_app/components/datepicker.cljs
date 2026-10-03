(ns musicaltec-app.components.datepicker
  (:require [reagent.core :as r]
            [musicaltec-app.components.icons :as icons]
            ["flowbite-datepicker" :refer [Datepicker]]))

(defonce ^:private _pt-br-locale
  (let [loc (.-locales Datepicker)]
    (aset loc "pt-BR"
          (clj->js
           {:days       ["Domingo" "Segunda" "Terça" "Quarta" "Quinta" "Sexta" "Sábado"]
            :daysShort  ["Dom" "Seg" "Ter" "Qua" "Qui" "Sex" "Sáb"]
            :daysMin    ["Do" "Se" "Te" "Qu" "Qu" "Se" "Sa"]
            :months     ["Janeiro" "Fevereiro" "Março" "Abril" "Maio" "Junho"
                         "Julho" "Agosto" "Setembro" "Outubro" "Novembro" "Dezembro"]
            :monthsShort ["Jan" "Fev" "Mar" "Abr" "Mai" "Jun"
                          "Jul" "Ago" "Set" "Out" "Nov" "Dez"]
            :today      "Hoje"
            :clear      "Limpar"
            :format     "dd/mm/yyyy"
            :weekStart  0}))))

(def ^:private input-class
  "bg-neutral-secondary-medium border border-default-medium text-heading text-sm rounded-base focus:ring-brand focus:border-brand block w-full ps-10 pe-3 py-2.5 shadow-xs placeholder:text-body")

(defn- pad [n]
  (if (< n 10) (str "0" n) (str n)))

(defn- iso->date
  [iso]
  (when-let [[_ y m d] (re-matches #"(\d{4})-(\d{2})-(\d{2})" (or iso ""))]
    (js/Date. (js/parseInt y) (dec (js/parseInt m)) (js/parseInt d))))

(defn- date->iso
  [^js d]
  (when d
    (str (.getFullYear d) "-" (pad (inc (.getMonth d))) "-" (pad (.getDate d)))))

(defn datepicker
  "Campo de data com o calendário do Flowbite.

  opts — {:value <iso \"yyyy-mm-dd\"> :on-change (fn [iso]) :placeholder <str>}"
  [{:keys [value on-change placeholder]}]
  (let [inst   (r/atom nil)
        input* (r/atom nil)]
    (r/create-class
     {:component-did-mount
      (fn [_]
        (let [input @input*
              dp    (Datepicker. input
                                 #js {:format "dd/mm/yyyy"
                                      :language "pt-BR"
                                      :autohide true})]
          (reset! inst {:dp dp :input input})
          (when value
            (.setDate dp (iso->date value)))
          (.addEventListener input "changeDate"
                             (fn [e]
                               (when on-change
                                 (on-change (date->iso (.-date (.-detail e)))))))))
      :component-did-update
      (fn [this _]
        (let [new-value (:value (second (r/argv this)))
              {:keys [dp]} @inst]
          (when (and dp new-value)
            (when-not (= new-value (date->iso (.getDate dp)))
              (.setDate dp (iso->date new-value))))))
      :component-will-unmount
      (fn [_]
        (when-let [{:keys [dp]} @inst]
          (.destroy dp)
          (reset! inst nil)))
      :reagent-render
      (fn [{:keys [placeholder]}]
        [:div {:class "relative"}
         [:div {:class "absolute inset-y-0 start-0 flex items-center ps-3.5 pointer-events-none"}
          icons/calendar-icon]
         [:input {:type "text"
                  :class input-class
                  :ref #(reset! input* %)
                  :read-only true
                  :placeholder (or placeholder "dd/mm/aaaa")}]])})))
