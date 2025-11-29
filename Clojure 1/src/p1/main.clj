(ns p1.main)

(def alph (list "a" "b" "c"))

(def N 3)

(def alph_list (map (fn [elem]
                 (list elem)) alph))
(println alph_list)


(def res_list alph_list)
(def temp res_list)


(while (> N 1)
  (def temp (reduce concat (map (fn [elem] (filter not-empty (map (fn [cur_list]
      (if (= (first cur_list) (first elem))
        (list)
        (concat elem cur_list)))
              res_list))) alph_list)))
  (def res_list temp)
  (def N (dec N))
  )

(println (map (fn [list] (reduce str list)) res_list))