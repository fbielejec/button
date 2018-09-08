(ns button.ui.subs
  (:require [re-frame.core :as re-frame]))

(re-frame/reg-sub
 ::current-block-number
 (fn [db]
   (get-in db [:button-app :current-block-number])))
