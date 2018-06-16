(ns d3-tuts.bars
  (:require-macros [d3-tuts.macros :refer [attrs]] :reload)
  )


(defonce app-state (atom {:data
                          (into #queue [](for [i (range 20)]
                                           {:key i :val (rand-int 30)}))}))

(def height 200)
(def width 800)

(defn posicBars
  ([svg]
   (posicBars svg 0))
  ([svg offset]
   (let [{:keys [xscale yscale]} @app-state
         bw (.bandwidth xscale)]
     (-> svg
         (attrs
          {
           "x" #(+ (xscale %2) (* offset bw))
           "y" #(- height (yscale (:val %)))
           "width" bw
           "height" #(yscale (:val %))
           "fill" #(str "rgb(0,0," (int (* 10 (:val %))) ")")
           })))))

(defn posicText
  ([svg]
   (posicText svg 0) )
  ([svg offset]
   (let [{:keys [data xscale yscale]} @app-state
         bw (.bandwidth xscale)]
     (-> svg
         (attrs
          {
           "x" #(+ (* (+ offset %2) (/ width (count data))) (/ bw 2))
           "y" #(- height -13 (yscale (:val %)))
           "font-family" "sans-serif"
           "fill" "white"
           "text-anchor" "middle"
           "font-size" "11px"
           })))))

(defn setScales []
  (let [data (:data @app-state)]
    (swap! app-state
           assoc
           :xscale
           (->  js/d3
                (.scaleBand)
                (.domain  (clj->js (range (count data))))
                (.range #js [0 width])
                (.paddingInner 0.05)
                (.align .5)))
    (swap! app-state
           assoc
           :yscale
           (-> js/d3
               (.scaleLinear)
               (.domain #js [0 (:val (apply max-key :val data))])
               (.rangeRound #js [0 height])))))

(defn append-svg []
  (-> js/d3
      (.select "#bars")
      (.append "svg")
      (attrs {
              "height" height
              "width" width})))

(defn draw-bars []
  (let [{:keys [svg data]} @app-state
        objs (-> svg
                 (.selectAll "rect")
                 (.data (into-array data) #(:key %)))]
    (-> objs
        (.enter)
        (.append "rect")
        (posicBars))))

(defn draw-text []
  (let [{:keys [svg data]} @app-state
        objs (-> svg
                 (.selectAll "text")
                 (.data (into-array data) #(:key %)))]
  (-> objs
      (.enter)
      (.append "text")
      (.text #(:val %))
      (posicText))))

(defn update-bars []
  (let [{:keys [svg data]} @app-state
        objs (-> svg
                 (.selectAll "rect")
                 (.data (into-array data) #(:key %)))
        bw (.bandwidth (:xscale @app-state))]
    (-> objs
        (.enter)
        (.append "rect")
        (posicBars 2)
        (.merge objs)
        (.transition)
        (.duration 500)
        (posicBars))
    (-> objs
        (.exit)
        (.transition)
        (.duration 500)
        (attrs {"x" (- bw)})
        (.remove))))

(defn update-text []
  (let [{:keys [svg data]} @app-state
        objs (-> svg
                 (.selectAll "text")
                 (.data (into-array data) #(:key %)))
        bw (.bandwidth (:xscale @app-state))
        ]
    (-> objs
        (.exit)
        (.transition)
        (.duration 500)
        (attrs {"x" (- bw)})
        (.remove))
    (-> objs
        (.enter)
        (.append "text")
        (.text #(:val %))
        (posicText 1)
        (.merge objs)
        (.transition)
        (.duration 500)
        (posicText))))

(defn addData []
  (let [d (:data @app-state)
        top (:key (apply max-key :key d))]
    (swap!
     app-state
     assoc
     :data
     (conj d {:key (inc top) :val (rand-int 30)}))))

(defn delData []
  (let [d (:data @app-state)
        top (:key (apply min-key :key d))]
    (swap!
     app-state
     assoc
     :data
     (pop d))))

(defn updateD3 [k]
  (this-as el
    (if (= (.-id el) "add")
      (addData)
      (delData)
      ))
  (setScales)
  (update-bars )
  (update-text ))

(defn main []
  (let [svg (append-svg) {data :data xs :xscale ys :yscale } @app-state]
    (swap! app-state assoc :svg svg)
    (setScales)
    (draw-bars)
    (draw-text)
    (-> js/d3
        (.selectAll "p.bar")
        (.on "click" updateD3))
    )
  )
