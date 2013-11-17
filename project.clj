(defproject ttstats-api "0.1.0-SNAPSHOT"
  :description "REST API for the ttstats song resource"
  :url "http://ttstats-api.herokuapp.com/songs"
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [compojure "1.1.6"]
                 [ring/ring-json "0.1.2"]
                 [c3p0/c3p0 "0.9.1.2"]
                 [org.clojure/java.jdbc "0.2.3"]
                 [com.h2database/h2 "1.3.168"]
                 [ring/ring-servlet "1.2.0-RC1"]
                 [cheshire "4.0.3"]]
  :min-lein-version "2.0.0"
  :plugins [[lein-ring "0.8.8"]]
  :ring {:handler ttstats-api.handler/app}
  :profiles
  {:dev {:dependencies [[javax.servlet/servlet-api "2.5"]
                        [ring-mock "0.1.5"]]}})
