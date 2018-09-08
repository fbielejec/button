(ns button.ui.components.charts
  (:require [cljsjs.d3]
            [reagent.core :as r]))

(defn tile-chart []
  (r/create-class
   {:reagent-render (fn []
                      [:div {:id "tilechart"}])
    :component-did-mount (fn []

                           ;; (prn (aget js/d3 "treemapBinary"))

                           (let [width 500
                                 height 500
                                 data  {:name "rect"
                                        :children [{:id 0 :value 50}
                                                   {:id 1 :value 50}
                                                   {:id 2 :value 50}
                                                   {:id 3 :value 50}
                                                   {:id 4 :value 50}]}
                                 treemap (-> js/d3
                                             .treemap
                                             (.size (clj->js [width height]))
                                             (.padding 15)
                                             (.round true)
                                             (.tile (-> js/d3 .-treemapBinary)))

                                 tree (-> js/d3
                                          (.hierarchy (clj->js data)))]

                             (-> tree
                                 (.sum (fn [d]
                                         (prn "DATA "(aget d "value"))
                                         (aget d "value"))))

                             (prn (-> tree .leaves))

                             (-> js/d3
                                 (.select (str "#tilechart"))
                                 ;; (.append "svg")
                                 (.attr "class" (str "tilechart"))
                                 (.attr "width" width)
                                 (.attr "height" height)
                                 (.selectAll ".node")
                                 (.data (-> tree .leaves))
                                 (.enter)
                                 (.append "div")
                                 (.attr "class" "node")
                                 (.style "background" "#ffffff")

                                 (.attr "title" (fn [d] (aget d "data" "id")))

                                 (.style "left" (fn [d]
                                                  (str (aget d "x0") "px")))

                                 (.style "top" (fn [d]
                                                 (prn "y0" (aget d "y0") )
                                                 (str (aget d "y0") "px")))

                                 (.style "width" (fn [d]
                                                   (str (- (aget d "x1")
                                                           (aget d "x0")) "px")))

                                 (.style "height" (fn [d]
                                                    (str (- (aget d "y1")
                                                            (aget d "y0")) "px")))


                                 )

                             ))}))
