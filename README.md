# SmartClass_API_W2100255
---

### Author:Shaveen Fernando
### ID:W2100255

---

---

## Overview of the API 





---


---

## How to Build it and to Run it.




---

---
## Sample Curl Commands






---





---

## Report: Answers to Listed Questions in the Coursework 

---
### Part 1 (Setup & Discovery)

**Q1: In your report, explain the default lifecycle of a JAX-RS Resource class. Is a
new instance instantiated for every incoming request, or does the runtime treat it as a
singleton? Elaborate on how this architectural decision impacts the way you manage and
synchronize your in-memory data structures (maps/lists) to prevent data loss or race con-
ditions.**







**Q2: Why is the provision of ”Hypermedia” (links and navigation within responses)
considered a hallmark of advanced RESTful design (HATEOAS)? How does this approach
benefit client developers compared to static documentation?**


### Part 2 (Room Management)

**Q1: When returning a list of rooms, what are the implications of returning only
IDs versus returning the full room objects? Consider network bandwidth and client side
processing**






**Q2:Is the DELETE operation idempotent in your implementation? Provide a detailed
justification by describing what happens if a client mistakenly sends the exact same DELETE
request for a room multiple times.**







### Part 3 (Sensors & Filtering)


**Q1; We explicitly use the @Consumes (MediaType.APPLICATION_JSON) annotation on
the POST method. Explain the technical consequences if a client attempts to send data in
a different format, such as text/plain or application/xml. How does JAX-RS handle this
mismatch?**






**Q2: You implemented this filtering using @QueryParam. Contrast this with an alterna-
tive design where the type is part of the URL path (e.g., /api/vl/sensors/type/CO2). Why
is the query parameter approach generally considered superior for filtering and searching
collections?**




### Part 4 (Sub - Resources)


**Q1:Discuss the architectural benefits of the Sub-Resource Locator pattern. How
does delegating logic to separate classes help manage complexity in large APIs compared
to defining every nested path (e.g., sensors/{id}/readings/{rid}) in one massive con-
troller class?**



### Part 5 (Error Handling & Logging)

**Q1:Why is HTTP 422 often considered more semantically accurate than a standard
404 when the issue is a missing reference inside a valid JSON payload?**




**Q2: From a cybersecurity standpoint, explain the risks associated with exposing
internal Java stack traces to external API consumers. What specific information could an
attacker gather from such a trace?**




**Q3: Why is it advantageous to use JAX-RS filters for cross-cutting concerns like
logging, rather than manually inserting Logger.info() statements inside every single re-
source method?**



