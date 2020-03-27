(ns process.core
  (:use [clojure.process])
  (:use [clojure.test])
  (:use [leiningen.cucumber])
  (:import [cucumber.api.cli Main]))

; (deftest run-process
;   (. cucumber.api.cli.Main (main (into-array ["--format" "pretty" "--glue" "test" "test/features"]))))
