# appkernel

Minimalistic Clojure/Clojurescript application framework.


## Motivation

1. We want to build multiple applications
1. We want to share code (domain logic and UI)
1. We want to share coding patterns
1. We want our domain logic to be runnable on the server and the client


## Overview

An application consists of modules.

Modules provide user interface components, query handlers and command handlers
and event handlers.

User interface components issue queries and trigger commands.

Command handlers create events.

The appkernel framework provides an API for querying, for command execution
and for definition of modules.


## Documentation

`appkernel` provides a common API for issuing commands and queries. Extensions
provide functions for command execution and query answering.


### State

`appkernel` uses a single data structure for all application state.
Used in a [re-frame](https://github.com/Day8/re-frame) application, this would
be the `app-db`.
Used in a server, it would be a clojure `agent`.


### Queries

A query consists of a name and parameters, while the result is a map containing
the payload and metadata.

```clojure
[:shopping-cart/cart-items {:cart-id "76335E-EC56"}]
=>
{:cart-id "76335E-EC56"
 :cart-items [{:id "23" :title "The Joy of Clojure"
               :id "42" :title "Domain-Driven Design"
 :query/time 1239979303563}]}
```

To add new query capabilities to an application, you have to register query
handlers.
Multiple query handlers can participate in answering a single query.



## License

Copyright © 2018 Witoslaw Koczewski, Artjom Weyand

Distributed under the Eclipse Public License either version 1.0 or any later
version.
