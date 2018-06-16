(ns d3-tuts.macros)

(defmacro attrs [obj mp]
  (if (empty? mp)
      (list 'identity obj)
      (loop [blck `(-> ~obj )
             [k v] (first mp)
             r (rest mp)
             ]
        (if (empty? r)
          (concat blck (list (list '.attr k v)))
          (recur
           (concat blck (list (list '.attr k v)))
           (first r)
           (rest r))))))
