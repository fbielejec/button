(ns button.ui.components.button
  (:require [reagent.core :as r]
            [button.ui.events :as events]
            [re-frame.core :as re-frame]))

(defn component []
  [:div.button-center
   [:div.select-button
    {:on-click (fn [e]
                 (re-frame/dispatch [::events/button-pressed]))}
    "DO NOT PRESS"]])
