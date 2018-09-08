(ns button.ui.home.page
  (:require
   [district.ui.component.page :refer [page]]
   [district.ui.graphql.subs :as gql]
   [button.ui.components.button :as button]
   [button.ui.components.block-counter :as counter]
   [button.ui.components.charts :as charts]
   [button.ui.events :as events]
   [re-frame.core :refer [subscribe dispatch]]
   [reagent.core :as r]))

(defmethod page :route/home []
  (let [last-pressed-block-number (subscribe
                                   [::gql/query {:queries
                                                 [[:last-press-block-number]]}
                                    {:refetch-on #{::events/button-press-success}}])]
    (fn []
      (println "last pressed:" @last-pressed-block-number)
      [:div
       [:div.outermost-layout
        [:div.main-layout
         [button/component]
         [counter/component @last-pressed-block-number]]]
       [charts/tile-chart]])))
