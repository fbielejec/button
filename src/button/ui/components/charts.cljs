(ns button.ui.components.charts
  (:require [cljsjs.d3]
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
                                                  (aget d "value")))
                                          (.sort (fn [d1 d2]
                                                   (- (aget d2 "value")
                                                      (aget d1 "value")))))]
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
