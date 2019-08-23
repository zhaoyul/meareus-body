(ns size-measure.env
  (:require
    [selmer.parser :as parser]
    [clojure.tools.logging :as log]
    [size-measure.dev-middleware :refer [wrap-dev]]))

(def defaults
  {:init
   (fn []
     (parser/cache-off!)
     (log/info "\n-=[size-measure started successfully using the development profile]=-"))
   :stop
   (fn []
     (log/info "\n-=[size-measure has shut down successfully]=-"))
   :middleware wrap-dev})
