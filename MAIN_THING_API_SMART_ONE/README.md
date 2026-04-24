# Smart Campus Sensor & Room Management API

A JAX-RS RESTful API built with Jersey 2 and Grizzly embedded server. No Spring, no database — pure in-memory data structures.

---

## API Overview

Base URL: `http://localhost:8080/api/v1`

| Resource | Path |
|---|---|
| Discovery | `GET /api/v1` |
| Rooms | `/api/v1/rooms` |
| Sensors | `/api/v1/sensors` |
| Readings | `/api/v1/sensors/{sensorId}/readings` |

---

## How to Build and Run

### Prerequisites
- Java 11+
- Maven 3.6+

### Build
```bash
mvn clean package
```

### Run
```bash
java -jar target/smartcampus-api-1.0-SNAPSHOT.jar
```

The server starts at `http://localhost:8080/api/v1`

---

## Sample curl Commands

### 1. Discovery endpoint
```bash
curl http://localhost:8080/api/v1
```

### 2. Create a room
```bash
curl -X POST http://localhost:8080/api/v1/rooms \
  -H "Content-Type: application/json" \
  -d '{"id":"LIB-301","name":"Library Quiet Study","capacity":40}'
```

### 3. Create a sensor linked to that room
```bash
curl -X POST http://localhost:8080/api/v1/sensors \
  -H "Content-Type: application/json" \
  -d '{"id":"CO2-001","type":"CO2","status":"ACTIVE","currentValue":400,"roomId":"LIB-301"}'
```

### 4. Post a sensor reading
```bash
curl -X POST http://localhost:8080/api/v1/sensors/CO2-001/readings \
  -H "Content-Type: application/json" \
  -d '{"value":520}'
```

### 5. Get all sensors filtered by type
```bash
curl http://localhost:8080/api/v1/sensors?type=CO2
```

### 6. Try to delete a room that still has sensors (expect 409)
```bash
curl -X DELETE http://localhost:8080/api/v1/rooms/LIB-301
```

### 7. Get reading history for a sensor
```bash
curl http://localhost:8080/api/v1/sensors/CO2-001/readings
```

### 8. Try to register a sensor with a non-existent room (expect 422)
```bash
curl -X POST http://localhost:8080/api/v1/sensors \
  -H "Content-Type: application/json" \
  -d '{"id":"TEMP-999","type":"Temperature","status":"ACTIVE","currentValue":0,"roomId":"FAKE-ROOM"}'
```

---

## Report: Answers to Coursework Questions

---

### Part 1 — Setup & Discovery

**Q: Explain the default lifecycle of a JAX-RS Resource class. Is it per-request or singleton?**

By default in JAX-RS, a new instance of a resource class is created for every incoming HTTP request. This is known as the *per-request* lifecycle. The implication for in-memory data management is significant: if you store data in instance fields of the resource class, that data will be lost after each request because a fresh object is created each time. To prevent this, shared state — such as the rooms and sensors maps — must live outside the resource class entirely. In this project, a `DataStore` singleton (using `ConcurrentHashMap`) is used. `ConcurrentHashMap` is chosen over plain `HashMap` because multiple requests can arrive simultaneously, and concurrent writes to a regular `HashMap` can cause data corruption or loss.

**Q: Why is HATEOAS considered a hallmark of advanced RESTful design? How does it benefit client developers?**

HATEOAS (Hypermedia as the Engine of Application State) means that API responses include links to related actions and resources rather than just data. Instead of a client needing to read offline documentation to know that sensors live under `/api/v1/sensors`, the discovery endpoint tells them directly in the response. This reduces coupling between client and server — the server can change its URL structure and clients following links will adapt automatically. It also makes APIs self-documenting and easier to explore, which reduces onboarding time for developers integrating with the API.

---

### Part 2 — Room Management

**Q: What are the implications of returning only IDs versus full room objects in a list?**

Returning only IDs is very lightweight on bandwidth but forces the client to make N additional requests (one per room) to get any useful data — this is known as the N+1 problem. Returning full objects increases payload size but lets the client render a full list in a single round trip. For a campus system where the room list could be large but individual rooms are small objects, returning full objects is the better trade-off. If rooms carried very large nested data, a summary projection (ID + name only) with a link to the full resource would be the ideal middle ground.

**Q: Is the DELETE operation idempotent in your implementation?**

