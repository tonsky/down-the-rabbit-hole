(ns down-the-rabbit-hole.rabbit
  (:require
   [down-the-rabbit-hole.core :as core])
  (:import
   [org.jetbrains.skija Canvas Point Rect]))

(defmethod core/render :renderer/rabbit [canvas db game entity]
  (let [{:game/keys [now]} game
        [x y]  (cond
                 (core/in-phase? game :phase/player-enter) [276 -128]
                 (core/in-phase? game :phase/enemy-enter)  [276 (core/lerp-phase game -128 48)]
                 (core/in-phase? game :phase/items-enter)  [(core/lerp-phase game 276 326) 48]
                 :else [326 48])
        sprite (-> (:game/now game) (/ 100) (mod 5) long)
        dy (core/oscillation (:game/now game) 0 3000 10)
        {:keys [center]} entity]
    (core/draw-sprite canvas (* sprite 32) 128 32 64 x (+ y dy))
    (when (core/in-phase? game :phase/player-turn :phase/enemy-turn)
      (core/draw-text-centered canvas
        (str "♥" (:health entity) " ⚡" (:energy entity))
        (+ x 16) 140))))

(defn rabbit-tx []
  [{:role      :role/enemy
    :renderer  :renderer/rabbit
    :z-index   200
    ; :center    (Point. 292 -64)
    ; :bbox      (Rect/makeXYWH 260 32 64 96)
    ; :hoverable true
    :health    10
    :energy    3}])