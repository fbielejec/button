(ns button.server.syncer
  (:require [bignumber.core :as bn]
            [button.server.contract.button-token :as button-token]
            [button.server.db :as button-db]
            [camel-snake-kebab.core :as cs]
            [cljs-web3.core :as web3]
            [cljs-web3.eth :as web3-eth]
            [district.server.smart-contracts
             :as
             smart-contracts
             :refer
             [replay-past-events]]
            [district.server.web3 :refer [web3]]
            [district.web3-utils :as web3-utils]
            [memefactory.server.macros :refer [try-catch]]
            [taoensso.timbre :as log]))

(defn- last-block-number []
  (web3-eth/block-number @web3))
 
(defmulti process-event (fn [contract-type ev] [contract-type (:event-type ev)])) 

(defmethod process-event [:contract/button-token :contract-minted-event]
  [_ ev]
  (try-catch
   (let [{:keys [:token-id :number :weight :last-press-block-number :owner-address :image-hash]} ev]

     (button-db/add-token {:button-token/token-id token-id
                           :button-token/number number
                           :button-token/owner-address owner-address
                           :button-token/weight weight
                           :button-token/image-hash image-hash})
     (button-db/set-last-press-block-number last-press-block-number)))) 

(defmethod process-event :default
  [k ev]
  (log/warn (str "No process-event defined for processing " k ev) ))

(defn dispatch-event [contract-type err {:keys [args event] :as a}]
  (let [event-type (cond 
                     (:event-type args) (cs/->kebab-case-keyword (web3-utils/bytes32->str (:event-type args)))
                     event      (cs/->kebab-case-keyword event))
        ev (-> args
               ;; (assoc : (:address a))
               (assoc :event-type event-type)
               (update :timestamp bn/number))]
    (log/info (str contract-type " " event-type) {:ev ev :a a} ::dispatch-event) 
    (process-event contract-type ev)))

(defn start [{:keys [:initial-param-query] :as opts}]
  (when-not (web3/connected? @web3)
    (throw (js/Error. "Can't connect to Ethereum node")))
  (let [last-block-number (last-block-number)
        watchers [{:watcher (partial button-token/token-minted-event [:button-token]) 
                   :on-event #(dispatch-event :contract/button-token %1 %2)}]] 
    (concat

     ;; Replay every past events (from block 0 to (dec last-block-number))
     (when (pos? last-block-number)
       (->> watchers
            (map (fn [{:keys [watcher on-event]}]
                   (-> (apply watcher [{} {:from-block 0 :to-block (dec last-block-number)}])
                       (replay-past-events on-event))))
            doall))

     ;; Filters that will watch for last event and dispatch
     (->> watchers
          (map (fn [{:keys [watcher on-event]}]
                 (apply watcher [{} "latest" on-event])))
          doall))))

(defn stop [syncer]
  (doseq [filter (remove nil? @syncer)]
    (web3-eth/stop-watching! filter (fn [err]))))
