(ns picture-gallery.components.registration
  (:require [reagent.core :refer [atom]]
            [picture-gallery.components.common :as c]
            [ajax.core :as ajax]
            [picture-gallery.validation :refer [registration-errors]]
            [reagent.session :as session]))

(defn register! [fields errors]
  (reset! errors (registration-errors @fields))
  (when-not @errors
    (ajax/POST "/register"
               {:params @fields
                :handler #(do
                            (session/put! :identity (:id @fields))
                            (reset! fields {})
                            (session/remove! :modal))
                :error-handler #(reset! errors {:server-error (get-in % [:response :message])})})))

(defn registration-form []
  (let [fields (atom {})
        error (atom nil)]
    (fn []
      [c/modal
       [:div "Picture Gallery Registration"]
       [:div
        [:div.card.card-block.bg-faded
         [:strong "* required field"]]
        [c/text-input "name" :id "enter a user name" fields]
        (when-let [error (first (:id @error))]
          [:div.alert.alert-danger error])
        [c/password-input "password" :pass "enter a password" fields]
        (when-let [error (first (:pass @error))]
          [:div.alert.alert-danger error])
        [c/password-input "password" :pass-confirm "re-enter the password" fields]
        (when-let [error (:server-error @error)]
          [:div.alert.alert-danger error])]
       [:div
        [:button.btn.btn-primary.btn-space
         {:on-click #(register! fields error)}
         "Register"]
        [:button.btn.btn-danger.btn-space
         {:on-click #(session/remove! :modal)}
         "Cancel"]]])))

(defn registration-button []
  [:button.btn.btn-info
   {:on-click #(session/put! :modal registration-form)}
   "Register"])

(defn delete-account! []
  (ajax/POST "/private-api/delete-account"
             {:handler #(do
                          (session/remove! :identity)
                          (session/put! :page :home))}))

(defn delete-account-modal []
  (fn []
    [c/modal
     [:div>h2.alert.alert-danger "Delete Account!"]
     [:div>p "Are you sure you wish to delete the account and associated gallery?"]
     [:div
      [:button.btn.btn-primary.btn-space
       {:on-click (fn []
                    (delete-account!)
                    (session/remove! :modal))}
       "Delete"]
      [:button.btn.btn-danger.btn-space
       {:on-click (fn [] (session/remove! :modal))}
       "Cancel"]]]))
