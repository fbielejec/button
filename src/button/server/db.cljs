(ns button.server.db
  (:require [district.server.config :refer [config]]
              ;; [district.server.db :as db]
              ;; [district.server.db.column-types :refer [address not-nil default-nil default-zero default-false sha3-hash primary-key]]
              ;; [district.server.db.honeysql-extensions]
              ;; [honeysql.core :as sql]
              ;; [honeysql.helpers :refer [merge-where merge-order-by merge-left-join defhelper]]
              ;; [medley.core :as medley]
              [mount.core :as mount :refer [defstate]]
              ;; [print.foo :refer [look] :include-macros true]
              ;; [taoensso.timbre :as logging :refer-macros [info warn error]]

              ))

#_(def ipfs-hash (sql/call :char (sql/inline 46)))

#_(def registry-entries-columns
    [[:button-token/token-id address primary-key not-nil]
     [:button-token/number :unsigned :integer not-nil]
     [:button-token/owner-address address not-nil]
     [:button-token/we :unsigned :BIG :INT not-nil]
     [:reg-entry/created-on :unsigned :integer not-nil]
     [:reg-entry/challenge-period-end :unsigned :integer not-nil]
     [:challenge/challenger address default-nil]
     [:challenge/created-on :unsigned :integer default-nil]
     [:challenge/voting-token address default-nil]
     [:challenge/reward-pool :unsigned :BIG :INT default-nil]
     [:challenge/meta-hash ipfs-hash default-nil]
     [:challenge/comment :varchar default-nil]
     [:challenge/commit-period-end :unsigned :integer default-nil]
     [:challenge/reveal-period-end :unsigned :integer default-nil]
     [:challenge/votes-for :BIG :INT default-nil]
     [:challenge/votes-against :BIG :INT default-nil]
     [:challenge/claimed-reward-on :unsigned :integer default-nil]])

(defn start [args]
  (atom {:last-press-block-number 0
         :tokens []}))

(defn stop []
  ::stopped)

(defstate ^{:on-reload :noop} button-db
  :start (start (merge (:button/db @config)
                       (:button/db (mount/args))))
  :stop (stop))

(defn get-last-press-block-number []
  (:last-press-block-number @@button-db))

(defn get-all-tokens []
  (:tokens @@button-db))
