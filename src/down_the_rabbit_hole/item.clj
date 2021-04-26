(ns down-the-rabbit-hole.item
  (:require
   [down-the-rabbit-hole.core :as core])
  (:import
   [org.jetbrains.skija Canvas Rect]))

(declare coord description)

(def types
  [:bottle
   :mushroom
   :cookie
   :knight
   :king
   :queen
   :clock
   :spade
   :white-rose
   :red-rose
   :smile])

(defrecord Item [id type phase]
  core/IRenderable
  (-render [this canvas now]
    (let [sprite (.indexOf types type)
          dy     (core/oscillation @core/*now (* 1000 phase) (+ 2000 (* 1000 phase)) 3)
          [x y]  (coord id type phase)]
      (core/draw-sprite canvas (* sprite 32) 384 32 32 x (+ y dy))
      (when (core/selected? this)
        (core/draw-text-centered canvas (description this) 132 200))))

  (-z-index [this] 300)

  core/IHoverable
  (-bbox [this]
    (let [[x y] (coord id type phase)]
      (Rect/makeXYWH x y 32 32)))

  core/ISelectable)

(defn item [type]
  (->Item (core/next-id) type (rand)))

(defn coord [id type phase]
  (let [items  (->> @core/*state :objects vals (filter #(instance? Item %)) (map :id) sort)
        idx    (.indexOf items id)
        dx     (-> phase (- 0.5) (* 10) long)]
    (if (even? idx)
      [(+ 55 dx)
       (-> 90
         (- (-> (count items) (/ 2) (Math/ceil) (* 48) (/ 2) long))
         (+ (-> idx (/ 2) (Math/floor) (* 48))))]
      [(+ 190 dx)
       (-> 90
         (- (-> (count items) (/ 2) (Math/floor) (* 48) (/ 2) long))
         (+ (-> idx (/ 2) (Math/floor) (* 48))))])))

(defmulti description :type)

(defmethod description :bottle [_]
  "Bottle\nHeal ♥1")

(defmethod description :mushroom [_]
  "Mushroom\n50% grow\n50% shrink")

(defmethod description :cookie [_]
  "Cookie\nIgnore next ⚔attack")

(defmethod description :knight [_]
  "Knight\nAttack for ⚔1")

(defmethod description :king [_]
  "King\nDefend for ♢1")

(defmethod description :queen [_]
  "Queen\nAttack for ⚔2")

(defmethod description :clock [_]
  "Clock\nSkip turn")

(defmethod description :spade [_]
  "Spade\n50% attack ⚔1\n50% defend ♢1")

(defmethod description :white-rose [_]
  "White Rose\nSkip turn")

(defmethod description :red-rose [_]
  "Red Rose\nTake extra turn")

(defmethod description :smile [_]
  "Smile\nDisappear for a turn")
