# SmartClass_API_W2100255
---

### Author:Shaveen Fernando
### ID:W2100255

## Overview of the API 
Base URL: `http://localhost:8080/api/v1`
Rooms URL:`http://localhost:8080/api/v1/rooms`
Sensors URL:`http://localhost:8080/api/v1/sensors`


## Sample Curl Commands

### 1. Discovery endpoint
```bash
curl
http://localhost:8080/api/v1
```

### 2. Create a room
```bash
curl
 POST http://localhost:8080/api/v1/rooms \
  -d '{"id":"LIB-001","name":"Library","capacity":30}'
```

### 3. Get all sensors filtered by type
```bash
curl http://localhost:8080/api/v1/sensors?type=CO2
```

### 4. Get reading history for a sensor
```bash
curl http://localhost:8080/api/v1/sensors/CO2-001/readings
```

### 5. Create a sensor linked to that room
```bash
curl
POST http://localhost:8080/api/v1/sensors 
   '{"id":"CO2-001","type":"CO2","status":"ACTIVE","currentValue":400,"roomId":"LIB-301"}'
```


---

## Report: Answers to Listed Questions in the Coursework 

---
### Part 1 (Setup & Discovery)

**Q1: In your report, explain the default lifecycle of a JAX-RS Resource class. Is a
new instance instantiated for every incoming request, or does the runtime treat it as a
singleton? Elaborate on how this architectural decision impacts the way you manage and
synchronize your in-memory data structures (maps/lists) to prevent data loss or race con-
ditions.**

JAX-RS, acts and  behaves like a temporary worker that gets created for a single request and then disappears, so if any data is stored inside it is lost once the request finishes,its like writing notes on paper and throwing them away. To keep data such as rooms or sensors, you need a permanent place like a DataStore singleton, which acts like a shared storage that stays alive across all requests. Since multiple requests can try to access or update this data at the same time, using a ConcurrentHashMap ensures everything stays safe and consistent, allowing concurrent access without data loss or conflicts.



**Q2: Why is the provision of ”Hypermedia” (links and navigation within responses)
considered a hallmark of advanced RESTful design (HATEOAS)? How does this approach
benefit client developers compared to static documentation?**

HATEOAS (Hypermedia as the Engine of Application State) means that API responses include links to related actions and resources rather than just data. Instead of a client needing to read offline documentation to know that sensors live under `/api/v1/sensors`, the discovery endpoint tells them directly in the response. This reduces coupling between client and server — the server can change its URL structure and clients following links will adapt automatically. It also makes APIs self-documenting and easier to explore, which reduces onboarding time for developers integrating with the API.


### Part 2 (Room Management)

**Q1: When returning a list of rooms, what are the implications of returning only
IDs versus returning the full room objects? Consider network bandwidth and client side
processing**


Returning only IDs keeps things very lightweight, but it forces the client to make extra requests for each room to get actual details—this is the classic N+1 problem and can slow things down. On the other hand, returning full room objects makes the response a bit bigger, but the client gets everything it needs in one go, which is usually more efficient overall. For something like a campus system where you might have many rooms but each one is relatively small, sending full objects is the better choice. If the room data ever becomes large or complex, a good middle ground is to return just basic info (like ID and name) along with a link to fetch the full details when needed


**Q2:Is the DELETE operation idempotent in your implementation? Provide a detailed
justification by describing what happens if a client mistakenly sends the exact same DELETE
request for a room multiple times.**

If a client sends `DELETE /api/v1/rooms/LIB-301` and the room does not exist (either because it never existed or was already deleted), the server returns `204 No Content` — the same response as a successful deletion. The system state after both calls is identical: the room is absent. This satisfies the HTTP idempotency requirement for DELETE. The only case where DELETE is blocked is when the room has sensors assigned, which returns `409 Conflict`. That constraint is consistent regardless of how many times the client retries.



### Part 3 (Sensors & Filtering)


