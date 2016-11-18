(ns picture-gallery.core
  (:require [picture-gallery.components.registration :as reg]
            [picture-gallery.components.login :as l]
            [picture-gallery.components.upload :as u]
            [picture-gallery.ajax :refer [load-interceptors!]]
            [reagent.core :as reagent :refer [atom]]
            [reagent.session :as session]
            [ajax.core :as ajax])
  (:import goog.History))

(defn user-menu []
  (if-let [id (session/get :identity)]
    [:ul.nav.navbar-nav.pull-right
     [:li.nav-item [u/upload-button]]
     [:li.nav-item
      [:button.dropdown-item.btn
       {:on-click #(ajax/POST
                    "/logout"
                    {:handler (fn [] (session/remove! :identity))})}
       [:i.fa.fa-user] " " id " | sign out"]]]
    [:ul.nav.navbar-nav.pull-right
     [:li.nav-item [l/login-button]]
     [:li.nav-item [reg/registration-button]]]))

(defn nav-link [uri title page collapsed?]
  [:li.nav-item
   {:class (when (= page (session/get :page)) "active")}
   [:a.nav-link
    {:href uri
     :on-click #(reset! collapsed? true)} title]])

(defn navbar []
  (let [collapsed? (atom true)]
    (fn []
      [:nav.navbar.navbar-light.bg-faded
       [:button.navbar-toggler.hidden-sm-up
        {:on-click #(swap! collapsed? not)} "☰"]
       [:div.collapse.navbar-toggleable-xs
        (when-not @collapsed? {:class "in"})
        [:a.navbar-brand {:href "#/"} "Picture Gallery!"]
        [:ul.nav.navbar-nav
         [nav-link "#/" "Home" :home collapsed?]
         [nav-link "#/about" "About" :about collapsed?]]
        [user-menu]]])))

(defn modal []
  (when-let [session-modal (session/get :modal)] 
    [session-modal]))

(defn page []
  [:div 
   [navbar]
   [modal] 
   [:div.container>div.starter-template
    [:h1 "Picture Gallery Smart Project"]
    [:p.lead "Esta pagina esta en construcción "]]])

(defn mount-components []
  (reagent/render-component [page] (.getElementById js/document "app")))

(defn init! []
  (load-interceptors!)
  (session/put! :identity js/identity)
  (mount-components))
