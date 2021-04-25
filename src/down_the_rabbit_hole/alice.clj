(ns down-the-rabbit-hole.alice
  (:require
   [down-the-rabbit-hole.core :as core])
  (:import
   [org.jetbrains.skija Canvas Rect]))

(defrecord Alice [id]
  core/IRenderable
  (-render [this canvas now]
    (let [sprite (-> now (/ 300) (mod 2) long)
          dy (core/oscillation @core/*now 0 2000 5)]
      (core/draw-sprite canvas (* sprite 64) 0 64 128 100 (+ 30 dy))
      (when (or (core/hovered? this) (core/selected? this))
        (core/draw-text-centered canvas "Alice" 132 154))))

  (-z-index [this] 200)

  core/IHoverable
  (-bbox [this]
    (Rect/makeXYWH 100 40 64 108))

  core/ISelectable)


(defn alice []
  (->Alice (core/next-id)))