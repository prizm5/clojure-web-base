(defproject nimbus-api "0.1.0"
  :description ""
  :url ""
  :min-lein-version "2.0.0"
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [com.climate/clj-newrelic "0.1.1"]
                 [ring/ring-json "0.3.1"]
                 [com.ashafa/clutch "0.4.0-RC1"]
                 [jumblerg/ring.middleware.cors "1.0.1"]
                 [ring "1.3.1"]
                 [org.clojure/tools.logging "0.3.1"]
                 [enlive "1.1.5"]
                 [ring-mock "0.1.5"]
                 [clj-time "0.8.0"]
                 [midje "1.6.3"]
                 [org.clojure/tools.nrepl "0.2.6"]
                 [environ "1.0.0"]
                 [ring/ring-json "0.3.1"]
                 [com.cemerick/friend "0.2.1" :exclusions [org.clojure/core.cache]]
                 [org.clojure/tools.trace "0.7.8"]
                 [hiccup "1.0.5"]
                 [clj-jwt "0.0.11"]
                 [friend-oauth2 "0.1.1"] 
                 [compojure "1.1.9"]
                 [com.cemerick/drawbridge "0.0.6"]
                 [sonian/carica "1.1.0"]
                 [peridot "0.3.0"]
                 [org.clojure/data.json "0.2.5"]
                 [log4j/log4j "1.2.17" :exclusions [javax.mail/mail
                                                    javax.jms/jms
                                                    com.sun.jmdk/jmxtools
                                                    com.sun.jmx/jmxri]]
                 [clj-time "0.8.0"]
                 [org.clojure/core.async "0.1.346.0-17112a-alpha"]
                 ]
  :plugins [[lein-environ "1.0.0"]
            [lein-ring "0.8.13"]
            [lein-midje "3.1.3"]
            [cider/cider-nrepl "0.8.1"]]
  :java-agents [#_[com.newrelic.agent.java/newrelic-agent "3.11.0"]]
  :profiles {:dev {:env {:isdev true}
                   :dependencies [#_[javax.servlet/servlet-api "2.5"]
                                  [ring-mock "0.1.5"]]}}
  :ring {:handler nimbus-api.handler/app
         :uberwar-name "nimbus-api.war"
         :init nimbus-api.handler/init}
  :main nimbus-api.handler/-main
  :aot :all)
