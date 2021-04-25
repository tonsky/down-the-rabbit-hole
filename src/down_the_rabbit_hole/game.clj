(ns down-the-rabbit-hole.game
  (:require
   [clojure.java.io :as io]
   [clojure.stacktrace :as stacktrace]
   [clojure.string :as str])
  (:import
   [java.io ByteArrayOutputStream]
   [java.util Random]
   [org.jetbrains.skija Canvas Color4f Font FontStyle FontMgr Image Paint PaintMode PaintStrokeCap Rect Typeface]))

(def *broken (atom false))

(def screen-width 426)
(def screen-height 240)

(defn init-particle [particles idx]
  (aset-float particles (* idx 3) (rand-int screen-width)) ;; x
  (aset-float particles (+ (* idx 3) 1) (* screen-height 2)) ;; y
  (aset-float particles (+ (* idx 3) 2) (+ 1 (rand 15)))) ;; speed

(def rand-wall
  (int-array (repeatedly 150 #(rand-int 2))))

(def rand-bg
  (int-array (repeatedly 300 #(rand-int 3))))

(defn new-state []
  (let [particles (float-array 90)]
    (doseq [i (range 0 (/ (alength particles) 3))]
      (init-particle particles i))
    {:particles particles}))

(def *state (atom (new-state)))

(defn slurp-bytes [x]
  (with-open [is (io/input-stream x)
              os (ByteArrayOutputStream.)]
    (io/copy is os)
    (.toByteArray os)))

(def sprites
  (Image/makeFromEncoded (slurp-bytes (io/resource "sprites.png"))))

(defn color [^long l]
  (.intValue (Long/valueOf l)))

(defn with-alpha [^long l ^long alpha]
  (color
    (bit-or
      (-> (bit-and l 0x00FFFFFF))
      (-> (bit-and alpha 0xFF) (bit-shift-left 24)))))

(defn in? [x xs]
  (some #(when (= % x) true) xs))

(defmacro cond+ [& clauses]
  (when-some [[test expr & rest] clauses]
    (case test
      :let `(let ~expr (cond+ ~@rest))
      `(if ~test ~expr (cond+ ~@rest)))))

(defn on-tick [state now]
  (let [{:keys [particles]} state]
    (doseq [idx (range 0 (/ (alength particles) 3))
            :let [y     (aget particles (+ (* idx 3) 1))
                  speed (aget particles (+ (* idx 3) 2))
                  y'    (Math/round (- y speed))]]
      (if (< y' 0)
        (init-particle particles idx)
        (do
          (aset-float particles (+ (* idx 3) 1) y')
          #_(aset-float particles (+ (* idx 3) 2) (* speed 1.1)))))
    (assoc state
      :particles particles)))

(defn oscillation [now phase period amplitude]
  (-> now (mod period) (/ period) (+ phase) (* 2 Math/PI) (Math/cos) (* amplitude) (Math/round)))

(defn draw-text [canvas ^String s x0 y0]
  (loop [i 0
         x x0
         y y0]
    (cond+
      (>= i (count s))
      nil

      :let [ch (.charAt s i)]

      (= \newline ch)
      (recur (inc i) x0 (+ y 12))

      :let [sx (-> ch (int) (mod 16) (* 8))
            sy (-> ch (int) (quot 16) (* 8) (+ 320))]

      :else
      (do
        (.drawImageRect canvas sprites (Rect/makeXYWH sx sy 8 8) (Rect/makeXYWH x y 8 8))
        (recur (inc i) (+ x 8) y)))))

(defn draw-text-centered [canvas ^String s center y0]
  (let [lines (str/split-lines s)]
    (.save canvas)
    (.translate canvas center y0)
    (doseq [line lines
            :let [width (-> (count line) (* 8))]]
      (doseq [[ch x] (map vector line (range))
              :let [sx (-> ch (int) (mod 16) (* 8))
                    sy (-> ch (int) (quot 16) (* 8) (+ 320))]]
        (.drawImageRect canvas sprites (Rect/makeXYWH sx sy 8 8) (Rect/makeXYWH (- (* x 8) (quot width 2)) 0 8 8)))
      (.translate canvas 0 12))
    (.restore canvas)))

(defn draw-impl [^Canvas canvas window-width window-height]
  (let [now (System/currentTimeMillis)
        _   (swap! *state on-tick now)
        {:keys [particles]} @*state]

    (.clear canvas (color 0xFF280e5b))
    (.scale canvas 3 3)

    ;; bg
    (let [di (-> now (/ 900) long (mod 15))
          dy (-> now (mod 900) (/ 900) (* 64))]
      (doseq [y (range 0 15)
              x (range 0 20)
              :let [sprite (aget rand-bg (-> y (+ di) (* 20) (+ x) (mod 300)))]]
        (.drawImageRect canvas sprites (Rect/makeXYWH (* sprite 64) 256 64 64) (Rect/makeXYWH (* x 64) (-> y (* 64) (- dy)) 64 64))))

    ;; Alice
    (let [dy (oscillation now 0 2000 5)]
      (.drawImageRect canvas sprites (Rect/makeXYWH 0 0 64 128) (Rect/makeXYWH 100 (+ 30 dy) 64 128))
      (draw-text-centered canvas "Alice" 132 140))

    ;; Enemy
    (let [sprite (-> now (/ 300) (mod 2) long)
          dy (oscillation now 0 3000 10)]
      (.drawImageRect canvas sprites (Rect/makeXYWH (* sprite 64) 128 64 64) (Rect/makeXYWH 260 (+ 60 dy) 64 64))
      (draw-text-centered canvas "The Rabbit" 292 140))

    ;; particles
    (let [paint (-> (Paint.) (.setColor (color 0x00FFFFFF)))]
      (doseq [idx (range 0 (/ (alength particles) 3))
              :let [x     (aget particles (+ (* idx 3) 0))
                    y     (aget particles (+ (* idx 3) 1))
                    speed (aget particles (+ (* idx 3) 2))]]
        (.setColor paint (with-alpha 0x00FFFFFF (-> speed (/ 15) (* 128) long)))
        (.drawRect canvas (Rect/makeXYWH x y 2 (Math/round (-> speed (* 1)))) paint)))

    ;; walls
    (let [di (-> now (/ 300) long (mod 150))
          dy (-> now (mod 300) (/ 300) (* 64))]
      (doseq [i (range 0 (+ 2 (Math/ceil (/ screen-height 64))))
              :let [sprite (aget rand-wall (mod (+ di i) 150))]]
        (.drawImageRect canvas sprites (Rect/makeXYWH (* sprite 64) 192 64 64) (Rect/makeXYWH 0 (-> i (* 64) (- dy)) 64 64)))
      (.save canvas)
      (.translate canvas screen-width 0)
      (.scale canvas -1 1)
      (doseq [i (range 0 (+ 2 (Math/ceil (/ screen-height 64))))
              :let [sprite (aget rand-wall (mod (+ di i 75) 150))]]
        (.drawImageRect canvas sprites (Rect/makeXYWH (* sprite 64) 192 64 64) (Rect/makeXYWH 0 (-> i (* 64) (- dy)) 64 64)))
      (.restore canvas))
))

(defn draw [canvas window-width window-height]
  (try
    (when-not @*broken
      (draw-impl canvas window-width window-height))
    (catch Exception e
      (reset! *broken true)
      (stacktrace/print-stack-trace (stacktrace/root-cause e)))))

(defn on-key-pressed-impl [state key]
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

    ;; (println key)
    (do
     (println key)
     state)))

(defn on-key-pressed [key]
  (try
    (when-not @*broken
      (swap! *state on-key-pressed-impl key))
    (catch Exception e
      (reset! *broken true)
      (stacktrace/print-stack-trace (stacktrace/root-cause e)))))

(comment
  (do (reset! *state (new-state)) :done)
)