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
            [button.shared.smart-contracts]             
            [button.server.db]
            [button.server.syncer]
            [button.server.deployer]
            [district.server.web3 :refer [web3]]
            [district.server.web3-watcher]
            [goog.date.Date]
            [graphql-query.core :refer [graphql-query]]          
            [mount.core :as mount]
            [button.shared.graphql-schema :refer [graphql-schema]]
            [button.server.graphql-resolvers :refer [resolvers-map]]
            [cljs.spec.alpha :as s]
            [cljs.spec.gen.alpha :as sg]
            [clojure.test.check.generators])) 

(nodejs/enable-util-print!)

(def graphql-module (nodejs/require "graphql"))
(def parse-graphql (aget graphql-module "parse"))
(def visit (aget graphql-module "visit"))

(defn on-jsload []
  (graphql/restart {:schema (utils/build-schema graphql-schema
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
  (mount/stop #'button.server.db/button-db
              #'button.server.syncer/syncer)
  (-> (mount/start #'button.server.db/button-db
                   #'button.server.syncer/syncer)
      pprint/pprint))

(defn -main [& _]
  (-> (mount/with-args 
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
                            :smart-contracts {:contracts-var #'button.shared.smart-contracts/smart-contracts
                                              :print-gas-usage? true
                                              :auto-mining? false}}}})
      (mount/except [#'button.server.deployer/deployer]) 
      (mount/start)
      pprint/pprint))

(set! *main-cli-fn* -main)

(s/def :button-token/token-id string?)
(s/def :button-token/number pos-int?)
(s/def :button-token/owner-address (s/with-gen (s/and string?
                                                      #(str/starts-with? % "0x"))
                                     #(s/gen #{"0x5ed8cee6b63b1c6afce3ad7c92f4fd7e1b8fad9f"})))

(s/def :button-token/weight (s/and pos-int?
                                   #(< % 10)))

(s/def :button-token/image-hash string?)

(s/def ::button-token (s/keys :req [:button-token/token-id
                                    :button-token/number
                                    :button-token/owner-address
                                    :button-token/weight
                                    :button-token/image-hash]))


(defn gen-dummy-data []
  (swap! @button.server.db/button-db assoc :tokens (sg/generate (s/gen (s/coll-of ::button-token)))))

#_(district.server.smart-contracts/contract :button)
#_(district.server.smart-contracts/contract-call (district.server.smart-contracts/instance :button)
                                                 :press {:from (first (web3-eth/accounts @web3))
                                                         :gas 4000000})

#_(district.server.smart-contracts/contract-call :button :Transfer {} "latest" (fn [& args] (println "GOT AN EVENT()" args)))

