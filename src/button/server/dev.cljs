(ns button.server.dev
  (:require [bignumber.core :as bn]
            [camel-snake-kebab.core :as cs :include-macros true]
            [cljs-time.core :as t]
            [cljs-web3.core :as web3]
            [cljs-web3.eth :as web3-eth]
            [cljs-web3.evm :as web3-evm]
            [cljs.nodejs :as nodejs]
            [cljs.pprint :as pprint]
            [clojure.pprint :refer [print-table]]
            [clojure.string :as str]
            [district.graphql-utils :as graphql-utils]
            [district.server.config :refer [config]]
            [district.server.db :as db]
            [district.server.graphql :as graphql]
            [district.server.graphql.utils :as utils]
            [district.server.logging :refer [logging]]
            [district.server.middleware.logging :refer [logging-middlewares]]
            [district.server.smart-contracts]
            [district.server.web3 :refer [web3]]
            [district.server.web3-watcher]
            [goog.date.Date]
            [graphql-query.core :refer [graphql-query]]          
            [mount.core :as mount]))

(nodejs/enable-util-print!)

(def graphql-module (nodejs/require "graphql"))
(def parse-graphql (aget graphql-module "parse"))
(def visit (aget graphql-module "visit"))

(defn on-jsload []
  #_(graphql/restart {:schema (utils/build-schema graphql-schema
                                                resolvers-map
                                                {:kw->gql-name graphql-utils/kw->gql-name
                                                 :gql-name->kw graphql-utils/gql-name->kw})
                    :field-resolver (utils/build-default-field-resolver graphql-utils/gql-name->kw)}))

(defn deploy-to-mainnet []
  (mount/stop #'district.server.web3/web3
              #'district.server.smart-contracts/smart-contracts)
  (mount/start-with-args (merge
                           (mount/args)
                           {:web3 {:port 8545}
                            :deployer {:write? true
                                       :gas-price (web3/to-wei 4 :gwei)}})
                         #'district.server.web3/web3
                         #'district.server.smart-contracts/smart-contracts))

(defn redeploy []
  (mount/stop)
  (-> (mount/with-args
        (merge
          (mount/args)
          {:deployer {:write? true}}))
    (mount/start)
    pprint/pprint))

(defn resync []
  #_(mount/stop #'button.server.db/button-db
              #'button.server.syncer/syncer)
  #_(-> (mount/start #'button.server.db/button-db
                   #'button.server.syncer/syncer)
      pprint/pprint))

(defn -main [& _]
  #_(-> (mount/with-args
        {:config {:default {:logging {:level "info"
                                      :console? true}
                            :graphql {:port 6300
                                      :middlewares [logging-middlewares]
                                      :schema (utils/build-schema graphql-schema
                                                                  resolvers-map
                                                                  {:kw->gql-name graphql-utils/kw->gql-name
                                                                   :gql-name->kw graphql-utils/gql-name->kw})
                                      :field-resolver (utils/build-default-field-resolver graphql-utils/gql-name->kw)
                                      :path "/graphql"
                                      :graphiql true}
                            :web3 {:port 8549}
                            :generator {}
                            :deployer {}
                            :smart-contracts {:contracts-var #'button.shared.smart-contracts/smart-contracts
                                              :print-gas-usage? true
                                              :auto-mining? true}}}})
      (mount/except [#'button.server.deployer/deployer
                     #'button.server.generator/generator])
      (mount/start)
      pprint/pprint))

(set! *main-cli-fn* -main)
