(ns button.ui.events
  (:require [re-frame.core :as re-frame]))

(re-frame/reg-event-db
 ::current-block-number
 (fn [db [_ number]]
   (assoc-in db [:button-app :current-block-number] number)))

(re-frame/reg-event-db
 ::last-pressed-block-number
 (fn [db [_ number]]
   (assoc-in db [:button-app :last-pressed-block-number] number)))
