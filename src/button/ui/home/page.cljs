(ns button.ui.home.page
  (:require
   [district.ui.component.page :refer [page]]
   [button.ui.components.button :as button]
   [button.ui.components.block-counter :as counter]
   [button.ui.components.charts :as charts]
   [re-frame.core :refer [subscribe dispatch]]
   [reagent.core :as r]))

(defmethod page :route/home []
  (fn []
    [:div
     [:div.outermost-layout
      [:div.main-layout
       [button/component]
       [counter/component]]]
     [charts/tile-chart]]))
