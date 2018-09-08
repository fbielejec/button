(ns button.ui.components.charts
  (:require [cljsjs.d3]
            [district.ui.web3-accounts.subs :as accounts-subs]
            [district.ui.graphql.subs :as gql]
            [re-frame.core :as re-frame]
            [reagent.core :as r]
            [district.ui.component.form.input :as inputs]
            [button.ui.events :as events]))

(defn on-change [token-id]
  (fn [e]
    (let [files (-> e .-target .-files)
          f (aget files 0)]
      (let [url-reader (js/FileReader.)
            ab-reader (js/FileReader.)]
        (set! (.-onload url-reader) (fn [e]
                                      (let [img-data (-> e .-target .-result)]
                                        ;; set img-data in src
                                        #_(.log js/console "Setting " img-data))))
        (.readAsDataURL url-reader f)
        (re-frame/dispatch [::events/upload-image f token-id])))))

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

                             (treemap tree)
                             (-> js/d3
                                 (.select "#tilechart")
                                 (.selectAll ".chart-node")
                                 (.data (-> tree .leaves))
                                 (.enter)
                                 (.append "div")
                                 (.attr "class" (fn [d]
                                                  (cond-> "chart-node"
                                                    (= active-account (aget d "data" "owner"))
                                                    (str " user-owned-node"))))
                                 (.style "background" "#ffffff")
                                 (.style "outline-width" "3px")
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
                                                            (aget d "y0")) "px")))


                                 (.append "label")
                                 (.attr "for" (fn [d]
                                                (aget d "data" "id")))

                                 (.append "img")
                                 (.attr "src"  (fn [d]
                                                 (str "https://gateway.ipfs.io/ipfs/" (aget d "data" "image-hash"))))
                                 (.style "width" "100%")
                                 (.style "height" "100%"))

                             (-> js/d3
                                 (.selectAll ".user-owned-node" )
                                 (.append "input")
                                 (.attr "id" (fn [d]
                                               (aget d "data" "id")))
                                 (.attr "type" "file")

                                 (.attr "on-change" (fn [d]

                                                      ;; (prn "TOKEN ID" (aget d "data" "id"))

                                                      (on-change (aget d "data" "id"))


                                                      ))


                                 )


                             ))}))

;; <div class="image-upload">
;;     <label for="file-input">
;;         <img src="placeholder.jpg"/>
;;     </label>

;;     <input id="file-input" type="file"/>
;; </div>

(defn tile-chart []
  (let [active-account (re-frame/subscribe [::accounts-subs/active-account])
        response (re-frame/subscribe [::gql/query {:queries [[:all-tokens [:button-token/token-id
                                                                           :button-token/owner-address
                                                                           :button-token/weight
                                                                           :button-token/image-hash]]]}])]
    (when-not (:graphql/loading? @response)
      (let [children (reduce
                      (fn [children {:keys [:button-token/token-id :button-token/owner-address :button-token/weight :button-token/image-hash] }]
                        (conj children {:id token-id
                                        :value weight
                                        :owner owner-address
                                        :image-hash image-hash}))
                      []
                      (-> @response :all-tokens))]
        [tile-chart-component {:children children
                               :active-account @active-account}]))))
