(ns down-the-rabbit-hole.item
  (:require
   [down-the-rabbit-hole.core :as core])
  (:import
   [org.jetbrains.skija Canvas Paint PaintMode Rect]))

(def items
  {:bottle     {:sprite 0 :description "Bottle\nHeal ♥1"}
   :mushroom   {:sprite 1 :description "Mushroom\n50% grow\n50% shrink"}
   :cookie     {:sprite 2 :description "Cookie\nIgnore next ⚔"}
   :knight     {:sprite 3 :description "Knight\nAttack for ⚔1"}
   :king       {:sprite 4 :description "King\nDefend for ♢1"}
   :queen      {:sprite 5 :description "Queen\nAttack for ⚔2"}
   :clock      {:sprite 6 :description "Clock\nSkip turn"}
   :spade      {:sprite 7 :description "Spade\n50% attack ⚔1\n50% defend ♢1"}
   :white-rose {:sprite 8 :description "White Rose\nSkip turn"}
   :red-rose   {:sprite 9 :description "Red Rose\nTake extra turn"}
   :smile      {:sprite 10 :description "Smile\nDisappear for a turn"}})

(defn lerp [game from to idx]
  (let [ratio (-> (- (:game/now game) (:game/phase-started game))
                (/ (:game/phase-length game)))
        ratio' (-> ratio (* 1.9) (- (* idx 0.9 1/6)) (min 1.0) (max 0.0))]
    (core/lerp from to ratio')))

(defmethod core/render :renderer/item [canvas db game entity]
  (let [{:game/keys [now]} game
        {:item/keys [phase type owner idx] :keys [bbox]} entity
        {:keys [sprite description]} (get items type)
        dy      (core/oscillation now (* 1000 phase) (+ 2000 (* 1000 phase)) 3)
        ry      (cond
                  (core/in-phase? game :phase/items-enter)
                  (lerp game core/screen-height 0 idx)
                  
                  (and (= :role/player (:role owner)) (core/in-phase? game :phase/player-items-leave))
                  (lerp game 0 (- core/screen-height) idx)

                  (and (= :role/enemy (:role owner)) (core/in-phase? game :phase/enemy-items-leave))
                  (lerp game 0 (- core/screen-height) idx)
                  
                  (and (= :role/player (:role owner)) (core/in-phase? game :phase/enemy-turn :phase/enemy-items-leave))
                  (- core/screen-height)

                  :else
                  0)
        _       (core/draw-sprite canvas (* sprite 32) 384 32 32 (.getLeft bbox) (+ (.getTop bbox) dy ry))
        hovered (core/hovered db)]
    (when (and (core/in-phase? game :phase/player-turn :phase/enemy-turn)
            (= (:db/id entity) (:db/id hovered)))
      (let [l (.getLeft bbox)
            t (.getTop bbox)
            r (.getRight bbox)
            b (.getBottom bbox)]
        (with-open [paint (-> (Paint.) (.setColor (core/color 0xFFFFFFFF)) (.setMode PaintMode/STROKE) (.setStrokeWidth 2))]
          (.drawLine canvas (- l 2) (- t 1) (+ l 4) (- t 1) paint)
          (.drawLine canvas (- l 1) (- t 2) (- l 1) (+ t 4) paint)

          (.drawLine canvas (+ r 2) (- t 1) (- r 4) (- t 1) paint)
          (.drawLine canvas (+ r 1) (- t 2) (+ r 1) (+ t 4) paint)

          (.drawLine canvas (- l 2) (+ b 1) (+ l 4) (+ b 1) paint)
          (.drawLine canvas (- l 1) (- b 4) (- l 1) (+ b 1) paint)

          (.drawLine canvas (+ r 2) (+ b 1) (- r 4) (+ b 1) paint)
          (.drawLine canvas (+ r 1) (- b 4) (+ r 1) (+ b 1) paint)))
      (case (:role owner)
        :role/player (core/draw-text-centered canvas description 133 172)
        :role/enemy  (core/draw-text-centered canvas description 313 172)))))
  
(defn item-tx [idx items-count owner]
  (let [type (rand-nth (keys items))
        [x rx ry h] (case (:role owner)
                      :role/player
                      [140 (* (mod idx 2) 40) (* idx 20) (- (* items-count 20) 8)]
                      :role/enemy 
                      [270 0 (* idx 40) (- (* items-count 40) 8)])
        y (-> 80 (- (quot h 2)) (+ ry))]
    [{:role       :role/item
      :renderer   :renderer/item
      :bbox       (Rect/makeXYWH (+ x rx) y 32 32)
      :item/type  type
      :item/phase (rand)
      :item/owner (:db/id owner)
      :item/idx   idx
      :z-index    300}]))