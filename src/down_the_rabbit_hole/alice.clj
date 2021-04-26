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
      (core/draw-sprite canvas (* sprite 72) 0 72 96 100 (+ 30 dy))
      (core/draw-text-centered canvas "Alice\n♥10 ⚡4" 132 140)))

  (-z-index [this] 200)

  core/IHoverable
  (-bbox [this]
    (Rect/makeXYWH 100 32 72 96))

  core/ISelectable)


(defn alice []
  (->Alice (core/next-id)))