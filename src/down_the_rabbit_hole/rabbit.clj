(ns down-the-rabbit-hole.rabbit
  (:require
   [down-the-rabbit-hole.core :as core])
  (:import
   [org.jetbrains.skija Canvas]))

(defrecord Rabbit []
  core/IRender
  (-render [this canvas now]
    (let [sprite (-> now (/ 300) (mod 2) long)
          dy (core/oscillation now 0 3000 10)]
      (core/draw-sprite canvas (* sprite 64) 128 64 64 260 (+ 60 dy))
      (core/draw-text-centered canvas "The Rabbit" 292 140)))
  (-z-index [this] 200))
