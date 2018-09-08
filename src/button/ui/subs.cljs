(ns button.ui.subs
  (:require [re-frame.core :as re-frame]))

(re-frame/reg-sub
 ::count-since-last-pressed-block
 (fn [db]
   (let [last-pressed-block-number (get-in db [:button-app :last-pressed-block-number])
         current-block-number (get-in db [:button-app :current-block-number])]
     (- current-block-number last-pressed-block-number))))
