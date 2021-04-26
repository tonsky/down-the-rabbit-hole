(ns down-the-rabbit-hole.rabbit
  (:require
   [down-the-rabbit-hole.core :as core])
  (:import
   [org.jetbrains.skija Canvas Rect]))

(defrecord Rabbit [id]
  core/IRenderable
  (-render [this canvas now]
    (let [sprite (-> now (/ 300) (mod 2) long)
          dy (core/oscillation now 0 3000 10)]
      (core/draw-sprite canvas (* sprite 64) 128 64 64 260 (+ 50 dy))
      (core/draw-text-centered canvas "Rabbit\n♥66" 292 140)))
  
  (-z-index [this] 200)

  core/IHoverable
  (-bbox [this]
    (Rect/makeXYWH 260 32 64 96))

  core/ISelectable)

(defn rabbit []
  (->Rabbit (core/next-id)))