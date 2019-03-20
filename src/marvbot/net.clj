(ns marvbot.net
  (:require [cats.builtin]
            [cats.core :as m]
            [cats.monad.either :as either]
            [clj-sockets.core :as socket])
  (:import java.io.BufferedReader
           java.io.InputStreamReader
           java.io.PrintWriter
           java.net.Socket))

(defn connect [{server :server port :port}]
  (either/try-either (new Socket server port)))

(defn- socket-input->buffered-reader [socket]
  (->> (.getInputStream socket)
       (new InputStreamReader)
       (new BufferedReader)))

(defn- buffered-reader->message [reader callback]
  (let [content (.readLine reader)]
    (if (not (nil? content))
      (do
        (callback content)
        (either/right content))
      (either/left content))))

(defn reader [socket]
  (either/branch-right socket #(either/try-either
                                (socket-input->buffered-reader %))))

(defn read-and-dispatch [reader callback]
  (either/branch-right reader #(buffered-reader->message % callback)))

(defn writer [socket]
  (either/branch-right socket #(either/try-either
                                (->> (.getOutputStream %)
                                     (new PrintWriter)))))

(defn send-messages [writer messages]
  (either/branch-right writer #(either/try-either
                                (do (mapv (fn[m] (.println % m)) messages)
                                    (.flush %)))))
