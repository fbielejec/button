(ns button.server.deployer
  (:require
   [cljs-web3.core :as web3]
   [cljs-web3.eth :as web3-eth]
   [district.cljs-utils :refer [rand-str]]
   [district.server.config :refer [config]]
   [district.server.smart-contracts :refer [contract-event-in-tx contract-address deploy-smart-contract! write-smart-contracts!]]
   [district.server.web3 :refer [web3]]
   ;; [button.server.contract.button-token :as button-token]
   [mount.core :as mount :refer [defstate]]))

(defn deploy-button-token! [default-opts]
  (deploy-smart-contract! :buton-token (merge default-opts {:gas 2000000})))

(defn deploy [{:keys [:write? :initial-registry-params :transfer-dank-token-to-accounts
                      :use-n-account-as-deposit-collector :use-n-account-as-cut-collector]
               :as deploy-opts}]
  (let [accounts (web3-eth/accounts @web3)
        deploy-opts (merge {:from (last accounts)}
                           deploy-opts)]

    (deploy-button-token! deploy-opts)

    (when write?
      (write-smart-contracts!))))

(defstate ^{:on-reload :noop} deployer
  :start (deploy (merge (:deployer @config)
                        (:deployer (mount/args)))))
