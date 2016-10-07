(ns clopack-native.core)

(require '[net.n01se.clojure-jna :as jna])

; Refer to 'clopack_native' as 'cn'.
(jna/to-ns cn clopack_native)

(defn create-ctx [interface]
  "Creates a new packet context with the given interface.
   Returns a packet context on success, nil on errors."
  (let [handle  jna/invoke Integer cn/dev_open interface
        buf-len jna/invoke Integer cn/dev_buf_len]
    (when-not (= handle -1)
      {:handle  handle
       :buf-len buf-len})))

(defn read-frame [ctx]
  "Reads a data frame as from a packet context.
   Returns a byte-array on success, nil otherwise"
  (let [handle    (:handle ctx)
        buf-len   (:buf-len ctx)
        buf       (byte-array buf-len)
        num-read  (jna/invoke Integer cn/dev_read handle buf buf-len)]
    (when-not (= num-read -1)
      (take num-read buf))))

(defn write-frame [ctx buf]
  "Writes the given byte-array as a data frame through a packet context.
   Returns the number of bytes written on success, nil otherwise."
  (let [handle      (:handle ctx)
        buf-len     (count buf)
        num-written (jna/invoke Integer cn/dev_write handle buf buf-len)]
    (when-not (= num-written -1)
      num-written)))

(defn destroy-ctx [ctx]
  "Destroys a packet context."
  (jna/invoke Integer cn/dev_close (:handle ctx))
  nil)
