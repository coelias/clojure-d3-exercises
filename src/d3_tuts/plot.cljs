(ns d3-tuts.plot
  (:require-macros [d3-tuts.macros :refer [attrs]] :reload))

(defonce app-state (atom {:data
                          (into #queue [] (for [i (range 20)]
                                            {:key i :val (rand-int 30)}))}))

(def points 300)
(def height 600)
(def padding 40)
(def width 600)

(defn generate-data []
  (let [x (* 100 (+ 3 (rand-int 5)))
        y (* 100 (+ 3 (rand-int 5)))]
    (swap! app-state
           assoc
           :data
           (repeatedly
            points
            #(vector (rand-int x) (rand-int y))))))

(defn append-svg []
  (-> js/d3
      (.select "#plot")
      (.append "svg")
      (attrs {"height" height
              "width" width})))

(defn create-axis []
  (let [{:keys [xscale yscale]} @app-state]
    (swap! app-state
           assoc
           :xaxis
           (-> js/d3
               (.axisBottom)
               (.scale xscale)
               (.ticks 5)))
    (swap! app-state
           assoc
           :yaxis
           (-> js/d3
               (.axisLeft)
               (.scale yscale)
               (.ticks 5)))))

(defn setScales []
  (let [data (:data @app-state)]
    (swap! app-state
           assoc
           :xscale
           (->  js/d3
                (.scaleLinear)
                (.domain #js [0 (first (apply max-key first data))])
                (.rangeRound #js [padding (- width padding)])))
    (swap! app-state
           assoc
           :yscale
           (-> js/d3
               (.scaleLinear)
               (.domain #js [0 (second (apply max-key second data))])
               (.rangeRound #js [(- height padding) padding])))))

(defn updateScales []
  (let [{:keys [data xscale yscale]} @app-state]
    (.domain xscale #js [0 (first (apply max-key first data))])
    (.domain yscale #js [0 (second (apply max-key second data))])
  ))

(defn draw-plot []
  (let [{:keys [svg data xscale yscale xaxis yaxis]} @app-state]
    (-> svg
        (.append "g")
        (.attr "clip-path" "url(#chart-area)")
        (.selectAll "circle")
        (.data (into-array data))
        (.enter)
        (.append "circle")
        (attrs{
               "cx" #(xscale (first %))
               "cy" #(yscale (second %))
               "r" 2
               }))
    (-> svg
        (.append "g")
        (attrs {"class" "y axis"
                "transform" (str "translate(" padding ",0)")
                })
        (.call yaxis))
    (-> svg
        (.append "g")
        (attrs {"class" "x axis"
                "transform" (str "translate(0," (- height padding ) ")")
                })
        (.call xaxis))))

(defn updateAxis []
  (let [{:keys [svg xaxis yaxis]} @app-state]
    (-> svg
        (.select ".x.axis")
        (.transition)
        (.duration 1000)
        (.call xaxis))
    (-> svg
        (.select ".y.axis")
        (.transition)
        (.duration 1000)
        (.call yaxis))
  ))

(defn updateD3 []
  (generate-data)
  (updateScales)
  (updateAxis)
  (let [{:keys [svg data xscale yscale xaxis yaxis]} @app-state]
    (-> svg
        (.selectAll "circle")
        (.data (into-array data))
        (.transition)
        (.duration 1000)
        (.on "start" #(this-as th
                        (-> js/d3
                            (.select th)
                            (attrs {"fill" "magenta"
                                    "r" 7
                                    }))))
        (attrs{
               "cx" #(xscale (first %))
               "cy" #(yscale (second %))
               })
        (.on "end" #(this-as th
                        (-> js/d3
                            (.select th)
                            (.transition)
                            (.duration 1000)
                            (attrs {"fill" "black"
                                    "r" 2
                                    })))))))


(defn main []
  (let [svg (append-svg) {xs :xscale ys :yscale} @app-state]
    (swap! app-state assoc :svg svg)
    (generate-data)
    (setScales)
    (create-axis)
    (draw-plot)
    (-> svg
        (.append "clipPath")
        (.attr "id" "chart-area")
        (.append "rect")
        (attrs {"x" padding
                "y" padding
                "width" (- width (* 2 padding) )
                "height" (- height (* 2 padding) )}))
    (-> js/d3
        (.select "#shuffle")
        (.on "click" updateD3))))
