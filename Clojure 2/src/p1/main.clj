(ns p1.main
  (:require [clojure.test :refer :all])
  )

(defn next_seg [low segment_size, primes]
          (let [
                high (+ low segment_size)
                sieve (boolean-array segment_size true)
                ]

              (doseq [p primes]
                (let [start (+ p p)]
                  (doseq [i (range start high p)]
                    (when (>= i low)
                      (aset sieve (- i low) false)))))


                (for [i (range segment_size) :when (aget sieve i)]
                  (+ i low)
                  )

            )
  )



(defn Er []
          (lazy-seq
            (let [
                  primes [2 3]]
              ((fn generate-segments [low seg_size known-primes]
                 (let [new-primes (next_seg low seg_size known-primes)
                       all-primes (concat known-primes new-primes)]
                   (concat new-primes
                           (lazy-seq
                             (generate-segments (+ low seg_size) (* seg_size 2)
                                                all-primes)))))
               2 4 primes))
              )
            )


(deftest testFirst
  (let [primes (take 25 (Er))]
    (is (some #{2} primes))
    (is (some #{3} primes))
    (is (not (some #{1} primes)))
    (is (not (some #{4} primes)))
    )
  )

(deftest testTF
  (let [primes (take 25 (Er))]
    (is (not (some #{25} primes)))
    )
  )

(deftest testH
  (let [primes (take 100 (Er))]
    (is (some #{541} primes))
    )
  )

(->> (Er) (take 100) (println))