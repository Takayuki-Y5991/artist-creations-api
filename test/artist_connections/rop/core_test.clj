(ns artist-connections.rop.core-test
  (:require [clojure.test :refer :all]
            [artist-connections.rop.core :refer :all]))

;; Helper functions for tests
(defn add-one [x]
  (ok (inc x)))

(defn multiply-by-two [x]
  (ok (* 2 x)))

(defn fail-if-negative [x]
  (if (neg? x)
    (error "Negative number")
    (ok x)))

(defn merge-results [results]
  (ok (apply + results)))

;; Test basic ok/error functions
(deftest ok-error-test
  (testing "ok function creates success result"
    (is (= {:ok 42} (ok 42)))
    (is (= {:ok nil} (ok nil)))
    (is (= {:ok [1 2 3]} (ok [1 2 3]))))

  (testing "error function creates failure result"
    (is (= {:error "Something went wrong"} (error "Something went wrong")))
    (is (= {:error nil} (error nil)))))

;; Test bind operator >>=
(deftest bind-operator-test
  (testing ">>= with success"
    (is (= {:ok 2} (>>= (ok 1) add-one)))
    (is (= {:ok 4} (>>= (ok 2) multiply-by-two))))

  (testing ">>= with failure"
    (let [error-result (error "Failed")]
      (is (= error-result (>>= error-result add-one)))))

  (testing ">>= chain operations"
    (is (= {:ok 4} (-> (ok 1)
                       (>>= add-one)
                       (>>= multiply-by-two))))

    (is (= {:error "Negative number"}
           (-> (ok 1)
               (>>= add-one)
               (>>= (fn [x] (fail-if-negative -2)))
               (>>= multiply-by-two))))))

;; Test railway-> macro
(deftest railway-macro-test
  (testing "railway-> with success chain"
    (is (= {:ok 4} (railway-> (ok 1)
                              add-one
                              multiply-by-two))))

  (testing "railway-> with failure"
    (is (= {:error "Negative number"}
           (railway-> (ok -1)
                      fail-if-negative
                      add-one))))

  (testing "railway-> stops at first failure"
    (is (= {:error "First error"}
           (railway-> (error "First error")
                      add-one
                      multiply-by-two)))))

;; Test parallel execution
(deftest parallel-test
  (testing "parallel with all success"
    (let [parallel-op (parallel add-one multiply-by-two)]
      (is (= {:ok [2 2]} (parallel-op 1)))))

  (testing "parallel with some failures"
    (let [parallel-op (parallel add-one fail-if-negative)]
      (is (= {:error "Negative number"} (parallel-op -1)))))

  (testing "parallel with empty steps"
    (let [parallel-op (parallel)]
      (is (= {:ok []} (parallel-op 1))))))

;; Test parallel-async execution
(deftest parallel-async-test
  (testing "parallel-async with all success"
    (let [slow-add (fn [x] (Thread/sleep 100) (ok (inc x)))
          slow-multiply (fn [x] (Thread/sleep 100) (ok (* 2 x)))
          parallel-op (parallel-async slow-add slow-multiply)]
      (is (= {:ok [2 2]} (parallel-op 1)))))

  (testing "parallel-async with some failures"
    (let [slow-add (fn [x] (Thread/sleep 100) (ok (inc x)))
          slow-fail (fn [x] (Thread/sleep 100) (error "Async error"))
          parallel-op (parallel-async slow-add slow-fail)]
      (is (= {:error "Async error"} (parallel-op 1)))))

  (deftest parallel-async-test
    (testing "parallel execution correctness"
      (let [slow-op (fn [x] (Thread/sleep 50) (ok (inc x)))
            parallel-op (parallel-async slow-op slow-op)]
        (is (= {:ok [2 2]} (parallel-op 1))
            "Should correctly process parallel operations")))

    (testing "parallel-async performance"
      (let [compute-op (fn [x]
                         ;; Thread/sleep の代わりに実際の計算処理
                         (dotimes [_ 100000] (Math/sin x))
                         (ok x))
            parallel-op (parallel-async compute-op compute-op)

            ;; 逐次実行
            sequential-start (System/currentTimeMillis)
            _ (doall [(compute-op 1) (compute-op 1)])
            sequential-time (- (System/currentTimeMillis) sequential-start)

            ;; 並列実行
            parallel-start (System/currentTimeMillis)
            parallel-result (parallel-op 1)
            parallel-time (- (System/currentTimeMillis) parallel-start)]

        ;; 結果の正確性を確認
        (is (= {:ok [1 1]} parallel-result)
            "Should return correct results")

        ;; パフォーマンスの検証
        (is (< parallel-time sequential-time)
            (format "Parallel execution (%dms) should be faster than sequential execution (%dms)"
                    parallel-time
                    sequential-time))))))



;; Test recover function
(deftest recover-test
  (testing "recover with success"
    (let [op (recover add-one (fn [_] (ok 42)))]
      (is (= {:ok 2} (op 1)))))

  (testing "recover with failure"
    (let [op (recover fail-if-negative (fn [_] (ok 42)))]
      (is (= {:ok 42} (op -1)))))

  (testing "recover chain"
    (let [op (-> (recover fail-if-negative (fn [_] (ok 1)))
                 (recover (fn [_] (ok 42))))]
      (is (= {:ok 2} (railway-> (ok 1)
                                op
                                add-one))))))

;; Test complex scenarios
(deftest complex-scenarios-test
  (testing "Complex chain with parallel and recovery"
    (let [complex-operation (fn [x]
                              (railway-> (ok x)
                                             (parallel add-one multiply-by-two)
                                             merge-results
                                             (recover fail-if-negative (fn [_] (ok 100)))
                                             add-one))]
      ;; Test case: Negative input
      (is (= {:ok 101} (complex-operation -1)))

      ;; Test case: Positive input
      (is (= {:ok 5} (complex-operation 1))))))

;; Performance tests
(deftest ^:performance performance-test
  (testing "Chain performance"
    (let [start-time (System/currentTimeMillis)
          result (railway-> (ok 1)
                            add-one
                            multiply-by-two
                            add-one
                            multiply-by-two)
          end-time (System/currentTimeMillis)
          elapsed (- end-time start-time)]
      (is (< elapsed 10) "Basic chain should complete quickly"))))