(ns user
  (:require
    [down-the-rabbit-hole.game :as game]
    [down-the-rabbit-hole.main :as main]))

(defn refresh []
  (reset! main/*running false)
  (require
    'down-the-rabbit-hole.core
    'down-the-rabbit-hole.decorations
    'down-the-rabbit-hole.alice
    'down-the-rabbit-hole.rabbit
    'down-the-rabbit-hole.item
    'down-the-rabbit-hole.end-turn
    'down-the-rabbit-hole.phases
    'down-the-rabbit-hole.game
    :reload)
  (game/start!)
  (reset! down-the-rabbit-hole.main/*running true))