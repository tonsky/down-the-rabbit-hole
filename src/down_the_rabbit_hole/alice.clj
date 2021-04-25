(ns down-the-rabbit-hole.alice
  (:require
   [down-the-rabbit-hole.core :as core])
  (:import
   [org.jetbrains.skija Canvas]))

(defrecord Alice []
  core/IRender
  (-render [this canvas now]
    (let [sprite (-> now (/ 300) (mod 2) long)
          dy (core/oscillation @core/*now 0 2000 5)]
      (core/draw-sprite canvas (* sprite 64) 0 64 128 100 (+ 30 dy))
      (core/draw-text-centered canvas "Alice" 132 140)))
  (-z-index [this] 200))