Yes. If a client sends `DELETE /api/v1/rooms/LIB-301` and the room does not exist (either because it never existed or was already deleted), the server returns `204 No Content` — the same response as a successful deletion. The system state after both calls is identical: the room is absent. This satisfies the HTTP idempotency requirement for DELETE. The only case where DELETE is blocked is when the room has sensors assigned, which returns `409 Conflict`. That constraint is consistent regardless of how many times the client retries.

---

### Part 3 — Sensor Operations & Filtering

**Q: What happens if a client sends data in a format other than `application/json` when `@Consumes(APPLICATION_JSON)` is declared?**

JAX-RS will reject the request before it even reaches the resource method. The framework inspects the `Content-Type` header of the incoming request and compares it against the value declared in `@Consumes`. If they do not match — for example, the client sends `text/plain` — JAX-RS returns an HTTP `415 Unsupported Media Type` response automatically. No custom code is needed; the annotation itself acts as a contract enforcement gate at the framework level.

**Q: Why is `@QueryParam` preferred over a path segment like `/sensors/type/CO2` for filtering?**

Path segments are designed to identify a specific resource by identity (e.g., `/sensors/CO2-001`). Filters and search criteria are not part of a resource's identity — they are parameters that narrow down a collection. Using a query parameter (`?type=CO2`) is semantically correct: the base resource is still `/sensors`, and the query parameter refines it. A path-based filter like `/sensors/type/CO2` also creates ambiguity — it could be confused with a sensor whose ID is literally `"type"`. Query parameters are also more extensible: you can add `?type=CO2&status=ACTIVE` without changing the URL structure, whereas nested path filters become unmanageable with multiple criteria.

---

### Part 4 — Sub-Resources

**Q: What are the architectural benefits of the Sub-Resource Locator pattern?**

The Sub-Resource Locator pattern (using a method that returns an object rather than a response) allows you to delegate routing to a separate class. In this project, `SensorResource` handles `/sensors` and `/sensors/{id}`, and hands off `/sensors/{id}/readings` to `SensorReadingResource`. The benefits are: (1) **Separation of concerns** — each class has a single responsibility; (2) **Reduced class size** — a single monolithic resource class handling every nested path would become thousands of lines long and very hard to maintain; (3) **Reusability** — `SensorReadingResource` could in theory be reused or tested independently; (4) **Clarity** — the code structure mirrors the API hierarchy, making it easier for new developers to navigate.

---

### Part 5 — Error Handling & Logging

**Q: Why is HTTP 422 more semantically accurate than 404 when a referenced `roomId` doesn't exist inside a valid JSON payload?**

A `404 Not Found` response traditionally means the URL path itself could not be resolved — the resource at that address doesn't exist. When a client POSTs a sensor with a `roomId` that doesn't exist, the request URL (`/api/v1/sensors`) is perfectly valid and was found by the server. The problem is not with the URL but with the *content* of the request body: it references a foreign key that cannot be resolved. HTTP 422 Unprocessable Entity is designed exactly for this scenario — the request was syntactically correct JSON, the server understood it, but it could not be processed because of a semantic/logical error within the payload. Using 422 gives clients far more precise information about what went wrong.

**Q: From a cybersecurity standpoint, what are the risks of exposing Java stack traces to external API consumers?**

A stack trace reveals: (1) **Technology fingerprinting** — the attacker learns you are using Java, which JAX-RS provider, which version, and what package structure your application has; (2) **Internal path exposure** — file paths and class names can reveal the deployment directory structure; (3) **Logic flow disclosure** — the attacker can see exactly which methods were called and in what order, making it far easier to craft targeted exploits; (4) **Library version intelligence** — specific class names can indicate library versions with known CVEs (Common Vulnerabilities and Exposures), giving the attacker a precise attack vector. The global catch-all `ExceptionMapper<Throwable>` in this project prevents all of this by logging the real error server-side and returning only a generic "unexpected error" message to the client.

**Q: Why use JAX-RS filters for logging rather than inserting `Logger.info()` into every resource method?**

Logging is a cross-cutting concern — it applies uniformly to every endpoint without being part of any endpoint's business logic. Manually inserting logger calls into every method creates: (1) **Code duplication** — the same boilerplate appears dozens of times; (2) **Inconsistency** — a developer adding a new endpoint might forget to add logging; (3) **Mixing of concerns** — business logic and infrastructure code become entangled and harder to read. A single filter class registers once and runs automatically for every request and response, guaranteeing consistent, complete logging with no risk of omission. It can also be enabled or disabled globally in one place.
