(ns button.ui.components.charts
  (:require [cljsjs.d3]
            [district.ui.web3-accounts.subs :as accounts-subs]
            [district.ui.graphql.subs :as gql]
            [re-frame.core :as re-frame]
            [reagent.core :as r]))

;; d3.scaleSequential(d3.interpolateRainbow).domain(d3.extent(data.map(d=>d.x)))

(defn tile-chart-component [{:keys [:children :active-account]}]
  (r/create-class
   {:reagent-render (fn [{:keys [:children :active-account]}]
                      [:div {:id "tilechart"}])
    :component-did-mount (fn []
                           (let [width 500
                                 height 500
                                 color-scale (-> js/d3
                                                 (.scaleSequential (-> js/d3 .-interpolateRainbow))
                                                 (.domain (-> js/d3 (.extent (clj->js (map :value children))))))
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
                                                  (aget d "value")))
                                          (.sort (fn [d1 d2]
                                                   (- (aget d2 "value")
                                                      (aget d1 "value")))))]

                             (prn (map :value children))
                             
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
                                 (.style "background-color" (fn [d]
                                                              
                                                              (prn (aget d "data" "value"))

                                                              (if (= active-account (aget d "data" "owner"))
                                                                "#66CC66"
                                                                (color-scale
                                                                 (aget d "data" "value")))))
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
  (let [active-account (re-frame/subscribe [::accounts-subs/active-account])
        response (re-frame/subscribe [::gql/query {:queries [[:all-tokens [:button-token/owner-address
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
        [tile-chart-component {:children children
                               :active-account @active-account}]))))
