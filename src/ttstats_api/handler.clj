(ns ttstats-api.handler
  (:import com.mchange.v2.c3p0.ComboPooledDataSource)
  (:use compojure.core)
  (:use cheshire.core)
  (:use ring.util.response)
  (:require [compojure.handler :as handler]
            [ring.middleware.json :as middleware]
            [clojure.java.jdbc :as sql]
            [compojure.route :as route]))


(def db-config
  {:classname "org.h2.Driver"
   :subprotocol "h2"
   :subname "mem:songs"
   :user ""
   :password ""})

(defn pool
  [config]
  (let [cpds (doto (ComboPooledDataSource.)
               (.setDriverClass (:classname config))
               (.setJdbcUrl (str "jdbc:" (:subprotocol config) ":" (:subname config)))
               (.setUser (:user config))
               (.setPassword (:password config))
               (.setMaxPoolSize 1)
               (.setMinPoolSize 1)
               (.setInitialPoolSize 1))]
    {:datasource cpds}))

(def pooled-db (delay (pool db-config)))

(defn db-connection [] @pooled-db)

(sql/with-connection (db-connection)
;  (sql/drop-table :songs) ; no need to do that for in-memory databases
  (sql/create-table :songs [:id "varchar(256)" "primary key"]
                               [:title :varchar]
                               [:time :varchar]
                               [:listeners :int]
                               [:lames :int]
                               [:hearts :int]
                               [:awesomes :int]
                               [:artist :varchar]))

(defn uuid [] (str (java.util.UUID/randomUUID)))

(defn get-all-songs []
  (response
    (sql/with-connection (db-connection)
      (sql/with-query-results results
        ["select * from songs"]
        (into [] results)))))

(defn get-song [id]
  (sql/with-connection (db-connection)
    (sql/with-query-results results
      ["select * from songs where id = ?" id]
      (cond
        (empty? results) {:status 404}
        :else (response (first results))))))

(defn create-new-song [s]
  (let [id (uuid)]
    (sql/with-connection (db-connection)
      (let [song (assoc s "id" id)]
        (sql/insert-record :songs song)))
    (get-song id)))

(defn update-song [id s]
    (sql/with-connection (db-connection)
      (let [song (assoc s "id" id)]
        (sql/update-values :songs ["id=?" id] song)))
    (get-song id))

(defn delete-song [id]
  (sql/with-connection (db-connection)
    (sql/delete-rows :songs ["id=?" id]))
  {:status 204})

(defroutes app-routes
  (context "/songs" [] (defroutes songs-routes
    (GET  "/" [] (get-all-songs))
    (POST "/" {body :body} (create-new-song body))
    (context "/:id" [id] (defroutes song-routes
      (GET    "/" [] (get-song id))
      (PUT    "/" {body :body} (update-song id body))
      (DELETE "/" [] (delete-song id))))))
  (route/not-found "Not Found"))

(def app
  (-> (handler/api app-routes)
      (middleware/wrap-json-body)
      (middleware/wrap-json-response)))
