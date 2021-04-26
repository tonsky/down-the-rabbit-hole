(ns down-the-rabbit-hole.game
  (:require
   [clojure.java.io :as io]
   [clojure.stacktrace :as stacktrace]
   [clojure.string :as str]
   [down-the-rabbit-hole.alice :as alice]
   [down-the-rabbit-hole.decorations :as decorations]
   [down-the-rabbit-hole.core :as core]
   [down-the-rabbit-hole.item :as item]
   [down-the-rabbit-hole.rabbit :as rabbit])
  (:import
   [java.util Random]
   [org.jetbrains.skija Canvas Color4f Font FontStyle FontMgr Image Paint PaintMode PaintStrokeCap Rect Typeface]))

(def *broken (atom false))

(defmacro safe-call [call]
  `(try
     (when-not @*broken
       ~call)
     (catch Exception e#
       (reset! *broken true)
       (stacktrace/print-stack-trace (stacktrace/root-cause e#)))))

(reset! core/*state
  {:frame (System/nanoTime)
   :hovered-id nil
   :selected-id nil
   :objects
   (reduce #(assoc %1 (:id %2) %2) {}
     (concat 
       [(decorations/background)
        (alice/alice)
        (rabbit/rabbit)
        (decorations/particles)
        (decorations/walls)]
       (repeatedly 5 #(item/item (rand-nth item/types)))))})

(defn on-tick [state now]
  (assoc state
    :frame (System/nanoTime)))

(defn draw-impl [^Canvas canvas window-width window-height]
  (let [now (reset! core/*now (System/currentTimeMillis))
        fps (long (/ 1000000000 (- (System/nanoTime) (:frame @core/*state 0))))
        {:keys [frame selected-id objects]} (swap! core/*state on-tick now)]

    (.clear canvas (core/color 0xFF140043))
    (.scale canvas 3 3)

    (doseq [obj (->> objects
                  vals
                  (filter #(satisfies? core/IRenderable %))
                  (sort-by core/-z-index))]
      (core/-render obj canvas now))
    (core/draw-text canvas (str fps) 10 224)

    (when-some [selected (get objects selected-id)]
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
          (.drawLine canvas (+ r 1) (- b 4) (+ r 1) (+ b 1) paint))))))

(defn draw [canvas window-width window-height]
  (safe-call (draw-impl canvas window-width window-height)))

(defn on-key-press-impl [state key pressed? mods]
  (condp = key
    262 ;; right
    state

    263 ;; left
    state

    264 ;; bottom
    state

    265 ;; up
    state

    82 ;; R
    state

    (do
      (println key)
      state)))

(defn on-key-press [key pressed? mods]
  (safe-call (swap! core/*state on-key-press-impl key pressed? mods)))

(defn on-mouse-move-impl [state x y]
  (let [{:keys [hovered-id objects]} state
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
        (assoc state :hovered-id nil)))))

(defn on-mouse-move [x y]
  (safe-call (swap! core/*state on-mouse-move-impl (long (/ x 3)) (long (/ y 3)))))

(defn on-mouse-click-impl [state button pressed? mods]
  (let [{:keys [hovered-id selected-id objects]} state]
    (cond
      (and (= 0 button) pressed? (some? hovered-id))
      (assoc state :selected-id hovered-id)

      (and (= 0 button) pressed?)
      (assoc state :selected-id nil)

      :else
      state)))

(defn on-mouse-click [button pressed? mods]
  (safe-call (swap! core/*state on-mouse-click-impl button pressed? mods)))
