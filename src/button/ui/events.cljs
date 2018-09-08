(ns button.ui.events
  (:require [re-frame.core :as re-frame]
            [cljs-web3.eth :as web3-eth]
            [cljs-web3.core :as web3]
            [district0x.re-frame.interval-fx]
            [district.ui.web3.queries :as web3-queries]
            [district.ui.web3.events :as web3-events]
            [district.ui.web3-tx.events :as web3-tx-events]
            [district.ui.smart-contracts.events :as contract-events]
            [district.ui.web3-accounts.queries :as account-queries]
            [district.ui.smart-contracts.queries :as contract-queries]))

(re-frame/reg-event-db
 ::current-block-number
 (fn [db [_ number]]
   (assoc-in db [:button-app :current-block-number] number)))

(re-frame/reg-event-db
 ::last-pressed-block-number
 (fn [db [_ number]]
   (assoc-in db [:button-app :last-pressed-block-number] number)))

(re-frame/reg-event-db
 ::blockchain-error
 (fn [db [_ error]]
   (assoc-in db [:button-app :error] error)))

(re-frame/reg-event-fx
 ::fetch-current-block-number
 (fn [{:keys [db]} [_ _]]
   {:web3/call {:web3 (web3-queries/web3 db)
                :fns [{:fn cljs-web3.eth/block-number
                       :args []
                       :on-success [::current-block-number]
                       :on-error [::blockchain-error]}]}}))

(re-frame/reg-event-fx
 ::start-poll-for-block-number
 (fn [{:keys [db]} [_ _]]
   (js/console.log "Starting to poll for the lastest block number.")
   {:dispatch-interval
    {:dispatch [::fetch-current-block-number]
     :id :some-id-goes-here
     :ms 3000}}))

(re-frame/reg-event-fx
 ::forward-to-timer
 (fn []
   {:register :my-forwarder
    :events #{::web3-events/web3-created}
    :dispatch-to [::start-poll-for-block-number]}))

(re-frame/reg-event-fx
 ::button-pressed
 (fn [{:keys [db]} [_ _]]
   (let [account (account-queries/active-account db)]
     {:dispatch [::web3-tx-events/send-tx
                 {:instance (contract-queries/instance db :button)
                  :fn :press
                  :args []
                  :tx-opts {:from account :gas 4500000}
                  :on-tx-hash [::tx-hash]
                  :on-tx-hash-n [[::tx-hash]]
                  :on-tx-hash-error [::tx-hash-error]
                  :on-tx-hash-error-n [[::tx-hash-error]]
                  :on-tx-success [::tx-success]
                  :on-tx-success-n [[::tx-success]]
                  :on-tx-error [::tx-error]
                  :on-tx-error-n [[::tx-error]]}]})))

(re-frame/reg-event-fx
 ::upload-image
 (fn [_ [_ file token-id]]
   (.log js/console "Uploading " file)
   {:ipfs/call {:func "add"
                :args [file]
                :on-success [::set-image-hash token-id]
                :on-error ::error}}))

(re-frame/reg-event-fx
 ::set-image-hash
 (fn [{:keys [db]} [_ token-id {:keys [Name Hash Size]}]]
   (let [account #_(account-queries/active-account db) "0x4c3f13898913f15f12f902d6480178484063a6fb"]
     (.log js/console "Calling " (contract-queries/instance db :button) (clj->js [:set-image-hash token-id Hash {:from account :gas 4500000}]))
     {:dispatch [::web3-tx-events/send-tx
                 {:instance (contract-queries/instance db :button)
                  :fn :set-image-hash
                  :args [token-id Hash]
                  :tx-opts {:from account :gas 4500000}
                  :on-tx-hash [::tx-hash]
                  :on-tx-hash-n [[::tx-hash]]
                  :on-tx-hash-error [::tx-hash-error]
                  :on-tx-hash-error-n [[::tx-hash-error]]
                  :on-tx-success [::tx-success]
                  :on-tx-success-n [[::tx-success]]
                  :on-tx-error [::tx-error]
                  :on-tx-error-n [[::tx-error]]}]})))

