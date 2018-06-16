(ns d3-tuts.core
  (:require [cljsjs.d3]
            [d3-tuts.bars :refer [main] :rename {main main-bars}]
            [d3-tuts.plot :refer [main] :rename {main main-plot}]
            )
  )

(enable-console-print!)


;; define your app data so that it doesn't get over-written on reload



(defn remove-svg []
  (-> js/d3
      (.selectAll "#bars svg")
      (.remove)
      )
  (-> js/d3
      (.selectAll "#plot svg")
      (.remove)
      )
  )

(defn ^:export main []
  (main-bars)
  (main-plot)
  )

(defn on-js-reload []
  (remove-svg)
  (main))


(main-bars)
(main-plot)
