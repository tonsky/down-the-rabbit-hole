(ns down-the-rabbit-hole.game
  (:require
   [clojure.java.io :as io]
   [clojure.string :as str]
   [datascript.core :as ds]
   [down-the-rabbit-hole.alice :as alice]
   [down-the-rabbit-hole.decorations :as decorations]
   [down-the-rabbit-hole.core :as core]
   [down-the-rabbit-hole.item :as item]
   [down-the-rabbit-hole.phases :as phases]
   [down-the-rabbit-hole.rabbit :as rabbit])
  (:import
   [java.util Random]
   [org.jetbrains.skija Canvas Color4f Font FontStyle FontMgr Image Paint PaintMode PaintStrokeCap Rect Typeface]))

(defn start! []
  (as-> (ds/empty-db core/schema) %
    (ds/db-with % (phases/tick % {:game/phase :phase/start}))
    (reset! core/*db %)))

(defn draw [^Canvas canvas]
  (let [db   @core/*db
        game (core/game db)
        nano (System/nanoTime)
        fps  (long (/ 1000000000 (- nano (:game/nano game 0))))
        now  (System/currentTimeMillis)
        _    (ds/transact core/*db [{:db/id 1 :game/nano nano :game/now now}])
        db   (:db-after
              (ds/transact! core/*db
                (phases/tick db game)))
        game (core/game db)]

    (.scale canvas 3 3)
    (doseq [entity (->> (core/entities db :aevt :renderer)
                     (sort-by :z-index))]
      (core/render canvas db game entity))
    #_(core/draw-text canvas (str fps) 10 224)))

(defn on-key-press [key pressed? mods]
  #_(println key pressed? mods))

(def *mouse-pos (atom [0 0]))

(defn on-mouse-move [x y]
  (let [x (long (/ x 3))
        y (long (/ y 3))
        _ (reset! *mouse-pos [x y])
        db @core/*db
        tx (core/cond+
             :let [hovered (core/hovered db)]

             (and (some? hovered) (core/in-rect? x y (:bbox hovered)))
             []

             :let [hovered' (core/find-hovered db [x y])]

             (some? hovered')
             (concat
               (when (some? hovered)
                 [[:db/retract (:db/id hovered) :hovered true]])
               [[:db/add (:db/id hovered') :hovered true]])
   
             (some? hovered)
             [[:db/retract (:db/id hovered) :hovered true]]

             :else
             [])]
  (when-not (empty? tx)
    (ds/transact! core/*db tx))))

(defn on-mouse-click [button pressed? mods]
  (let [db      @core/*db
        hovered (or (core/hovered db)
                  (core/find-hovered db @*mouse-pos))]
    (when (and (= 0 button) pressed? (some? hovered))
      (cond
        (= :role/end-turn (:role hovered))
        (ds/transact! core/*db
          [{:db/id 1
            :game/phase         :phase/player-items-leave
            :game/phase-started (:game/now (core/game db))
            :game/phase-length  500}
           [:db/retractEntity (:db/id hovered)]])))))
