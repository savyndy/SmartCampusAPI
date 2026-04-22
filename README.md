Smart Campus Sensor & Room Management API

### Overview
The "Smart Campus" Sensor & Room Management API is a high-performance RESTful web service designed to manage a university's campus-wide infrastructure, handling thousands of rooms and diverse arrays of sensors (such as CO2 monitors, occupancy trackers, and smart lighting controllers).

Built strictly using **JAX-RS (Jakarta RESTful Web Services)** without the use of frameworks like Spring Boot, this API provides a seamless interface for campus facilities managers and automated building systems to interact with sensor telemetry and room metadata.

Key Architectural Features:
* Thread-Safe In-Memory Data Management: As per project constraints, no external databases (e.g., SQL Server) are utilized. The system relies on synchronized, thread-safe data structures (`ConcurrentHashMap`) to handle concurrent requests without data loss or race conditions.
* Deep Nesting via Sub-Resources. Implements the Sub-Resource Locator pattern to maintain and append historical logs of readings for every sensor through a logical resource hierarchy (`/sensors/{sensorId}/readings`).
* Advanced Error Handling: The API is "leak-proof" and never returns raw Java stack traces or default server error pages. It utilizes custom Exception Mappers to enforce business logic and return semantically accurate HTTP status codes with meaningful JSON bodies (e.g., `409 Conflict` for data orphans, `422 Unprocessable Entity` for dependency validation).
* Cross-Cutting Observability: Integrates `ContainerRequestFilter` and `ContainerResponseFilter` to automatically log HTTP methods, URIs, and status codes for all incoming and outgoing API traffic.
* HATEOAS Discovery: Features a root discovery endpoint that provides versioning metadata and hypermedia navigation links to primary resource collections.


Build & Launch Instructions

This project is configured as a standard Maven Web Application and utilizes a lightweight servlet container (Apache Tomcat) to serve the JAX-RS endpoints.

Prerequisites:
* Java Development Kit (JDK) 8 or higher
* Apache Maven
* Apache Tomcat Server (or equivalent lightweight servlet container)
* IDE of choice (e.g., Apache NetBeans, Eclipse, or IntelliJ IDEA)

