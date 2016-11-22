(ns picture-gallery.components.gallery
  (:require [reagent.core :as reagent :refer [atom]]
            [reagent.session :as session]
            [ajax.core :as ajax]
            [clojure.string :as s]
            [picture-gallery.components.common :as c]))

(defn delete-image! [name]
  (ajax/POST "/private-api/delete-image"
             {:params {:image-name (s/replace name #"thumb-" "")
                       :thumb-name name}              
              :handler #(do
                          (session/update-in! [:thumbnail-links]
                                              (fn [links]
                                                (remove
                                                 (fn [link] (= name (:name link)))
                                                 links)))
                          (session/remove! :modal))}))

(defn delete-image-button [owner name]
  (session/put! :modal
                (fn []
                  [c/modal
                   [:p "Remove " name "?"]
                   [:div [:img {:src (str "/gallery/" owner "/" name)}]]
                   [:div
                    [:button.btn.btn-primary.btn-space
                     {:on-click #(delete-image! name)}
                     "Delete"]
                    [:button.btn.btn-danger.btn-space
                     {:on-click #(session/remove! :modal)}
                     "Cancel"]]])))

(defn partition-links [links]
  (when links
    (vec (partition-all 6 links))))

(defn foward [i pages]
  (if (< i (dec pages)) (inc i) i))

(defn back [i]
  (if (pos? i) (dec i) i))

(defn nav-link [page i]
  [:li.page-item
   {:class (when (= i @page) "active")}
   [:a.page-link
    {:on-click #(reset! page i)}
    [:span i]]])

(defn pager [pages page]
  (when (> pages 1)
    (into
     [:nav.text-xs-center>ul.pagination.pagination-lg]
     (concat
      [[:li.page-item
        {:class (when (= @page 0) "disabled")}
        [:a.page-link
         {:on-click #(swap! page back pages)}
         [:span "<"]]]]
      (map (partial nav-link page) (range pages))
      [[:li.page-item
        {:class (when (= @page (dec pages)) "disabled")}
        [:a.page-link
         {:on-click #(swap! page foward pages)}
         [:span ">"]]]]))))

(defn rgb->str [[r g b] mask]
  (str "rgba(" r "," g "," b "," mask ")"))

(defn set-background! [style [c1 c2 c3]]
  (set! (.-background style)
        (str "linear-gradient("
             (rgb->str c3 0.8) ","
             (rgb->str c3 0.9) ","
             (rgb->str c1 1) ")")))

(defn ^:export image-panel-did-mount [thumb-link]
  (fn [div]
    (.getColors
     (js/AlbumColors. thumb-link)
     (fn [colors]
       (-> div reagent/dom-node .-style (set-background! colors))))))

(defn render-image-panel [link]
  (fn []
    [:img.image.panel.panel-default
     {:on-click #(session/remove! :modal)
      :src link}]))

(defn image-panel [thumb-link link]
  (reagent/create-class {:render (render-image-panel link)
                         :component-did-mount (image-panel-did-mount thumb-link)}))

(defn image-modal [thumb-link link]
  (fn []
    [:div
     [image-panel thumb-link link]
     [:div.modal-backdrop.fade.in]]))

(defn thumb-link [{:keys [owner name]}]
  [:div.col-sm-4
   [:img
    {:src (str js/context "/gallery/" owner "/" name)
     :on-click #(session/put! :modal
                              (image-modal
                               (str js/context "/gallery/" owner "/" name)
                               (str js/context "/gallery/" owner "/"
                                    (s/replace name #"thumb-" ""))))}]
   (when (= (session/get :identity) owner)
     [:div.text-xs-center>div.btn.btn-danger
      {:on-click #(delete-image-button owner name)}
      [:i.fa.fa-times]])])

(defn gallery [links]
  [:div.text-xs-center
   (for [row (partition-all 3 links)]
     ^{:key row}
     [:div.row
      (for [link row]
        ^{:key link}
        [thumb-link link])])])

(defn gallery-page []
  (let [page (atom 0)]
    (fn []
      [:div.container
       (when-let [thumbnail-links (partition-links (session/get :thumbnail-links))]
         [:div.row>div.col-md-12
          [pager (count thumbnail-links) page]
          [gallery (thumbnail-links @page)]])])))

(defn fetch-gallery-thumbs! [owner]
  (ajax/GET (str "/list-thumbnails/" owner)
            {:handler #(session/put! :thumbnail-links %)}))


