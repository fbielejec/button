(ns button.ui.components.charts
  (:require [cljsjs.d3]
            [district.ui.graphql.subs :as gql]
            [re-frame.core :as re-frame]
            [reagent.core :as r]))

(defn tile-chart-component [children]
  (r/create-class
   {:reagent-render (fn [children]
                      [:div {:id "tilechart"}])
    :component-did-mount (fn []
                           (let [width 500
                                 height 500
                                 data (clj->js {:name "rect"
                                                :children children})
                                 treemap (-> js/d3
                                             .treemap
                                             (.size (clj->js [width height]))
                                             (.padding 15)
                                             (.round true)
                                             (.tile (-> js/d3 .-treemapBinary)))
                                 tree (-> js/d3
                                          (.hierarchy data)
                                          (.sum (fn [d]
                                                  (aget d "value"))))]
                             (treemap tree)
                             (-> js/d3
                                 (.select (str "#tilechart"))
                                 (.selectAll ".node")
                                 (.data (-> tree .leaves))
                                 (.enter)
                                 (.append "div")
                                 (.attr "class" "tilechart")
                                 (.attr "class" "node")
                                 (.style "background" "#ffffff")
                                 (.style "left" (fn [d]
                                                  (str (aget d "x0") "px")))
                                 (.style "top" (fn [d]
                                                 (str (aget d "y0") "px")))
                                 (.style "width" (fn [d]
                                                   (str (- (aget d "x1")
                                                           (aget d "x0")) "px")))
                                 (.style "height" (fn [d]
                                                    (str (- (aget d "y1")
                                                            (aget d "y0")) "px"))))))}))

(defn tile-chart []
  (let [response (re-frame/subscribe [::gql/query {:queries [[:all-tokens [:button-token/owner-address
                                                                           :button-token/weight
                                                                           :button-token/image-hash]]]}])]
    (when-not (:graphql/loading? @response)
      (let [children (reduce
                      (fn [children {:keys [:button-token/owner-address :button-token/weight :button-token/image-hash] }]
                        (conj children {:id image-hash
                                        :value weight
                                        :owner owner-address}))
                      []
                      (-> @response :all-tokens))]
        [tile-chart-component children]))))
