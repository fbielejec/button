(ns button.ui.components.charts
  (:require [cljsjs.d3]
            [district.ui.web3-accounts.subs :as accounts-subs]
            [district.ui.graphql.subs :as gql]
            [re-frame.core :as re-frame]
            [reagent.core :as r]
            [district.ui.component.form.input :as inputs]
            [button.ui.events :as events]))

(defn chart-tile [token-id]
  [:div {:style {:background-color :red}}
   [:input {:type :file
            :id "file"
            :on-change (fn [e]
                         (let [files (-> e .-target .-files)
                               f (aget files 0)]
                           (let [url-reader (js/FileReader.)
                                 ab-reader (js/FileReader.)]
                             (set! (.-onload url-reader) (fn [e]
                                                           (let [img-data (-> e .-target .-result)]
                                                             ;; set img-data in src
                                                             #_(.log js/console "Setting " img-data))))
                             (.readAsDataURL url-reader f)
                             (re-frame/dispatch [::events/upload-image f token-id]))))}]])

;; d3.scaleSequential(d3.interpolateRainbow).domain(d3.extent(data.map(d=>d.x)))

(defn tile-chart-component [{:keys [:children :active-account]}]
  (r/create-class
   {:reagent-render (fn [{:keys [:children :active-account]}]
                      [:div.title-chart {:id "tilechart"}])
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
                                 (.selectAll ".chart-node")
                                 (.data (-> tree .leaves))
                                 (.enter)
                                 (.append "div")
                                 (.attr "class" "tilechart")
                                 (.attr "class" "chart-node")
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
