# appkernel Architecutre Decision Records

[Documenting Architecture Decisions](http://thinkrelevance.com/blog/2011/11/15/documenting-architecture-decisions)

## ADR-1 Query Handling

### Context

Depending on the query, the query handling must differ:

1. Response data is already in memory. A single query handler can provide
   the data instantly.
1. Response data needs to be loaded. The data is not in memory, so it
   has to be loaded. Examples for sources: filesystem, browser store,
   URL, webservice.
1. Multiple result providers. Sometimes there is no single answer, but
   multiple "agents" providing answers. For example a full text search
   query could be answered by multiple independent modules providing
   matches for their entities.

Depending on the issuer of a query, the query handling must differ:

1. The user interface must get a response instantly. When an asynchronous
   query handler is involved, an intermediate response is required.
1. An already asynchronous process (like an event handler which updates
   some projection) can wait for the response. To simplify implementation
   of such an event handler, we may want synchronous query handling
   even if asynchronous query handlers are involved.

