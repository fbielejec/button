(ns button.server.db
  (:require [district.server.config :refer [config]]
            [mount.core :as mount :refer [defstate]]
            [taoensso.timbre :as log]))

(defn start [args]
  (let [watch-fn (fn [_key _ref _old-state new-state]
                   (log/info "Database updated" new-state))
        atomic-db (atom {:last-press-block-number 0
                         :tokens []})]
    (add-watch atomic-db :persist-watcher watch-fn)
    atomic-db))

(defn stop []
  ::stopped)

(defstate ^{:on-reload :noop} button-db
  :start (start (merge (:button/db @config)
                       (:button/db (mount/args))))
  :stop (stop))

(defn set-last-press-block-number [number]
  (swap! @button-db assoc :last-press-block-number number))

(defn update-image-hash [t-id hash]
(swap! @button-db (fn [db]
                      (update db :tokens
                              (fn [tokens] 
                                (mapv (fn [{:keys [:button-token/token-id] :as t}]
                                        (if (= t-id token-id)
                                          (assoc t :button-token/image-hash hash)
                                          t))
                                      tokens))))))



(defn get-last-press-block-number []
  (:last-press-block-number @@button-db))

(defn add-token [token]
  (swap! @button-db update-in [:tokens] conj token))

(defn get-all-tokens []
  (:tokens @@button-db))
