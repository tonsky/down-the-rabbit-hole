(ns down-the-rabbit-hole.main
  (:require
   [clojure.stacktrace :as stacktrace]
   [down-the-rabbit-hole.core :as core]
   [down-the-rabbit-hole.game :as game])
  (:import
   [org.jetbrains.skija BackendRenderTarget Canvas ColorSpace DirectContext FramebufferFormat Paint Rect Surface SurfaceColorFormat SurfaceOrigin]
   [org.lwjgl.glfw Callbacks GLFW GLFWErrorCallback GLFWKeyCallbackI GLFWCursorPosCallbackI GLFWMouseButtonCallbackI]
   [org.lwjgl.opengl GL GL11]
   [org.lwjgl.system MemoryUtil]))

(set! *warn-on-reflection* true)

(defonce *running (atom true))

(defmacro safe-call [& call]
  `(try
     (when @*running
       (~@call))
     (catch Throwable e#
       (reset! *running false)
       (stacktrace/print-stack-trace (stacktrace/root-cause e#)))))

(defn display-scale [window]
  (let [x (make-array Float/TYPE 1)
        y (make-array Float/TYPE 1)]
    (GLFW/glfwGetWindowContentScale window x y)
    [(first x) (first y)]))

(defn -main [& args]
  (.set (GLFWErrorCallback/createPrint System/err))
  (GLFW/glfwInit)
  (GLFW/glfwWindowHint GLFW/GLFW_VISIBLE GLFW/GLFW_FALSE)
  (GLFW/glfwWindowHint GLFW/GLFW_RESIZABLE GLFW/GLFW_TRUE)
  (let [width 1280
        height 720
        window (GLFW/glfwCreateWindow width height "Down the Rabbit Hole" MemoryUtil/NULL MemoryUtil/NULL)]
    (GLFW/glfwMakeContextCurrent window)
    (GLFW/glfwSwapInterval 1)
    (GLFW/glfwShowWindow window)  
    (GL/createCapabilities)

    (doto (Thread. #(clojure.main/main))
      (.start))

    (GLFW/glfwSetKeyCallback window
      (reify GLFWKeyCallbackI
        (invoke [this _ key _ action mods]
          (condp = action
            GLFW/GLFW_PRESS   (safe-call #'game/on-key-press key true mods)
            GLFW/GLFW_REPEAT  (safe-call #'game/on-key-press key true mods)
            GLFW/GLFW_RELEASE (safe-call #'game/on-key-press key false mods)))))

    (GLFW/glfwSetCursorPosCallback window
      (reify GLFWCursorPosCallbackI
        (invoke [this _ xpos ypos]
          (safe-call #'game/on-mouse-move xpos ypos))))

    (GLFW/glfwSetMouseButtonCallback window
      (reify GLFWMouseButtonCallbackI
        (invoke [this _ button action mods]
          (condp = action
            GLFW/GLFW_PRESS   (safe-call #'game/on-mouse-click button true mods)
            GLFW/GLFW_REPEAT  (safe-call #'game/on-mouse-click button true mods)
            GLFW/GLFW_RELEASE (safe-call #'game/on-mouse-click button false mods)))))

    (let [context (DirectContext/makeGL)
          fb-id   (GL11/glGetInteger 0x8CA6)
          [scale-x scale-y] (display-scale window)
          target  (BackendRenderTarget/makeGL (* scale-x width) (* scale-y height) 0 8 fb-id FramebufferFormat/GR_GL_RGBA8)
          surface (Surface/makeFromBackendRenderTarget context target SurfaceOrigin/BOTTOM_LEFT SurfaceColorFormat/RGBA_8888 (ColorSpace/getSRGB))
          canvas  (.getCanvas surface)]
      (.scale canvas scale-x scale-y)
      (safe-call #'game/start!)
      (loop []
        (when (not (GLFW/glfwWindowShouldClose window))
          (let [layer (.save canvas)]
            (.clear canvas (core/color 0xFF140043))
            (safe-call #'game/draw canvas)
            (.restoreToCount canvas layer))
          (.flush context)
          (GLFW/glfwSwapBuffers window)
          (GLFW/glfwPollEvents)
          (recur)))

      (Callbacks/glfwFreeCallbacks window)
      (GLFW/glfwHideWindow window)
      (GLFW/glfwDestroyWindow window)
      (GLFW/glfwPollEvents)

      (.close surface)
      (.close target)
      (.close context)

      (GLFW/glfwTerminate)
      (.free (GLFW/glfwSetErrorCallback nil))
      (shutdown-agents)
)))
