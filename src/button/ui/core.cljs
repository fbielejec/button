(ns button.ui.core
  (:require
   [cljs.spec.alpha :as s]
   [clojure.string :as str]
   [district.ui.component.router :refer [router]]
   #_[district.ui.graphql]
   [district.ui.now]
   [district.ui.reagent-render]
   [district.ui.router-google-analytics]
   [district.ui.router]
   [district.ui.smart-contracts]
   [district.ui.web3-account-balances]
   [district.ui.web3-accounts]
   [district.ui.web3-balances]
   [district.ui.web3-sync-now]
   [district.ui.web3-tx-log]
   [district.ui.web3-tx]
   [district.ui.web3]
   [district.ui.window-size]
   [button.ui.home.page]
   [button.shared.routes :refer [routes]]
   [mount.core :as mount]
   [re-frisk.core :refer [enable-re-frisk!]]))

(def debug? ^boolean js/goog.DEBUG)

(defn dev-setup []
  (when debug?
    (enable-console-print!)
    (enable-re-frisk!)))

#_(def skipped-contracts [:ds-guard :param-change-registry-db :meme-registry-db :minime-token-factory])

(defn ^:export init []
  (s/check-asserts debug?)
  (dev-setup)
  (-> (mount/with-args
        (merge {:web3 {:url "http://localhost:8549"}
                #_#_:smart-contracts {:contracts (apply dissoc smart-contracts skipped-contracts)}
                #_#_:web3-balances {:contracts (select-keys smart-contracts [:DANK])}
                #_#_:web3-account-balances {:for-contracts [:ETH]}
                #_#_:web3-tx-log {:open-on-tx-hash? true
                                  :tx-costs-currencies [:USD]}
                :reagent-render {:id "app"
                                 :component-var #'router}
                :router {:routes routes
                         :default-route :route/home}
                #_#_:router-google-analytics {:enabled? (not debug?)}
                #_#_:graphql {:schema graphql-schema
                              :url "http://localhost:6300/graphql"}
                #_#_:ipfs {:host "http://127.0.0.1:5001" :endpoint "/api/v0"}}))
      (mount/start)))
