(ns main
  (:require [clojure.test :refer :all])
  )


(def cnt_elem_sub_list 20)
(def cnt_futures 4)


(defn get_lists [start_list cnt_elem cnt_lists]
  (vec
    (for [i (range cnt_lists)]
      (take cnt_elem (drop (* i cnt_elem) start_list)))))


(defn futureFilter [start_list cond]
  (future
    (reduce (fn [acc item]
              (if (cond item)
                (conj acc item)
                acc)
              ) [] start_list)))



(defn my_filter [start_list cond]
  (if (empty? start_list)
    ()
  (lazy-seq
    (let [cur_lists (get_lists start_list cnt_elem_sub_list cnt_futures)
          futures (for [i (range cnt_futures)]
                          (futureFilter (get cur_lists i) cond)
                          )
          ans (map deref futures)
          ]

      (concat (reduce (fn [acc item]
                        (concat acc item)
                        ) [] ans)
              (lazy-seq
                (my_filter (drop (* cnt_futures  cnt_elem_sub_list) start_list) cond)
                )
              )
      )
    )
  )
  )

(deftest testPos
  (let [slist (list -1 2 -5 4 5 -4 6)
        filtered (my_filter slist pos?)
        ans (list 2 4 5 6)]
    (is (= filtered ans))
    )
  )

(deftest testUnlimited
  (let [slist (range)
        filtered (take 5 (my_filter slist even?))
        ans (list 0 2 4 6 8)]
      (is (= filtered ans))
    )
  )

(let [s1 (System/currentTimeMillis)
      res1 (take 20000 (my_filter (range) even?))
      s2 (System/currentTimeMillis)
      res2 (take 20000 (filter even? (range)))
      s3 (System/currentTimeMillis)
      ]

    (println res1)
    (println s2 - s1)
    (println res2)
    (println s3 - s2)
  )