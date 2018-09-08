(ns button.ui.components.charts
  (:require [cljsjs.d3]
            [reagent.core :as r]))

(defn tile-chart []
  (r/create-class
   {:reagent-render (fn []
                      [:div {:id "tilechart"}])
    :component-did-mount (fn []

                           ;; (prn (js-keys js/d3))
                           
                           (let [width 500
                                 height 500
                                 data  {:name :rect
                                         :tokens [{:id 0 :weight 50}
                                                  {:id 1 :weight 50}
                                                  {:id 2 :weight 50}
                                                  {:id 3 :weight 50}
                                                  {:id 4 :weight 50}]}
                                 treemap (-> js/d3
                                          .treemap
                                          (.size (clj->js [width height]))
                                          (.padding 15) 
                                          (.round true)
                                          ;; .tile (-> js/d3 (.treemapBinary))
                                          )
                                 ]
                             
                             (-> js/d3
                                 (.select (str "#tilechart"))
                                 (.append "svg")
                                 (.attr "class" (str "tilechart"))
                                 (.attr "width" width)
                                 (.attr "height" height)
                                 )

                             ))}))
