(ns down-the-rabbit-hole.decorations
  (:require
   [down-the-rabbit-hole.core :as core])
  (:import
   [org.jetbrains.skija Canvas Paint Rect Shader]))

(def bg-height-tiles (* core/screen-height-tiles 4))

(defmethod core/render :renderer/background [canvas db game entity]
  (let [tiles (:background/tiles entity)
        dy (-> (:game/now game) (mod (* 64 bg-height-tiles 900)) (/ 900) (* 64) long)]
    (doseq [y (range (Math/floor (/ dy 64)) (+ (Math/floor (/ dy 64)) core/screen-height-tiles 1))
            x (range 0 core/screen-width-tiles)
            :let [sprite (aget tiles (-> y (* core/screen-width-tiles) (+ x) (mod (alength tiles))))]]
      (core/draw-sprite canvas (* sprite 64) 256 64 64 (* x 64) (- (* y 64) dy)))
    (with-open [paint  (Paint.)
                shader (Shader/makeLinearGradient (float 0) (float 0) (float core/screen-width) (float 0)
                         (int-array [(core/color 0x80000000) (core/color 0x00000000) (core/color 0x00000000) (core/color 0x80000000)])
                         (float-array [0.0 0.4 0.6 1.0]))]
      (.setShader paint shader)
      (.drawRect canvas (Rect/makeXYWH 0 0 core/screen-width core/screen-height) paint))))

(defmethod core/render :renderer/particles [canvas db game entity]
  (let [{:particles/keys [xs speeds phases]} entity]
    (with-open [paint (Paint.)]
      (doseq [i (range 0 (alength xs))
              :let [x     (aget xs i)
                    phase (aget phases i)
                    speed (aget speeds i)
                    period (/ 1000 (+ 0.2 (* 0.6 speed)))
                    y     (-> (:game/now game) (+ phase) (mod period) (/ period) (* -1 core/screen-height) (+ core/screen-height))]]
        (.setColor paint (core/color 0xFFFFFF (-> speed (* 64) long)))
        (.drawRect canvas (Rect/makeXYWH x y 2 (* 4 speed)) paint)))))
  
(defmethod core/render :renderer/walls [canvas db game entity]
  (let [{:walls/keys [left right]} entity]
      (let [dy (-> (:game/now game) (mod (* 64 bg-height-tiles 300)) (/ 300) (* 64) long)]
        (doseq [y (range (Math/floor (/ dy 64)) (+ (Math/floor (/ dy 64)) core/screen-height-tiles 1))
                :let [sprite (aget left (mod y bg-height-tiles))]]
          (core/draw-sprite canvas (* sprite 64) 192 64 64 0 (- (* y 64) dy)))

        (.save canvas)
        (.translate canvas core/screen-width 0)
        (.scale canvas -1 1)
        (doseq [y (range (Math/floor (/ dy 64)) (+ (Math/floor (/ dy 64)) core/screen-height-tiles 1))
                :let [sprite (aget right (mod y bg-height-tiles))]]
          (core/draw-sprite canvas (* sprite 64) 192 64 64 0 (- (* y 64) dy)))
        (.restore canvas))))

(defn decorations-tx []
  (let [rand-bg-tile #(if (<= (rand) 0.85) 0 (+ 1 (rand-int 8)))
        rand-wall-tile #(if (<= (rand) 0.3) 0 (+ 1 (rand-int 6)))]
    [{:renderer :renderer/background
      :z-index  100
      :background/tiles (int-array (repeatedly (* core/screen-width-tiles bg-height-tiles) rand-bg-tile))}
     {:renderer :renderer/particles
      :z-index  150
      :particles/xs     (int-array (repeatedly 100 #(rand-int core/screen-width)))
      :particles/phases (int-array (repeatedly 100 #(rand-int 1000)))
      :particles/speeds (float-array (repeatedly 100 rand))}
     {:renderer :renderer/walls
      :z-index  1000
      :walls/left (int-array (repeatedly bg-height-tiles rand-wall-tile))
      :walls/right (int-array (repeatedly bg-height-tiles rand-wall-tile))}]))
