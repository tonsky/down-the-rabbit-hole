(ns down-the-rabbit-hole.core
  (:refer-clojure :exclude [find])
  (:require
   [clojure.java.io :as io]
   [clojure.stacktrace :as stacktrace]
   [clojure.string :as str]
   [datascript.core :as ds])
  (:import
   [java.io ByteArrayOutputStream]
   [java.util Random]
   [org.jetbrains.skija Canvas Color4f Font FontStyle FontMgr Image Paint PaintMode PaintStrokeCap Rect Typeface]))

(def screen-width 426)
(def screen-width-tiles 7)
(def screen-height 240)
(def screen-height-tiles 4)

(def schema
  {:role {:db/index true}
   :item/owner {:db/valueType :db.type/ref}})

(def *db (atom nil))

(defn slurp-bytes [x]
  (with-open [is (io/input-stream x)
              os (ByteArrayOutputStream.)]
    (io/copy is os)
    (.toByteArray os)))

(def sprites
  (Image/makeFromEncoded (slurp-bytes (io/resource "sprites.png"))))

(defn color
  ([^long l]
   (.intValue (Long/valueOf l)))
  ([^long l ^long alpha]
   (color
     (bit-or
       (-> (bit-and l 0x00FFFFFF))
       (-> (bit-and alpha 0xFF) (bit-shift-left 24))))))

(defn in? [x xs]
  (some #(when (= % x) true) xs))

(defn find [pred xs]
  (some #(if (pred %) %) xs))

(defmacro cond+ [& clauses]
  (when-some [[test expr & rest] clauses]
    (case test
      :let `(let ~expr (cond+ ~@rest))
      `(if ~test ~expr (cond+ ~@rest)))))

(defn oscillation [now phase period amplitude]
  (-> now (mod period) (/ period) (+ phase) (* 2 Math/PI) (Math/cos) (* amplitude) (Math/round)))

(defn draw-sprite [canvas sx sy sw sh x y]
  (.drawImageRect canvas sprites (Rect/makeXYWH sx sy sw sh) (Rect/makeXYWH x y sw sh))
  sw)

(defn letter-width [ch]
  (cond
    (< (int ch) 128) 8
    (= ch \⚔) 8
    :else 10))

(defn draw-letter [canvas ch x y]
  (cond
    (= ch \♥)
    (draw-sprite canvas 0 320 10 10 x (- y 1))
    (= ch \⚡)
    (draw-sprite canvas 16 320 10 10 x (- y 1))
    (= ch \⚔)
    (draw-sprite canvas 32 320 9 10 x (- y 1))
    (= ch \♢)
    (draw-sprite canvas 48 320 10 10 x (- y 1))
    :else
    (let [sx (-> ch (int) (mod 16) (* 8))
          sy (-> ch (int) (quot 16) (* 8) (+ 320))]
      (draw-sprite canvas sx sy 8 8 x y))))

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

      :else
      (do
        (draw-letter canvas ch x y)
        (recur (inc i) (+ x (letter-width ch)) y)))))

(defn draw-text-centered [canvas ^String s center y0]
  (let [lines (str/split-lines s)]
    (.save canvas)
    (.translate canvas 0 y0)
    (doseq [line lines
            :let [width (reduce + 0 (map letter-width line))]]
      (loop [i 0
             x (- center (quot width 2))]
        (when (< i (count line))
          (let [ch (.charAt line i)]
            (draw-letter canvas ch x 0)
            (recur (inc i) (+ x (letter-width ch))))))
      (.translate canvas 0 12))
    (.restore canvas)))

(defn in-rect? [x y ^Rect rect]
  (and
    (>= x (.getLeft rect))
    (<= x (.getRight rect))
    (>= y (.getTop rect))
    (<= y (.getBottom rect))))

(defn entities [db index & fragments]
  (map #(ds/entity db (:e %)) (apply ds/datoms db index fragments)))

(defmulti render (fn [canvas db game entity] (:renderer entity)))

(defn lerp [from to ratio]
  (-> (- to from)
    (* ratio)
    (+ from)))

(defn game [db]
  (ds/entity db 1))

(defn in-phase? [game & phases]
  (in? (:game/phase game) phases))

(defn lerp-phase [game from to]
  (let [ratio (-> (- (:game/now game) (:game/phase-started game))
                (/ (:game/phase-length game)))]
    (lerp from to ratio)))