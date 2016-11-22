(ns picture-gallery.routes.services
  (:require [picture-gallery.routes.services.auth :as auth]
            [picture-gallery.routes.services.upload :as upload]
            [picture-gallery.routes.services.gallery :as gallery]
            [compojure.api.meta :refer [restructure-param]]
            [buddy.auth.accessrules :refer [restrict]]
            [buddy.auth :refer [authenticated?]] 
            [ring.util.http-response :refer :all]
            [compojure.api.sweet :refer :all]
            [compojure.api.upload :refer :all]            
            [schema.core :as s]))

(s/defschema UserRegistration
  {:id String
   :pass String
   :pass-confirm String})

(s/defschema Result
  {:result s/Keyword
   (s/optional-key :message) String})

(s/defschema Gallery
  {:owner               String
   :name                String
   (s/optional-key :rk) s/Num})

(s/defschema Images
  {:image-name String
   :thumb-name String})


(defn access-error [_ _]
  (unauthorized {:error "unauthorized"}))

(defn wrap-restricted [handler rule]
  (restrict handler {:handler  rule
                     :on-error access-error}))

(defmethod restructure-param :auth-rules
  [_ rule acc]
  (update-in acc [:middleware] conj [wrap-restricted rule]))

(defmethod restructure-param :current-user
  [_ binding acc]
  (update-in acc [:letks] into [binding `(:identity ~'+compojure-api-request+)]))

(defapi service-routes
  {:swagger {:ui "/swagger-ui"
             :spec "/swagger.json"
             :data {:info {:version "1.0.0"
                           :title "Picture Gallery API"
                           :description "Public Services"}}}}
  
  (POST "/register" req
        :return Result
        :body [user UserRegistration]
        :summary "register a new user"
        (auth/register! req user))
  (POST "/login" req
        :header-params [authorization :- String]
        :summary "login the user and create a session"
        :return Result
        (auth/login! req authorization))
  (POST "/logout" []
        :summary "remove user session"
        :return Result
        (auth/logout!))
  (GET "/gallery/:owner/:name" []
       :summary "display user image"
       :path-params [name :- String]
       (gallery/get-image name))
  (GET "/list-thumbnails/:owner" []
       :path-params [owner :- String]
       :summary "list thumbnails for images in the gallery"
       :return [Gallery]
       (gallery/list-thumbnails owner))
  (GET "/list-galleries" []
       :summary "lists a thumbnail for each user"
       :return [Gallery]
       (gallery/list-galleries))

  (context "/private-api" []
           
           :auth-rules authenticated?
           
           :tags ["private"]
           
           (POST "/delete-image" req
                 :body [images Images]
                 :summary "delete the specified file from the database"
                 :return Result
                 (gallery/delete-image! (:identity req) (:thumb-name images)  (:image-name images)))

           (POST "/upload" req
                 :multipart-params [file :- TempFileUpload]
                 :middleware [wrap-multipart-params]
                 :summary "handles image upload"
                 :return Result
                 (upload/save-image! (:identity req) file))

           (POST "/delete-account" req
                 (auth/delete-account! (:identity req)))))
