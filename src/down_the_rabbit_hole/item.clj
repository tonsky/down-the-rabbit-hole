(ns down-the-rabbit-hole.item
  (:require
   [down-the-rabbit-hole.core :as core])
  (:import
   [org.jetbrains.skija Canvas Rect]))

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

(defmethod core/render :renderer/item [canvas db game entity]
  (let [{:game/keys [now]} game
        {:item/keys [phase idx type owner]} entity
        sprite (.indexOf types type)
        items  (:item/_owner owner)
        [x rx ry h] (case (:role owner)
                      :role/player
                      [140 (* (mod idx 2) 40) (* idx 20) (* (count items) 20)]
                      :role/enemy 
                      [270 0 (* idx 40) (* (count items) 20)])
        dy     (core/oscillation now (* 1000 phase) (+ 2000 (* 1000 phase)) 3)
        y      (-> 80 (- (quot h 2)) (+ ry))
        y      (cond
                 (core/in-phase? game :phase/items-enter) (core/lerp-phase game (+ core/screen-height y) y)
                 :else y)]
    (core/draw-sprite canvas (* sprite 32) 384 32 32 (+ x rx) (+ y dy))
    #_(core/draw-text-centered canvas (description this) 132 200)))

(defn item-tx [idx owner]
  (let [type (rand-nth types)]
    [{:renderer   :renderer/item
      ; :hoverable  true
      :item/type  type
      :item/phase (rand)
      :item/owner (:db/id owner)
      :item/idx   idx
      :z-index    300
      :item/description
      (case type
        :bottle   "Bottle\nHeal ♥1"
        :mushroom "Mushroom\n50% grow\n50% shrink"
        :cookie   "Cookie\nIgnore next ⚔attack"
        :knight   "Knight\nAttack for ⚔1"
        :king     "King\nDefend for ♢1"
        :queen    "Queen\nAttack for ⚔2"
        :clock    "Clock\nSkip turn"
        :spade    "Spade\n50% attack ⚔1\n50% defend ♢1"
        :white-rose "White Rose\nSkip turn"
        :red-rose "Red Rose\nTake extra turn"
        :smile    "Smile\nDisappear for a turn")}]))