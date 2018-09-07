(ns button.ui.home.page
  (:require
   [district.ui.component.page :refer [page]]
   [button.ui.components.button :as button]
   [re-frame.core :refer [subscribe dispatch]]
   [reagent.core :as r]))

(defmethod page :route/home []
  (fn []
    [button/button]))
