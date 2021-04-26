(ns down-the-rabbit-hole.end-turn
  (:require
   [datascript.core :as ds]
   [down-the-rabbit-hole.core :as core]
   [down-the-rabbit-hole.item :as item]
   [down-the-rabbit-hole.rabbit :as rabbit])
  (:import
   [org.jetbrains.skija Canvas Paint Point Rect]))

(defmethod core/render :renderer/end-turn [canvas db game entity]
  (let [{:game/keys [now]} game]
    (when (core/in-phase? game :phase/player-turn)
      (let [hovered (core/hovered db)
            color   (if (= :role/end-turn (:role hovered)) 0xFF862593 0xFF2c0c5f)]
          (with-open [bg (-> (Paint.) (.setColor (core/color color)))]
            (.drawRect canvas (Rect/makeXYWH 83 210 90 19) bg)
            (core/draw-text-centered canvas "End Turn" 128 216))))))

(defn end-turn-tx []
  [{:role     :role/end-turn
    :renderer :renderer/end-turn
    :bbox     (Rect/makeXYWH 83 210 90 19)
    :z-index  400}])