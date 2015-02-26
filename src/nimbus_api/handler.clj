(ns nimbus-api.handler
  (:require [nimbus-api.api :as api]
            [nimbus-api.configs :as configs]
            [nimbus-api.read.users :as users]
            [nimbus-api.read-db :as read-db]
            [nimbus-api.event-store :as event]
            [compojure.core :refer :all]
            [compojure.handler :as handler]
            [compojure.route :as route]
            [clojure.tools.nrepl.server :as nrepl-server]
            [cider.nrepl :refer (cider-nrepl-handler)]
            [cemerick.drawbridge :as dw]
            [clojure.tools.trace :refer [trace]]
            [environ.core :refer [env]]
            [ring.middleware.json :as middleware]
            [hiccup.core :refer :all]
            [ring.adapter.jetty :as ring]
            [ring.middleware.cors :refer [wrap-cors]]
            [ring.middleware.params :as param] 
            [clj-http.client :as client]
            [ring.middleware.keyword-params :as keyword-param] 
            [ring.middleware.nested-params :as nested-param] 
            [ring.util.response :refer [resource-response response redirect content-type]])
  (:gen-class))

(defroutes app-routes 
  (let [nrepl-handler (dw/ring-handler)] 
    (ANY "/repl" request (nrepl-handler request)))
  (route/not-found {:status 404 :headers {} :body "Not found"}))

(def app 
  (handler/site (-> 
                 (routes #'api/api-routes #'app-routes)
                 (wrap-cors #".*localhost.*" #".*mybluemix.net$" #".*bluemix.net$")
                 (param/wrap-params) 
                 (keyword-param/wrap-keyword-params) 
                 (nested-param/wrap-nested-params)
                 (middleware/wrap-json-body {:keywords? true})
                 (middleware/wrap-json-params {:keywords? true})
                 (middleware/wrap-json-response))))

(defn start-nrepl-server []
  (nrepl-server/start-server :port 7999 :handler cider-nrepl-handler)
  (trace "Started nrepl server at port 7999"))

(defn start-jetty [ip port]
  (ring/run-jetty app {:port port :ip ip}))

(defn init
  ([] (init (trace "event-db" configs/event-db) configs/read-db (env :isdev) true))
  ([events-name auth-name start-nrepl async?]
     (read-db/initialize-db auth-name (trace "cloudant-url" configs/cloudant-url))
     ;; we memoize the init function to be able to
     ;; "restart" it when the db connection breaks
     (event/initialize-db events-name configs/cloudant-url async?)  
     (@event/init-memo)
     (if start-nrepl (start-nrepl-server))))

(defn -main
  ([] (-main 8082))
  ([port] (-main port "0.0.0.0")) 
  ([port ip & args] 
     (let [port (Integer. port)]
       (trace (str "Started http server started at: " ip ":" port))
       (start-jetty ip port)
       (init)
       )))
