(ns marvbot.core
  (:require [cats.builtin]
            [cats.core :as m]
            [cats.monad.either :as either]
            [marvbot.net :as net]))

(def plugins
  [{:name :logger
    :desc "Log all received message"
    :matcher-fn (fn[m] (println m))
    :handler-fn (fn[_])}

   {:name :pong
    :desc "Respond for PING requests"
    :matcher-fn (fn[m] (re-find #"PING" m))
    :handler-fn (fn[_] ["PONG"])}

   {:name :nick
    :desc "Request for a Nickname"
    :matcher-fn (fn[m] (re-find #"No Ident response" m))
    :handler-fn (fn[_] ["USER marv-bot 0 * : marv-bot" "NICK marv-bot"])}])

(def socket
  (net/connect {:server "irc.freenode.net" :port 6667}))

(def reader
  (net/reader socket))

(def writer
  (net/writer socket))

(defn thank-you [left]
  (println "\n>>> Yoo, tks \\m/ " left))

(defn response [plugin message]
  (let [name (:name plugin)
        handler-fn (:handler-fn plugin)
        result (handler-fn message)]
    (println (str "Plugin: " name " Response: " result))
    (net/send-messages writer result)))

(defn read-and-dispatch []
  (net/read-and-dispatch reader
                         (fn[message]
                           (->> (filter #((:matcher-fn %) message) plugins)
                                (mapv #(response % message))))))

(defn -main []
  (loop [line (read-and-dispatch)]
    (if (either/right? line)
      (recur (read-and-dispatch))
      (thank-you line))))
