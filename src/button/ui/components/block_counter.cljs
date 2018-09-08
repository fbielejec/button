(ns button.ui.components.block-counter
  (:require [re-frame.core :as re-frame]
            [button.ui.subs :as subs]))

(defn component []
  [:div.block-counter
   [:span @(re-frame/subscribe [::subs/count-since-last-pressed-block])]])
