; Copyright 2009 Howard M. Lewis Ship
;
; Licensed under the Apache License, Version 2.0 (the "License");
; you may not use this file except in compliance with the License.
; You may obtain a copy of the License at
;
;   http://www.apache.org/licenses/LICENSE-2.0
;
; Unless required by applicable law or agreed to in writing, software
; distributed under the License is distributed on an "AS IS" BASIS,
; WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
; implied. See the License for the specific language governing permissions
; and limitations under the License.

(ns com.howardlewisship.cascade.test-utils
  (:use
    app1.fragments
    clojure.contrib.test-is
    clojure.contrib.pprint
    clojure.contrib.duck-streams
    com.howardlewisship.cascade.internal.utils))


(deftest classpath-resource-does-not-exist
  (is (= (find-classpath-resource "does/not/exist.txt") nil)))

(deftest classpath-resource-exists
  (let [path "com/howardlewisship/cascade/internal/resource.txt"]
    (is (= (slurp* (find-classpath-resource path))
      (slurp (str "src/test/resources/" path))))))

(deftest namespace-relative
  (is (= (slurp* (find-classpath-resource 'namespace-relative "ns.txt"))
    (slurp "src/test/resources/com/howardlewisship/cascade/test_utils/ns.txt"))))

(deftest read-single-form-with-constant
  (is (= (read-single-form "5") 5)))

(deftest read-single-form-with-simple-form
  (is (= (read-single-form "(5 7)") '(5 7))))

(defmacro unreachable
  []
  '(throw (RuntimeException. "This code should not be reachable.")))

(deftest read-single-form-but-too-many-forms
  (try
    (read-single-form "(first-form) (second-form)")
    (unreachable)
    (catch RuntimeException ex (is (= (.getMessage ex) "Input expression '(first-form) (second-form)' should contain only a single form.")))))

(defn test-re-partition
  [re s expected]
  (let [actual (map (fn [[s mr]] [s (and mr (.group mr))]) (re-partition-matches re s))]
    (is (= actual expected))))

(deftest re-partition-no-matches
  (test-re-partition
    #"\d+" "No digits here"
    [["No digits here" nil]]))

(deftest re-partition-matches-with-matches
  (test-re-partition #"\d+" "There are 100 matches."
    [["There are " "100"]
     [" matches." nil]]))


(deftest re-partition-with-consecutive-matches
  (test-re-partition #"\{.*?\}" "one{two}{three}four"
    [["one" "{two}"]
     ["" "{three}"]
     ["four" nil]]))

(deftest transform-string-using-re-map
  (let [text-fn (fn [text] (if (zero? (.length text)) nil (str \[ text \])))
        match-fn (fn [result] (.toUpperCase (.group result)))
        mapped (re-map #"\w+" "At last, the future awaits!" text-fn match-fn)]
    (is (= mapped ["AT" "[ ]" "LAST" "[, ]" "THE" "[ ]" "FUTURE" "[ ]" "AWAITS" "[!]"]))))