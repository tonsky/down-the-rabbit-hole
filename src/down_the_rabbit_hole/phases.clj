(ns down-the-rabbit-hole.phases
  (:require
   [datascript.core :as ds]
   [down-the-rabbit-hole.core :as core]
   [down-the-rabbit-hole.alice :as alice]
   [down-the-rabbit-hole.decorations :as decorations]
   [down-the-rabbit-hole.end-turn :as end-turn]
   [down-the-rabbit-hole.item :as item]
   [down-the-rabbit-hole.rabbit :as rabbit])
  (:import
   [org.jetbrains.skija Canvas Point Rect]))

(defmulti tick
  (fn [db game]
    (:game/phase game)))

(defmethod tick :default [db game]
  #_(try
    (println "default" (:game/phase game) (core/lerp-phase game 0 1))
    (catch Exception e))
  [])

(defmethod tick :phase/start [_ _]
  (let [now (System/currentTimeMillis)]
    (concat
      [{:db/id 1
        :game/nano          (System/nanoTime)
        :game/now           now
        :game/phase         :phase/player-enter
        :game/phase-started now
        :game/phase-length  200}]
      (decorations/decorations-tx)
      (alice/alice-tx))))

(defmethod tick :phase/player-enter [db game]
  (if (>= (core/lerp-phase game 0 1) 1)
    (concat
      [{:db/id 1
       :game/phase         :phase/enemy-enter
       :game/phase-started (:game/now game)
       :game/phase-length  400}]
      (rabbit/rabbit-tx))
    []))

(defmethod tick :phase/enemy-enter [db game]
  (if (>= (core/lerp-phase game 0 1) 1)
    [{:db/id 1
      :game/phase         :phase/players-separate
      :game/phase-started (:game/now game)
      :game/phase-length  200}]
    []))

(defn items-enter-tx [db game]
  (let [player       (first (core/entities db :avet :role :role/player))
        player-items (mapcat #(item/item-tx % 6 player) (range 0 6))
        enemy        (first (core/entities db :avet :role :role/enemy))
        enemy-items  (mapcat #(item/item-tx % 3 enemy) (range 0 3))]
    (concat
      [{:db/id 1
        :game/phase         :phase/items-enter
        :game/phase-started (:game/now game)
        :game/phase-length  500}]
      player-items
      enemy-items)))

(defmethod tick :phase/players-separate [db game]
  (if (>= (core/lerp-phase game 0 1) 1)
    (items-enter-tx db game)
    []))

(defmethod tick :phase/items-enter [db game]
  (if (>= (core/lerp-phase game 0 1) 1)
    (concat
      [{:db/id 1
        :game/phase         :phase/player-turn
        :game/phase-started (:game/now game)
        :game/phase-length  Long/MAX_VALUE}]
      (end-turn/end-turn-tx))
    []))

(defmethod tick :phase/player-items-leave [db game]
  (if (>= (core/lerp-phase game 0 1) 1)
    (concat
      [{:db/id 1
        :game/phase         :phase/enemy-items-leave
        :game/phase-started (:game/now game)
        :game/phase-length  500}]
      (->> (core/entities db :avet :role :role/item)
        (filter #(= :role/player (:role (:item/owner %))))
        (map (fn [e] [:db/retractEntity (:db/id e)]))))
    []))

(defmethod tick :phase/enemy-items-leave [db game]
  (if (>= (core/lerp-phase game 0 1) 1)
    (concat
      (->> (core/entities db :avet :role :role/item)
        (filter #(= :role/enemy (:role (:item/owner %))))
        (map (fn [e] [:db/retractEntity (:db/id e)])))
      (items-enter-tx db game))
    []))
