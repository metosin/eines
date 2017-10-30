# UNRELEASED

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
