(ns picture-gallery.components.login
  (:require [reagent.core :refer [atom]]
            [reagent.session :as session]
            [goog.crypt.base64 :as b64]
            [clojure.string :as string]
            [ajax.core :as ajax]
            [picture-gallery.components.common :as c]))

(def timeout-ms (* 1000 60 30))

(defn session-timer []
  (when (session/get :identity)
    (if (session/get :user-event)
      (do
        (session/remove! :user-event)
        (js/setTimeout #(session-timer) timeout-ms))
      (session/remove! :identity))))

(defn encode-auth [user pass]
  (->> (str user ":" pass)
       (b64/encodeString)
       (str "Basic ")))

(defn login! [fields error]
  (let [{:keys [id pass]} @fields]
    (reset! error nil)
    (ajax/POST "/login"
               {:headers {"Authorization" (encode-auth (string/trim id) pass)}
                :handler #(do
                            (session/remove! :modal)
                            (session/put! :identity id)
                            (js/setTimeout session-timer timeout-ms)
                            (reset! fields nil))
                :error-handler #(reset! error (get-in % [:response :message]))})))

(defn login-form []
  (let [fields (atom {})
        error (atom nil)
        submit-function! #(login! fields error)
        remove-function! #(session/remove! :modal)]
    (fn []
      [c/modal
       [:div "Picture Gallery Login"]
       [:div 
        [:div.card.card-block.bg-faded
         [:strong "* required field"]]
        [c/text-input-focus "name" :id "enter a user name" fields]
        [c/password-input "password" :pass "enter a password" fields]
        (when-let [error @error]
          [:div.alert.alert-danger error])]
       [:div
        [:button.btn.btn-primary.btn-space
         {:on-click submit-function!}
         "Login"]
        [:button.btn.btn-danger.btn-space
         {:on-click remove-function!}
         "Cancel"]]
       submit-function!
       remove-function!])))

(defn login-button []
  [:button.btn.btn-primary
   {:on-click #(session/put! :modal login-form)}
   "Login"])

