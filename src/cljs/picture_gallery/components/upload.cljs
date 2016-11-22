(ns picture-gallery.components.upload 
  (:require [clojure.string :as s]
            [goog.events :as gev]
            [reagent.core :as reagent :refer [atom]]
            [reagent.session :as session]
            [picture-gallery.components.common :as c]
            [picture-gallery.components.gallery :refer [fetch-gallery-thumbs!]])
  (:import goog.net.IframeIo
           goog.net.EventType
           [goog.events EventType]))

(defn upload-file! [upload-form-id status]
  (reset! status nil)
  (let [io (IframeIo.)]
    (gev/listen io
                goog.net.EventType.SUCCESS
                #(do
                   (fetch-gallery-thumbs! (session/get :identity))
                   (session/put! :page :gallery)
                   (reset! status [:div.alert.alert-success "file uploaded successfully"])))
    (gev/listen io
                goog.net.EventType.ERROR
                #(reset! status [:div.alert.alert-danger "failed to upload the file"]))
    (.setErrorChecker io #(s/includes? (.getResponseText io) "error"))
    (.sendFromForm io
                   (.getElementById js/document upload-form-id)
                   "/private-api/upload")))

(defn upload-form []
  (let [status (atom nil)
        form-id "upload-form"
        submit-function! #(upload-file! form-id status)
        cancel-function! #(session/remove! :modal)]
    (fn []
      [c/modal
       [:div "Upload File"]
       [:div 
        (when @status @status)
        [:form {:id form-id
                :enc-type "multipart/form-data"
                :method "POST"}
         [:div.form-group
          [:label {:for "file"} [:strong "Select an image for upload"]]
          [:input {:id "file" :name "file" :type "file" :auto-focus true}]]]]
       [:div 
        [:button.btn.btn-primary.btn-space
         {:on-click submit-function!}
         "Upload"]
        [:button.btn.btn-danger.btn-space
         {:on-click cancel-function!}
         "Cancel"]]
       #()
       cancel-function!])))

(defn upload-button []
  [:a.btn
   {:on-click #(session/put! :modal upload-form)}
   [:i.fa.fa-fw.fa-cloud-upload] " upload image"])
