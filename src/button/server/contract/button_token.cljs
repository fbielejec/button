(ns button.server.contract.button-token
  (:require
    [camel-snake-kebab.core :as cs :include-macros true]
    ;; [cljs-solidity-sha3.core :refer [solidity-sha3]]
    [district.server.smart-contracts :refer [contract-call]]))

(defn token-minted-event [contract-key & args]
  (apply contract-call contract-key :ContractMintedEvent args))
