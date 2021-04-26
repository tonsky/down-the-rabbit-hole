(ns down-the-rabbit-hole.alice
  (:require
   [datascript.core :as ds]
   [down-the-rabbit-hole.core :as core]
   [down-the-rabbit-hole.rabbit :as rabbit])
  (:import
   [org.jetbrains.skija Canvas Point Rect]))

(defmethod core/render :renderer/alice [canvas db game entity]
  (let [{:game/keys [now]} game
        [x y]  (cond
                 (core/in-phase? game :phase/player-enter) [100 (core/lerp-phase game -96 32)]
                 (core/in-phase? game :phase/enemy-enter)  [100 32]
                 (core/in-phase? game :phase/items-enter)  [(core/lerp-phase game 100 50) 32]
                 :else [50 32])
        dy     (core/oscillation now 0 2000 5)
        sprite (-> now (/ 300) (mod 2) long)]
    (core/draw-sprite canvas (* sprite 72) 0 72 96 x (+ y dy))
    (when (core/in-phase? game :phase/player-turn :phase/enemy-turn)
      (core/draw-text-centered canvas
        (str "♥" (:health entity) " ⚡" (:energy entity))
        (+ x 36) 140))))

(defn alice-tx []
  [{:role      :role/player
    :renderer  :renderer/alice
    :z-index   200
    ; :bbox      (Rect/makeXYWH 100 32 72 96)
    ; :hoverable true
    :health    10
    :energy    3}])