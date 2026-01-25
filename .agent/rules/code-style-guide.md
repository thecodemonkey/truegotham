
# Code Style Guide & Project Guidelines

This project follows a specific philosophy: **Maximum Simplicity & Self-Containment**.
It is designed to be easily maintained by AI agents and humans alike, avoiding unnecessary abstraction layers.

## 1. Project Overview & Architecture

*   **Type**: Monolithic Web Application.
*   **Backend**: Kotlin with Spring Boot 3.x.
*   **Frontend**: Vanilla JS (ES6+), HTML5, CSS3.
*   **Deployment**: Single JAR file (Frontend embedded in `src/main/resources/static`).
*   **Philosophy**: "No Build Step" for Frontend. No node_modules, no webpack, no transpilation.

## 2. Backend Guidelines (Kotlin / Spring Boot)

*   **Location**: `src/main/kotlin/il/tutorials/truegotham/...`
*   **Language**: Kotlin (JDK 21).
*   **Framework**: Spring Boot.
*   **Architecture**:
    *   `controller`: REST endpoints (Consume JSON, produce JSON).
    *   `service`: Business logic & AI orchestration.
    *   `repository`: Data access (Spring Data JPA).
    *   `model`: Entities and DTOs.
*   **Conventions**:
    *   Use **Constructor Injection**.
    *   Keep Controllers thin; move logic to Services.
    *   Use Kotlin's null-safety features.
    *   **Logging**: Use slf4j or standard logging.

## 3. Frontend Guidelines (Vanilla JS)

*   **Location**: `src/main/resources/static`
*   **Core Principle**: The frontend is served directly by Spring Boot's static resource handler. Changes are immediate (with dev tools or simple refresh).
*   **Libraries**:
    *   **jQuery**: Used for DOM manipulation and events.
    *   **Leaflet**: For Maps.
    *   **Chart.js**: For Charts.
    *   **Turf.js**: For geospatial helpers.
    *   *Note*: Do NOT add npm packages. If a library is needed, download the `.js` file to `lib/` and reference it in `index.html`.

### Component Structure
Each UI part is a "Component" with its own folder in `static/components/`:
```text
components/
└── my-component/
    ├── my-component.html  # Template (HTML fragment)
    ├── my-component.css   # Styles (scoped by class naming)
    └── my-component.js    # Logic (Object Namespace)
```

### Javascript Pattern
Use a **Namespace Object Pattern** for components to keep global scope clean:
```javascript
const MY_COMPONENT = {
  // Returns the HTML string (loaded via helper)
  view: async () => {
    return await loadHTML('my-component');
  },
  
  // Initialization logic (event listeners, data fetching)
  init: async () => {
    // jQuery used for concise DOM interactions
    $('.my-button').on('click', () => { ... });
  }
}
```

### CSS Guidelines
*   **No Preprocessors**: Write standard CSS.
*   **Nesting**: Native CSS nesting is allowed (supported by modern browsers).
*   **Layout**: Use Grid and Flexbox.
*   **Visuals**: Follow the "True Gotham" aesthetic – Dark mode, glassmorphism, neon accents.

## 4. Development Workflow

1.  **Start Backend**: `./gradlew bootRun`
2.  **Access**: `http://localhost:7171`
3.  **Frontend Changes**: Modify files in `src/main/resources/static`. Refresh browser. 
    *   *Note*: Spring Boot caching might need to be disabled or explicit refresh needed depending on IDE/Gradle setup.
4.  **Backend Changes**: Requires application restart (or Hot Reload if configured).

## 5. AI Agent Instructions

When asking an AI Layout/Code change:
*   ALWAYS specify if strictly Backend, Frontend, or Full-Stack change is needed.
*   Remember there is **no React/Vue/Angular**. Do not suggest `import Component from ...`.
*   Use `loadHTML` helper for templates.
*   Respect the directory structure.
