(ns clopack-native.core
  (:gen-class))

(require '[net.n01se.clojure-jna :as jna])

(defn- dev-open [interface]
  (jna/invoke Integer clopack_native/dev_open interface))

(defn- dev-read [dev buf buf-len]
  (jna/invoke Integer clopack_native/dev_read dev buf buf-len))

(defn- dev-write [dev buf buf-len]
  (jna/invoke Integer clopack_native/dev_write dev buf buf-len))

(defn- dev-close [dev]
  (jna/invoke Integer clopack_native/dev_close dev))

(defn- dev-buf-len []
  (jna/invoke Integer clopack_native/dev_buf_len))

(defn create-context [interface]
  "Creates a new packet context with the given interface.
   Returns a packet context on success, nil on errors."
  (let [dev     (dev-open interface)
        buf-len (dev-buf-len)]
    (when-not (= dev -1)
      {:dev     dev
       :buf-len buf-len})))

(defn read-frame [ctx]
  "Reads a data frame as from a packet context.
   Returns a byte-array on success, nil otherwise"
  (let [dev       (:dev ctx)
        buf-len   (:buf-len ctx)
        buf       (byte-array buf-len)
        num-read  (dev-read dev buf buf-len)]
    (when-not (= num-read -1)
      (take num-read buf))))

(defn write-frame [ctx buf]
  "Writes the given byte-array as a data frame through a packet context.
   Returns the number of bytes written on success, nil otherwise."
  (let [dev         (:dev ctx)
        buf-len     (count buf)
        num-written (dev-write dev buf buf-len)]
    (when-not (= num-written -1)
      num-written)))

(defn destroy-context [ctx]
  "Destroys a packet context."
  (dev-close (:dev ctx))
  nil)