**Q1; We explicitly use the @Consumes (MediaType.APPLICATION_JSON) annotation on
the POST method. Explain the technical consequences if a client attempts to send data in
a different format, such as text/plain or application/xml. How does JAX-RS handle this
mismatch?**

In JAX-RS, the framework acts like a strict gatekeeper: before your request even reaches your resource method, it checks the `Content-Type` header and compares it with what you’ve defined in `@Consumes`. If they don’t match—say the client sends `text/plain` instead of JSON—the request is rejected immediately with an HTTP 415 (Unsupported Media Type) error. This all happens automatically, so you don’t need to write any extra validation code; the annotation itself enforces the rules at the framework level.


**Q2: You implemented this filtering using @QueryParam. Contrast this with an alterna-
tive design where the type is part of the URL path (e.g., /api/vl/sensors/type/CO2). Why
is the query parameter approach generally considered superior for filtering and searching
collections?**


Path segments are meant to point to a specific resource by its identity `(like /sensors/CO2-001)`, while filters are just ways to narrow down a list, not identify a single item. That’s why using query parameters like `?type=CO2` makes more sense—the main resource is still `/sensors`, and the query just refines the results. If you tried something like /sensors/type/CO2, it can get confusing, since `"type" `might be mistaken as part of an actual sensor ID. Query parameters are also much more flexible, because you can easily add more filters like `?type=CO2&status=ACTIVE` without messing up the URL structure, whereas path-based filtering quickly becomes messy and hard to manage.

### Part 4 (Sub - Resources)


**Q1:Discuss the architectural benefits of the Sub-Resource Locator pattern. How
does delegating logic to separate classes help manage complexity in large APIs compared
to defining every nested path (e.g., sensors/{id}/readings/{rid}) in one massive con-
troller class?**


### Part 5 (Error Handling & Logging)

**Q1:Why is HTTP 422 often considered more semantically accurate than a standard
404 when the issue is a missing reference inside a valid JSON payload?**


`404 Not Found` response traditionally means the URL path itself could not be resolved — the resource at that address doesn't exist. When a client POSTs a sensor with a `roomId` that doesn't exist, the request URL (`/api/v1/sensors`) is perfectly valid and was found by the server. The problem is not with the URL but with the *content* of the request body: it references a foreign key that cannot be resolved. HTTP 422 Unprocessable Entity is designed exactly for this scenario — the request was syntactically correct JSON, the server understood it, but it could not be processed because of a semantic/logical error within the payload. Using 422 gives clients far more precise information about what went wrong.

**Q2: From a cybersecurity standpoint, explain the risks associated with exposing
internal Java stack traces to external API consumers. What specific information could an
attacker gather from such a trace?**

stack trace reveals: (1) **Technology fingerprinting** — the attacker learns you are using Java, which JAX-RS provider, which version, and what package structure your application has; (2) **Internal path exposure** — file paths and class names can reveal the deployment directory structure; (3) **Logic flow disclosure** — the attacker can see exactly which methods were called and in what order, making it far easier to craft targeted exploits; (4) **Library version intelligence** — specific class names can indicate library versions with known CVEs (Common Vulnerabilities and Exposures), giving the attacker a precise attack vector. The global catch-all `ExceptionMapper<Throwable>` in this project prevents all of this by logging the real error server-side and returning only a generic "unexpected error" message to the client.


**Q3: Why is it advantageous to use JAX-RS filters for cross-cutting concerns like
logging, rather than manually inserting Logger.info() statements inside every single re-
source method?**

Logging is something every endpoint needs, but it’s not actually part of what those endpoints are supposed to do. If you add logging manually in every method, you end up repeating the same code everywhere, risking inconsistency if someone forgets to include it, and cluttering your business logic with extra noise. Using a single filter solves all of that—it runs automatically for every request and response, keeps logging consistent across the whole app, and can be turned on or off from one place without touching individual endpoints