Step-by-Step Instructions:
1. Clone the Repository: Clone this public GitHub repository to your local machine.
   ```bash
   git clone [YOUR_GITHUB_REPO_LINK_HERE]
Open the Project: Open the cloned directory in your IDE. If using NetBeans, select File > Open Project and select the folder.

Resolve Dependencies: Ensure Maven has downloaded the required Jersey (JAX-RS) and Jackson (JSON) dependencies. You can do this by right-clicking the project and selecting Clean and Build (or running mvn clean install in the terminal).

Deploy and Run: Right-click the project in your IDE and select Run. The IDE will automatically deploy the application to your configured Apache Tomcat server.

Access the API: Once the server has launched successfully, the root API entry point will be available at:

Plaintext
http://localhost:8080/SmartCampusAPI/api/v1


Sample API Interactions (cURL Commands)
Note: Ensure your local server is running. If your port or deployment context differs, replace http://localhost:8080/SmartCampusAPI with your actual base URL.

1. Discovery Endpoint (GET Metadata)

Bash
curl -X GET http://localhost:8080/SmartCampusAPI/api/v1
2. Create a New Room (POST)

Bash
curl -X POST http://localhost:8080/SmartCampusAPI/api/v1/rooms \
-H "Content-Type: application/json" \
-d '{"id":"LIB-301", "name":"Quiet Study", "capacity":30}'
3. Register a New Sensor (POST with Foreign Key Validation)

Bash
curl -X POST http://localhost:8080/SmartCampusAPI/api/v1/sensors \
-H "Content-Type: application/json" \
-d '{"id":"CO2-001", "type":"CO2", "status":"ACTIVE", "currentValue":0.0, "roomId":"LIB-301"}'
4. Retrieve Sensors with Filtering (GET with QueryParam)

Bash
curl -X GET "http://localhost:8080/SmartCampusAPI/api/v1/sensors?type=CO2"
5. Append a Sensor Reading (POST to Sub-Resource)

Bash
curl -X POST http://localhost:8080/SmartCampusAPI/api/v1/sensors/CO2-001/readings \
-H "Content-Type: application/json" \
-d '{"value":415.5}'
📝 Conceptual Report
Part 1: The "Discovery" Endpoint

Question: Why is the provision of "Hypermedia" (links and navigation within responses) considered a hallmark of advanced RESTful design (HATEOAS)? How does this approach benefit client developers compared to static documentation?
Answer: The provision of "Hypermedia" is considered a hallmark of advanced RESTful design (specifically HATEOAS: Hypermedia as the Engine of Application State) because it transforms an API into a dynamic, self-documenting, and discoverable system. Instead of the client relying on hardcoded knowledge of the API's structure, the server dynamically guides the client by embedding relevant navigation links directly within the JSON responses. This approach offers significant benefits to client developers compared to static documentation:

Resilience to Change: If the backend routing or endpoint URLs need to change in the future, client applications will not break. Because the client reads the URI dynamically from the response rather than hardcoding it, the API can evolve without forcing client-side updates.

Reduced Cognitive Load: Developers do not need to constantly cross-reference static, often outdated documentation to figure out what endpoints exist or how to construct URLs for related resources. The API itself tells them exactly what actions are possible from the current state.

State Discoverability: The hypermedia links provided can dynamically reflect business logic. For example, a link to "delete" might only be included in the response if the resource is actually eligible for deletion, preventing the client developer from having to guess or manually validate state.

Part 2: Room Management

Question: In your report, explain the default lifecycle of a JAX-RS Resource class. Is a new instance instantiated for every incoming request, or does the runtime treat it as a singleton? Elaborate on how this architectural decision impacts the way you manage and synchronize your in-memory data structures (maps/lists) to prevent data loss or race conditions.
Answer: By default, the JAX-RS runtime treats Resource classes as request-scoped. This means that a brand-new instance of the Resource class is instantiated for every single incoming HTTP request, and it is subsequently destroyed once the response is dispatched. It is not treated as a singleton by default. This architectural decision has a critical impact on how in-memory data structures (such as HashMap or ArrayList) must be managed. Because multiple client requests can arrive concurrently, multiple instances of the Resource class will execute simultaneously on different threads. If these concurrent instances attempt to read, write, or delete data from a standard, non-thread-safe collection stored in a shared context (like a static variable or an injected singleton service), it will inevitably lead to race conditions, data corruption, or ConcurrentModificationException errors. To prevent data loss and ensure integrity in an in-memory setup, we must employ explicit synchronization strategies. This can be achieved by:

Using Thread-Safe Collections: Replacing standard data structures with concurrent alternatives from the java.util.concurrent package, such as ConcurrentHashMap for storing the Room/Sensor entities, and CopyOnWriteArrayList for nested collections.

Explicit Synchronization: Utilizing synchronized blocks or ReentrantReadWriteLock mechanisms around the specific blocks of code that perform write, update, or delete operations on shared resources, ensuring that only one thread can mutate the state of the in-memory database at any given time.

Question: When returning a list of rooms, what are the implications of returning only IDs versus returning the full room objects? Consider network bandwidth and client side processing.
Answer: Returning only IDs significantly reduces the initial payload size, which saves network bandwidth on the first request. However, it negatively impacts client-side processing because it forces the client to make multiple subsequent GET /{roomId} requests to fetch the full metadata for each individual room. This dramatically increases network latency and overall processing time. Conversely, returning the full room objects consumes more bandwidth upfront but provides all necessary data in a single round-trip, significantly reducing client-side processing complexity and minimizing the total number of HTTP requests required.

Question: Is the DELETE operation idempotent in your implementation? Provide a detailed justification by describing what happens if a client mistakenly sends the exact same DELETE request for a room multiple times.
Answer: Yes, the DELETE operation is idempotent in this implementation. Idempotency dictates that making multiple identical requests has the same effect on the server's core state as making a single request. If a client mistakenly sends the exact same DELETE request for a room multiple times, the first request will successfully remove the room from the data store and return a 204 No Content. Any subsequent identical DELETE requests will search the map, find that the room no longer exists, and simply return a 404 Not Found. Although the HTTP response code changes between the first and subsequent calls, the server's state (the room being absent from the system) remains exactly the same, thereby preserving idempotency.

Part 3: Sensor Operations & Linking

Question: We explicitly use the @Consumes(MediaType.APPLICATION_JSON) annotation on the POST method. Explain the technical consequences if a client attempts to send data in a different format, such as text/plain or application/xml. How does JAX-RS handle this mismatch?
Answer: If a client attempts to send a payload in a format like text/plain to an endpoint strictly annotated with @Consumes(MediaType.APPLICATION_JSON), the JAX-RS runtime will intercept the request before it even reaches the underlying Java method. Because the requested media type does not match the server's accepted configuration, JAX-RS automatically handles this mismatch by rejecting the request and returning an HTTP 415 Unsupported Media Type status code. This enforces strict protocol boundaries and prevents the API from crashing by attempting to parse incompatible data formats.

Question: You implemented this filtering using @QueryParam. Contrast this with an alternative design where the type is part of the URL path (e.g., /api/v1/sensors/type/CO2). Why is the query parameter approach generally considered superior for filtering and searching collections?
Answer: Designing URLs with path parameters (e.g., /api/v1/sensors/type/CO2) implies a strict, hierarchical relationship identifying a specific sub-resource. In contrast, query parameters (?type=CO2) are designed specifically as optional modifiers to an existing collection. The query parameter approach is vastly superior for filtering because it is dynamic and flexible. It allows clients to omit the filter entirely to get the full collection, or easily combine multiple independent filters (e.g., ?type=CO2&status=ACTIVE) without forcing the backend developer to map out an exponential number of static URL path combinations to handle every possible search scenario.

Part 4: Deep Nesting with Sub-Resources

Question: Discuss the architectural benefits of the Sub-Resource Locator pattern. How does delegating logic to separate classes help manage complexity in large APIs compared to defining every nested path (e.g., sensors/{id}/readings/{rid}) in one massive controller class?
Answer: The Sub-Resource Locator pattern provides exceptional architectural benefits by enforcing the Single Responsibility Principle and preventing controller bloat. If we defined every nested path (like sensors/{id}/readings/{rid}) inside one massive SensorResource class, that single file would quickly become an unmaintainable monolithic controller handling entirely separate business concerns (sensor management vs. telemetry history). By returning an instance of a separate SensorReadingResource class, the Sub-Resource Locator dynamically delegates responsibility. This creates a clean, modular hierarchy where the parent class acts solely as a router for that specific path branch, allowing developers to encapsulate the deeply nested reading logic into its own isolated context. This makes the API far easier to test, read, and scale.

Part 5: Advanced Error Handling & Exception Mapping

Question: Why is HTTP 422 often considered more semantically accurate than a standard 404 when the issue is a missing reference inside a valid JSON payload?
Answer: A standard 404 Not Found implies that the target URI endpoint itself does not exist. However, when a client posts a syntactically valid JSON payload to a valid endpoint, but the payload contains a semantic error (like referencing a foreign key that doesn't exist), the server successfully received and understood the request format. HTTP 422 Unprocessable Entity accurately reflects this state: the server understands the content type and syntax, but is unable to process the contained instructions due to a semantic failure.

Question: From a cybersecurity standpoint, explain the risks associated with exposing internal Java stack traces to external API consumers. What specific information could an attacker gather from such a trace?
Answer: Exposing raw Java stack traces acts as an unintentional reconnaissance tool for attackers, revealing sensitive internal system architecture. An attacker can gather highly specific intelligence, such as internal server directory paths, database versions, the specific third-party frameworks and library versions being used (which can be cross-referenced for known CVE vulnerabilities), and the precise execution flow of the business logic. This allows attackers to craft highly targeted exploits rather than relying on blind attacks.

Question: Why is it advantageous to use JAX-RS filters for cross-cutting concerns like logging, rather than manually inserting Logger.info() statements inside every single resource method?
Answer: Using JAX-RS filters centralizes cross-cutting concerns, adhering to the DRY (Don't Repeat Yourself) principle. Instead of polluting every single resource method with boilerplate Logger.info() statements, a filter intercepts all incoming requests and outgoing responses automatically in one centralized location. This ensures consistent observability across the entire API, prevents developers from forgetting to log new endpoints, and keeps the core business logic within the resource classes clean and focused solely on their primary responsibilities.