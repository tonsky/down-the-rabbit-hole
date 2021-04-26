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
    (core/draw-text canvas (str fps) 10 224)

    #_(when-some [selected (get objects selected-id)]
      (let [bbox (core/-bbox selected)
            l    (.getLeft bbox)
            t    (.getTop bbox)
            r    (.getRight bbox)
            b    (.getBottom bbox)]
        (with-open [paint (-> (Paint.) (.setColor (core/color 0xFFFFFFFF)) (.setMode PaintMode/STROKE) (.setStrokeWidth 2))]
          (.drawLine canvas (- l 2) (- t 1) (+ l 4) (- t 1) paint)
          (.drawLine canvas (- l 1) (- t 2) (- l 1) (+ t 4) paint)

          (.drawLine canvas (+ r 2) (- t 1) (- r 4) (- t 1) paint)
          (.drawLine canvas (+ r 1) (- t 2) (+ r 1) (+ t 4) paint)

          (.drawLine canvas (- l 2) (+ b 1) (+ l 4) (+ b 1) paint)
          (.drawLine canvas (- l 1) (- b 4) (- l 1) (+ b 1) paint)

          (.drawLine canvas (+ r 2) (+ b 1) (- r 4) (+ b 1) paint)
          (.drawLine canvas (+ r 1) (- b 4) (+ r 1) (+ b 1) paint))))

))

(defn on-key-press [key pressed? mods]
  #_(println key pressed? mods))


(defn on-mouse-move [x y]
  (let [x (long (/ x 3))
        y (long (/ y 3))]
  #_(let [{:keys [hovered-id objects]} state
        hovered (get objects hovered-id)]
    (if (and (some? hovered) (core/in-rect? x y (core/-bbox hovered)))
      state
      (if-some [hovered' (->> objects
                           (vals)
                           (filter #(satisfies? core/IHoverable %))
                           (sort-by core/-z-index)
                           (reverse)
                           (core/find #(core/in-rect? x y (core/-bbox %))))]
        (assoc state :hovered-id (:id hovered'))
        (assoc state :hovered-id nil))))))

(defn on-mouse-click [button pressed? mods]
  #_(let [{:keys [hovered-id selected-id objects]} state]
    (cond
      (and (= 0 button) pressed? (some? hovered-id))
      (assoc state :selected-id hovered-id)

      (and (= 0 button) pressed?)
      (assoc state :selected-id nil)

      :else
      state)))
