# appkernel Architecutre Decision Records

[Documenting Architecture Decisions](http://thinkrelevance.com/blog/2011/11/15/documenting-architecture-decisions)

## ADR-1 Query Handling

### Context

Depending on the query, the answering can differ:

1. Response data is already in memory. A single query handler can provide
   the data instantly.
1. Response data needs to be loaded. The data is not in memory, so it
   has to be loaded. Examples for sources: filesystem, browser store,
   URL, webservice.
1. Multiple result providers. Sometimes there is no single answer, but
   multiple "agents" providing answers. For example a full text search
   query could be answered by multiple independent modules providing
   matches for their entities.

The appkernel query API needs to support synchronous and asynchronous
requests.
