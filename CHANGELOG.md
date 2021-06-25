# UNRELEASED

* Updated deps:

```clj
[aleph "0.4.6"] is available but we use "0.4.5-alpha6"
[com.cognitect/transit-cljs "0.8.264"] is available but we use "0.8.243"
[org.clojure/tools.logging "1.1.0"] is available but we use "0.4.0"
[com.cognitect/transit-clj "1.0.324"] is available but we use "0.8.300"
[metosin/jsonista "0.2.7"] is available but we use "0.2.1"
```

# 0.0.9

* Updated deps to most recent versions
* **BREAKING**: `:middlewares` is now `:middleware`
* support transit read & write handlers in both frontend & backend:

```clj
(eines.core/handler-context
   ...
   {:transit {:writer {:handlers {}}
              :reader {:handlers {}}}})
```

```clj
(eines.core/init!
  {:on-connect on-connect
   :on-message on-message
   :on-close on-close
   :on-error on-error
   :transit {:writer {:handlers {}}
             :reader {:handlers {}}}})
```

# 0.0.8

* If the on-message handler is called in I/O thread, dispatch to worker thread.
