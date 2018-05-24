# Eines - Simple Clojure and ClojureScript library for WebSocket communication

> Eines, Finnish for Convenience food
>
> Convenience food, or tertiary processed food, is food that is 
> commercially prepared (often through processing) to optimise 
> ease of consumption.
>
> https://en.wikipedia.org/wiki/Convenience_food

Simple [Clojure](http://clojure.org) and [ClojureScript](https://clojurescript.org) library 
for [WebSocket](https://developer.mozilla.org/en-US/docs/Web/API/WebSocket) communication. 

## Goals

* Simple, batteries included library
* Support for Transit, JSON, and EDN message formats
* Support rpc style request/response from client to server, and from server to client
* Extensible via middleware
* Expose socket lifecycle to client

## Todo

- [ ] Write proper documentation
- [ ] API docs
- [ ] Compare to other libs
- [x] Symmetric RPC support
- [x] Better state machine for front
- [ ] Support for HttpKit
- [ ] Release

## License

Copyright © 2017-2018 Metosin Ltd.

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
