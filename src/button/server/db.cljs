(ns button.server.db
  (:require [mount.core :as mount]))

(defstate db :start (atom {})
  :stop ::stopped)
