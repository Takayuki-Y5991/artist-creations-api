(ns artist-connections.rop.core)

(defn ^:pure ok [value]
  {:ok value})

(defn ^:pure error [reason]
  {:error reason})

;; Bind operator
(defn >>= [result next-fn]
  (if (:ok result)
    (next-fn (:ok result))
    result))

;; Custom threading macro for ROP
(defmacro railway-> [init & steps]
  (reduce (fn [acc step]
            `(>>= ~acc (fn [x#] (~step x#))))
            init
            steps))

(defn parallel [& steps]
  (fn [x]
    (let [results (map #(% x) steps)]
      (if (every? :ok results)
        (ok (map :ok results))
        (error (some :error (filter :error results)))))))

(defn parallel-async [& steps]
  (fn [x]
    (let [promises (map (fn [step]
                          (let [p (promise)]
                            (future
                              (try
                                (deliver p (step x))
                                (catch Exception e
                                  (deliver p (error (str "Exception: " (.getMessage e)))))))
                            p))
                        steps)
          results (map deref promises)]
      (if (every? :ok results)
        (ok (map :ok results))
        (error (some :error (filter :error results)))))))

(defn recover [step recovery-fn]
  (fn [x]
    (let [result (step x)]
      (if (:ok result)
        result
        (recovery-fn result)))))