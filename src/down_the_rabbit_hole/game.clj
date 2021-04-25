(ns down-the-rabbit-hole.game
  (:require
   [clojure.java.io :as io]
   [clojure.stacktrace :as stacktrace]
   [clojure.string :as str]
   [down-the-rabbit-hole.alice :as alice]
   [down-the-rabbit-hole.decorations :as decorations]
   [down-the-rabbit-hole.core :as core]
   [down-the-rabbit-hole.rabbit :as rabbit])
  (:import
   [java.util Random]
   [org.jetbrains.skija Canvas Color4f Font FontStyle FontMgr Image Paint PaintMode PaintStrokeCap Rect Typeface]))

(def *broken (atom false))

(defn new-state []
    {:objects
      [(decorations/background)
       (alice/->Alice)
       (rabbit/->Rabbit)
       (decorations/particles)
       (decorations/walls)]})

(def *state (atom (new-state)))

(defn on-tick [state now]
  state)

(defn draw-impl [^Canvas canvas window-width window-height]
  (let [now (reset! core/*now (System/currentTimeMillis))
        _   (swap! *state on-tick now)
        {:keys [particles objects]} @*state]

    (.clear canvas (core/color 0xFF280e5b))
    (.scale canvas 3 3)

    (doseq [obj (->> objects
                  (filter #(satisfies? core/IRender %))
                  (sort-by core/-z-index))]
      (core/-render obj canvas now))
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