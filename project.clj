(defproject button "0.0.1"
  :description "playground for developing button"
  :url "https://github.com/fbielejec/button"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}

  :dependencies [[mount "0.1.11"]
                 [cljsjs/d3 "4.12.0-0"]
                 [district0x/district-ui-reagent-render "1.0.1"]
                 [org.clojure/clojurescript "1.9.946"]
                 [re-frame "0.10.5"]
                 [cljsjs/react-flexbox-grid "1.0.0-0"]
                 [district0x/district-web3-utils "1.0.2"]
                 [district0x/district-server-middleware-logging "1.0.0"]
                 [district0x/district-server-smart-contracts "1.0.8"]
                 [district0x/district-server-web3-watcher "1.0.2"]
                 [district0x/district-ui-web3 "1.0.1"]
                 [district0x/district-ui-web3-account-balances "1.0.2"]
                 [district0x/district-ui-web3-accounts "1.0.5"]
                 [district0x/district-ui-web3-balances "1.0.2"]
                 [district0x/district-ui-router "1.0.4"]
                 [district0x/district-ui-graphql "1.0.6"]
                 [district0x/district-ui-now "1.0.2"]
                 [district0x/district-ui-router-google-analytics "1.0.1"]
                 [district0x/district-ui-smart-contracts "1.0.5"]
                 [district0x/district-ui-web3-sync-now "1.0.3"]
                 [district0x/district-ui-web3-tx-log "1.0.2"]
                 [district0x/district-ui-web3-tx "1.0.9"]
                 [district0x/district-server-web3 "1.0.1"]
                 [district0x/district-ui-window-size "1.0.1"]
                 [district0x/re-frame-interval-fx "1.0.2"]
                 [district0x/district-server-logging "1.0.2"]
                 [district0x/district-server-config "1.0.1"]
                 [district0x/district-server-db "1.0.3"]
                 [district0x/district-server-graphql "1.0.15"]
                 [org.clojure/test.check "0.10.0-alpha3"]]

  :exclusions [[express-graphql]
               [org.clojure/clojure]
               [org.clojure/clojurescript]]

  :plugins [[lein-solc "1.0.1-1"]
            [lein-garden "0.3.0"]
            [lein-less "1.7.5"]]

  :less {:source-paths ["resources/public/css"]
         :target-path "resources/public/css"}

  :npm {:dependencies [[chalk "2.3.0"]
                       [express-graphql "./resources/libs/express-graphql-0.6.13.tgz"]
                       [graphql-tools "3.0.1"]
                       [graphql "0.13.1"]
                       [express "4.15.3"]
                       [cors "2.8.4"]
                       [graphql-fields "1.0.2"]
                       [solc "0.4.20"]
                       [source-map-support "0.5.3"]
                       [ws "4.0.0"]

                       ;; this isn't required directly but 0.6.1 is broken and
                       ;; district0x/district-server-web3 needs [ganache-core "2.0.2"]   who also needs "ethereumjs-wallet": "~0.6.0"
                       ;; https://github.com/ethereumjs/ethereumjs-wallet/issues/64
                       [ethereumjs-wallet "0.6.0"]]}

  :solc {:src-path "resources/public/contracts/src"
         :build-path "resources/public/contracts/build"
         :solc-err-only true
         :wc true
         :contracts :all}

  :source-paths ["src" "test"]

  :repl-options {:init-ns ^:skip-aot user}

  :figwheel {:css-dirs ["resources/public/css"]}

  :clean-targets ^{:protect false} ["resources/public/js/compiled"]

  :profiles {:dev {:source-paths ["dev"]
                   :resource-paths ["resources"]
                   :dependencies [[com.cemerick/piggieback "0.2.2"]
                                  [binaryage/devtools "0.9.7"]
                                  [figwheel-sidecar "0.5.14"]
                                  [org.clojure/clojure "1.8.0"]
                                  [org.clojure/tools.nrepl "0.2.13"]
                                  [re-frisk "0.5.3"]]
                   :plugins [[lein-npm "0.6.2"]
                             [lein-figwheel "0.5.14"]
                             [lein-cljsbuild "1.1.7"]]}}

  :cljsbuild {:builds [{:id "dev-server"
                        :source-paths ["src/button/server" "src/button/shared"]
                        :figwheel {:on-jsload "button.server.dev/on-jsload"}
                        :compiler {:main "button.server.dev"
                                   :output-to "dev-server/button.js"
                                   :output-dir "dev-server"
                                   :target :nodejs
                                   :optimizations :none
                                   :closure-defines {goog.DEBUG true}
                                   :source-map true}}
                       {:id "dev-ui"
                        :source-paths ["src"]
                        :figwheel {:on-jsload "district.ui.reagent-render/rerender"}
                        :compiler {:main "button.ui.core"
                                   :output-to "resources/public/js/compiled/app.js"
                                   :output-dir "resources/public/js/compiled/out"
                                   :asset-path "js/compiled/out"
                                   :source-map-timestamp true
                                   :closure-defines {goog.DEBUG true}
                                   :external-config {:devtools/config {:features-to-install :all}}}}]})
