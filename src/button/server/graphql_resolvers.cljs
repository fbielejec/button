(ns button.server.graphql-resolvers
  (:require [bignumber.core :as bn]
            ;; [cljs-time.core :as t]
            ;; [cljs-web3.core :as web3-core]
            ;; [cljs-web3.eth :as web3-eth]
            [cljs.nodejs :as nodejs]
            [clojure.string :as str]

            [district.graphql-utils :as graphql-utils]
            ;; [district.server.config :refer [config]]
            ;; [district.server.db :as db]
            ;; [district.server.smart-contracts :as smart-contracts]
            [district.server.web3 :as web3]
            ;; [honeysql.core :as sql]
            ;; [honeysql.helpers :as sqlh]
          
            [taoensso.timbre :as log]
            [button.server.db :as button-db]))

(def enum graphql-utils/kw->gql-name)

(defn all-tokens-query-resolver [_ _]
  (button-db/get-all-tokens))

(def resolvers-map
  {:Query {:meme all-tokens-query-resolver}})
