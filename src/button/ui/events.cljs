(ns button.ui.events
  (:require [re-frame.core :as re-frame]
            [cljs-web3.eth :as web3-eth]
            [cljs-web3.core :as web3]
            [district0x.re-frame.interval-fx]
            [district.ui.web3.queries :as web3-queries]
            [district.ui.web3.events :as web3-events]))

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
   (js/console.log "Web3 is:" (web3-queries/web3 db))
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
