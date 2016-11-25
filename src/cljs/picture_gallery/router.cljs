(ns picture-gallery.router
  (:require [picture-gallery.components.gallery :as g]
            [secretary.core :as secretary :include-macros true]
            [reagent.core :as reagent :refer [atom]]
            [reagent.session :as session]
            [goog.events :as events]
            [goog.history.EventType :as EventType])
  (:import goog.history.Html5History))


(defonce history (Html5History.))

(defn trans [path]
  (.setToken history path))

(def active-page (atom (session/get :page)))

(secretary/set-config! :prefix "#")

(secretary/defroute home "/" []
  (session/put! :page :home)
  (reset! active-page :home))

(secretary/defroute gallery "/gallery/:owner" [owner]
  (g/fetch-gallery-thumbs! owner)
  (session/put! :page :gallery)
  (reset! active-page :gallery))

(secretary/defroute about "/about" []
  (session/put! :page :about)
  (reset! active-page :about))

(defn- on-popstate [e]
  (-> e .-token secretary/dispatch!))

(defn hook-browser-navigation! []
  (events/listen history EventType/NAVIGATE on-popstate)
  (doto history 
    (.setEnabled true)
    (.setPathPrefix "")
    (.setUseFragment true)))
