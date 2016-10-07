(ns clopack-native.core
  (:gen-class))

(require '[net.n01se.clojure-jna :as jna])

(defn- dev-open [interface]
  (jna/invoke Integer clopack_native/dev_open interface))

(defn- dev-read [handle buf buf-len]
  (jna/invoke Integer clopack_native/dev_read handle buf buf-len))

(defn- dev-write [handle buf buf-len]
  (jna/invoke Integer clopack_native/dev_write handle buf buf-len))

(defn- dev-close [handle]
  (jna/invoke Integer clopack_native/dev_close handle))

(defn- dev-buf-len []
  (jna/invoke Integer clopack_native/dev_buf_len))

(defn create-context [interface]
  "Creates a new packet context with the given interface.
   Returns a packet context on success, nil on errors."
  (let [handle  (dev-open interface)
        buf-len (dev-buf-len)]
    (when-not (= handle -1)
      {:handle  handle
       :buf-len buf-len})))

(defn read-frame [ctx]
  "Reads a data frame as from a packet context.
   Returns a byte-array on success, nil otherwise"
  (let [handle    (:handle ctx)
        buf-len   (:buf-len ctx)
        buf       (byte-array buf-len)
        num-read  (dev-read handle buf buf-len)]
    (when-not (= num-read -1)
      (take num-read buf))))

(defn write-frame [ctx buf]
  "Writes the given byte-array as a data frame through a packet context.
   Returns the number of bytes written on success, nil otherwise."
  (let [handle      (:handle ctx)
        buf-len     (count buf)
        num-written (dev-write handle buf buf-len)]
    (when-not (= num-written -1)
      num-written)))

(defn destroy-context [ctx]
  "Destroys a packet context."
  (dev-close (:handle ctx))
  nil)
