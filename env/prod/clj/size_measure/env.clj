(ns size-measure.env
  (:require [clojure.tools.logging :as log]))

(def defaults
  {:init
   (fn []
     (log/info "\n-=[size-measure started successfully]=-"))
   :stop
   (fn []
     (log/info "\n-=[size-measure has shut down successfully]=-"))
   :middleware identity})
