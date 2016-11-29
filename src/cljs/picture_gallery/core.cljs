(ns picture-gallery.core
  (:require [picture-gallery.router :as r]
            [picture-gallery.components.registration :as reg]
            [picture-gallery.components.login :as l]
            [picture-gallery.components.upload :as u]
            [picture-gallery.components.gallery :as g]
            [picture-gallery.ajax :refer [load-interceptors!]]
            [reagent.core :as reagent :refer [atom]]
            [reagent.session :as session]
            [goog.events :as events]
            [goog.history.EventType :as EventType]
            [ajax.core :as ajax]))

(defn account-actions [id]
  (let [expanded? (atom false)]
    (fn []
      [:div.dropdown
       {:class (when @expanded? "open")
        :on-click #(swap! expanded? not)}
       [:button.btn.btn-secondary.dropdown-toggle
        {:type :button}
        [:i.fa.fa-fw.fa-user-circle {:aria-hidden "true"}] " " id [:span.caret]]
       [:div.dropdown-menu.user-actions
        [:a.dropdown-item.btn
         {:on-click #(session/put! :modal reg/delete-account-modal)}
         [:i.fa.fa-times] "  delete account"]
        [:a.dropdown-item.btn
         {:on-click #(ajax/POST "/logout"
                                {:handler (fn [] (session/remove! :identity))})}
         [:i.fa.fa-sign-out] "  sign out"]]])))

(defn user-menu []
  (if-let [id (session/get :identity)]
    [:ul.nav.navbar-nav.pull-right
     [:li.nav-item [u/upload-button]]
     [:li.nav-item [account-actions id]]]
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
        {:on-click #(swap! collapsed? not)} [:i.fa.fa-bars]]
       [:div.collapse.navbar-toggleable-xs
        (when-not @collapsed? {:class "in"})
        [:a.navbar-brand {:href (r/home)} "Picture Gallery!"]
        [:ul.nav.navbar-nav
         [nav-link (r/home) "Home" :home collapsed?]
         [nav-link (r/about) "About" :about collapsed?]]
        [user-menu]]])))

(defn galleries [gallery-links]
  [:div.lightbox-gallery
   (for [{:keys [owner name]} gallery-links]
     ^{:key (str owner name)}
     [:div
      [:a {:href (r/gallery {:owner owner})}
       [:img {:src (str js/context "/gallery/" owner "/" name)}]]])])

(defn home-page []
  (g/list-galleries!)
  (fn []
    [:div.container
     [:div.row
      [:div.col-md-12>h2.text-xs-center "Available Galleries"]]
     (when-let [gallery-links (session/get :gallery-links)]
       [:div.row>div.col-md-12
        [galleries gallery-links]])]))

(defn about-page []
  [:div "this is the story of picture-gallery... work in progress"])

(defn modal []
  (when-let [session-modal (session/get :modal)] 
    [session-modal]))

(def pages
  {:home    #'home-page
   :gallery #'g/gallery-page
   :about   #'about-page})

(defn page []
  [:div 
   [modal] 
   [(pages @r/active-page)]])

(defn mount-components []
  (reagent/render [#'navbar] (.getElementById js/document "navbar"))
  (reagent/render [#'page] (.getElementById js/document "app")))

(defn init! []
  (load-interceptors!)
  (r/hook-browser-navigation!)
  (session/put! :identity js/identity)
  (mount-components))
