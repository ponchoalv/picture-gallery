(ns picture-gallery.components.common)

(defn modal [header body footer submit-function remove-function]
  [:div
   [:div.modal-dialog
    [:div.modal-content
     [:div.modal-header [:h3 header]]
     [:div.modal-body
      {:on-key-down #(case (.-which %)
                       13 (submit-function)
                       27 (remove-function)
                       nil)
       :tab-index 1}
      body]
     [:div.modal-footer
      [:div.bootstrap-dialog-footer
       footer]]]]
   [:div.modal-backdrop.fade.in]])

(defn input [type id placeholder fields auto-focus]
  [:input.form-control.input-lg
   {:type type
    :placeholder placeholder
    :value (id @fields)
    :on-change #(swap! fields assoc id (-> % .-target .-value))
    :id id
    :auto-focus auto-focus}])

(defn form-input [type label id placeholder fields auto-focus optional?]
  [:div.form-group
   [:label label]
   (if optional?
     [input type id placeholder fields auto-focus]
     [:div.input-group
      [input type id placeholder fields auto-focus]
      [:span.input-group-addon
       "*"]])])

(defn text-input [label id placeholder fields & [optional?]]
  (form-input :text label id placeholder fields false optional?))

(defn text-input-focus [label id placeholder fields & [optional?]]
  (form-input :text label id placeholder fields true optional? ))

(defn password-input [label id placeholder fields & [optional?]]
  (form-input :password label id placeholder fields false optional?))
