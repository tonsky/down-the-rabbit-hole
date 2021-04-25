(ns down-the-rabbit-hole.core
  (:require
   [clojure.java.io :as io]
   [clojure.stacktrace :as stacktrace]
   [clojure.string :as str])
  (:import
   [java.io ByteArrayOutputStream]
   [java.util Random]
   [org.jetbrains.skija Canvas Color4f Font FontStyle FontMgr Image Paint PaintMode PaintStrokeCap Rect Typeface]))

(def screen-width 426)
(def screen-width-tiles 7)
(def screen-height 240)
(def screen-height-tiles 4)

(def *now (atom (System/currentTimeMillis)))

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

(defn oscillation [now phase period amplitude]
  (-> now (mod period) (/ period) (+ phase) (* 2 Math/PI) (Math/cos) (* amplitude) (Math/round)))

(defn draw-sprite [canvas sx sy sw sh x y]
  (.drawImageRect canvas sprites (Rect/makeXYWH sx sy sw sh) (Rect/makeXYWH x y sw sh)))

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
        (draw-sprite canvas sx sy 8 8 x y)
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
        (draw-sprite canvas sx sy 8 8 (- (* x 8) (quot width 2)) 0))
      (.translate canvas 0 12))
    (.restore canvas)))

(defprotocol IRender
  (-render [this canvas now])
  (-z-index [this]))

(defprotocol IHover
  (-bounds [this])
  (-on-hover [this])
  (-on-leave [this]))