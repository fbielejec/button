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
            [button.shared.macros :refer [try-catch]]
            [taoensso.timbre :as log]
            [mount.core :as mount :refer [defstate]]
            [district.server.config :refer [config]]))

(declare start)
(declare stop)

(defstate ^{:on-reload :noop} syncer
  :start (start (merge (:syncer @config)
                       (:syncer (mount/args))))
  :stop (stop syncer))

(defn- last-block-number []
  (web3-eth/block-number @web3))
 
(defmulti process-event (fn [contract-type ev] [contract-type (:event-type ev)])) 

(defmethod process-event [:contract/button :press]
  [_ ev]
  (try-catch
   (let [{:keys [:token-id]} ev
         token-id (bn/number token-id)
         token (merge {:button-token/token-id token-id}
                      (-> (zipmap [:button-token/number
                                   :button-token/weight
                                   :button-token/value
                                   :button-token/image-hash
                                   :button-token/owner-address]
                                  (button-token/load-token token-id))
                          (update :button-token/number bn/number)
                          (update :button-token/weight bn/number)
                          (update :button-token/value bn/number)))]
     (log/info "WE HAVE " (button-token/load-token token-id))
     (log/info "Inserting  token !!" token)
     (button-db/add-token token)
     (button-db/set-last-press-block-number (:button-token/number token)))))

(defmethod process-event [:contract/button :image-hash-set]
  [_ ev]
  (try-catch 
   (log/info (str "Updating " ev (bn/number (:token-id ev)) (web3/to-ascii (:image-hash ev))))  
   (button-db/update-image-hash (bn/number (:token-id ev)) (web3/to-ascii (:image-hash ev))))) 

(defmethod process-event :default
  [k ev]
  (log/warn (str "No process-event defined for processing " k ev) ))

(defn dispatch-event [contract-type err {:keys [args event] :as a}]
  (println "Got event" a)
  (let [event-type (cond 
                     (:event-type args) (cs/->kebab-case-keyword (web3-utils/bytes32->str (:event-type args)))
                     event      (cs/->kebab-case-keyword event))
        ev (-> args
               ;; (assoc : (:address a))
               (assoc :event-type event-type)
               (update :timestamp bn/number))]
    (println (str contract-type " " event-type) {:ev ev :a a} ::dispatch-event) 
    (process-event contract-type ev)))

(defn start [{:keys [:initial-param-query] :as opts}]
  (when-not (web3/connected? @web3)
    (throw (js/Error. "Can't connect to Ethereum node")))
  (let [last-block-number (last-block-number)
        watchers [{:watcher (partial button-token/token-minted-event) 
                   :on-event #(dispatch-event :contract/button %1 %2)}
                  {:watcher (partial button-token/token-hash-updated-event) 
                   :on-event #(dispatch-event :contract/button %1 %2)}]]  
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
