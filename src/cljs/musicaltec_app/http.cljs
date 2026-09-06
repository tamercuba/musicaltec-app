(ns musicaltec-app.http
  "Efeito re-frame :http baseado em js/fetch.

  Contrato do efeito:
    {:method        :get | :post | :put | :delete   (default :get)
     :url           \"/api/...\"
     :body          map (serializado como JSON)
     :basic-password \"...\"  ; login via Basic Auth (Authorization)
     :on-success    [:event-id]      ; despacha (conj on-success body-parsed)
     :on-failure    [:event-id]}     ; despacha (conj on-failure {:status .. :code ..})

  Cookies da sessão são enviados via `credentials: same-origin`. Para métodos
  não-seguros, o header `X-CSRF-Token` é anexado quando há token salvo."
  (:require [re-frame.core :as rf]
            [musicaltec-app.storage :as storage]))

(defn- unsafe-method? [method]
  (contains? #{:post :put :delete :patch} method))

(defn- parse-json [text]
  (when (and text (seq text))
    (try
      (js->clj (js/JSON.parse text) :keywordize-keys true)
      (catch js/Error _ nil))))

(defn- status-ok? [status]
  (and (>= status 200) (< status 300)))

(rf/reg-fx
 :http
 (fn [{:keys [method url body basic-password on-success on-failure]
       :or {method :get}}]
   (let [token   (storage/get-item :csrf-token)
         headers (cond-> {"Accept" "application/json"}
                   body (assoc "Content-Type" "application/json")
                   basic-password (assoc "Authorization"
                                         (str "Basic " (js/btoa (str ":" basic-password))))
                   (and (unsafe-method? method) token)
                   (assoc "X-CSRF-Token" token))
         opts    (cond-> {:method (name method)
                          :credentials "same-origin"
                          :headers (clj->js headers)}
                   body (assoc :body (js/JSON.stringify (clj->js body))))]
     (-> (js/fetch url (clj->js opts))
         (.then (fn [resp]
                  (let [status (.-status resp)]
                    (-> (.text resp)
                        (.then (fn [txt]
                                 (let [data (parse-json txt)]
                                   (if (status-ok? status)
                                     (when on-success
                                       (rf/dispatch (conj on-success data)))
                                     (do
                                       (when (= status 401)
                                         (rf/dispatch [:auth/expired]))
                                       (when on-failure
                                         (rf/dispatch
                                          (conj on-failure {:status status
                                                            :code (:code data)}))))))))))))
         (.catch (fn [_]
                   (when on-failure
                     (rf/dispatch (conj on-failure {:status 0
                                                    :code :network-error})))))))))
