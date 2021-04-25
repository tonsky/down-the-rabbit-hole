(ns down-the-rabbit-hole.decorations
  (:require
   [down-the-rabbit-hole.core :as core])
  (:import
   [org.jetbrains.skija Canvas Paint Rect]))

(def bg-height-tiles (* core/screen-height-tiles 4))

(defrecord Background [id arr]
  core/IRenderable
  (-render [this canvas now]
    (let [now @core/*now]
      (let [dy (-> now (mod (* 64 bg-height-tiles 900)) (/ 900) (* 64) long)]
        (doseq [y (range (Math/floor (/ dy 64)) (+ (Math/floor (/ dy 64)) core/screen-height-tiles 1))
                x (range 0 core/screen-width-tiles)
                :let [sprite (aget arr (-> y (* core/screen-width-tiles) (+ x) (mod (alength arr))))]]
          (core/draw-sprite canvas (* sprite 64) 256 64 64 (* x 64) (- (* y 64) dy))))))
  (-z-index [this] 100))

(defn background []
  (->Background (core/next-id) (int-array (repeatedly (* core/screen-width-tiles bg-height-tiles) #(rand-int 4)))))

(defrecord Particles [id xs phases speeds]
  core/IRenderable
  (-render [this canvas now]
    (let [now @core/*now]
      (with-open [paint (Paint.)]
        (doseq [i (range 0 (alength xs))
                :let [x     (aget xs i)
                      phase (aget phases i)
                      speed (aget speeds i)
                      period (/ 1000 (+ 1 speed))
                      y     (-> now (+ phase) (mod period) (/ period) (* -1 core/screen-height) (+ core/screen-height))]]
          (.setColor paint (core/with-alpha 0x00FFFFFF (-> speed (* 64) long)))
          (.drawRect canvas (Rect/makeXYWH x y 2 (Math/round (* speed 10))) paint)))))
  (-z-index [this] 900))

(defn particles []
  (->Particles
    (core/next-id)
    (int-array (repeatedly 30 #(rand-int core/screen-width)))
    (int-array (repeatedly 30 #(rand-int 1000)))
    (float-array (repeatedly 30 rand))))

(defrecord Walls [id left-wall right-wall]
  core/IRenderable
  (-render [this canvas now]
    (let [now @core/*now]
      (let [dy (-> now (mod (* 64 bg-height-tiles 300)) (/ 300) (* 64) long)]
        (doseq [y (range (Math/floor (/ dy 64)) (+ (Math/floor (/ dy 64)) core/screen-height-tiles 1))
                :let [sprite (aget left-wall (mod y bg-height-tiles))]]
          (core/draw-sprite canvas (* sprite 64) 192 64 64 0 (- (* y 64) dy)))

        (.save canvas)
        (.translate canvas core/screen-width 0)
        (.scale canvas -1 1)
        (doseq [y (range (Math/floor (/ dy 64)) (+ (Math/floor (/ dy 64)) core/screen-height-tiles 1))
                :let [sprite (aget right-wall (mod y bg-height-tiles))]]
          (core/draw-sprite canvas (* sprite 64) 192 64 64 0 (- (* y 64) dy)))
        (.restore canvas))))
  (-z-index [this] 1000))

(defn walls []
  (->Walls
    (core/next-id)
    (int-array (repeatedly bg-height-tiles #(rand-int 2)))
    (int-array (repeatedly bg-height-tiles #(rand-int 2)))))
