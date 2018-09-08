(ns button.ui.components.block-counter
  (:require [re-frame.core :as re-frame]
            [button.ui.subs :as subs]))

(defn component []
  (fn [data]
    (let [last-pressed-block-number (:last-press-block-number data)
          current-block-number @(re-frame/subscribe [::subs/current-block-number])]
      (js/console.log "lpbn:" last-pressed-block-number)
      (js/console.log "current block number:" current-block-number)
      [:div.block-counter
       [:span
        (- current-block-number
           last-pressed-block-number)]])))
