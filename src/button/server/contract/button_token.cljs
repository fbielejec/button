(ns button.server.contract.button-token
  (:require
    [camel-snake-kebab.core :as cs :include-macros true]
    ;; [cljs-solidity-sha3.core :refer [solidity-sha3]]
    [district.server.smart-contracts :refer [contract-call]]))

(defn token-minted-event [& args]
  (apply contract-call :button :Press args))

(defn token-hash-updated-event [& args]
  (apply contract-call :button :ImageHashSet args))

(defn load-token [token-id]
  (contract-call :button :load-token token-id))
