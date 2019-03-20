(ns marvbot.plugin
  (:require [cats.builtin]
            [cats.core :as m]
            [cats.monad.either :as either]
            [marvbot.net :as net]))




(def registered-plugins [])
