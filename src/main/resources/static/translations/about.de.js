export const ABOUT_CNT = {
  About: {
    left: `
truegotham ist ein interaktives Dashboard, das polizeiliche Pressemitteilungen nahezu in Echtzeit visualisiert. Ziel des Projekts ist es, trockene Textmeldungen durch den Einsatz moderner KI-Technologien in eine atmosphärische und verständliche Darstellung zu verwandeln. Dabei werden aktuelle Vorfälle aus der Region Dortmund(aktueller Stand) erfasst und grafisch aufbereitet.

Technisch basiert das System auf einem automatisierten Workflow: Ein Crawler sammelt regelmäßig neue Meldungen vom Presseportal ein. Die Meldungen werden anschließend durch eine mehrstufige KI-Pipeline verarbeitet. Hierbei werden die Texte klassifiziert, Standorte extrahiert, geocodiert und Zusammenfassungen erstellt. Zusätzlich generiert die KI passende, atmosphärische Bilder, die zusammen mit den analysierten Daten in einer modernen Weboberfläche auf einer Karte dargestellt werden. Des Weiteren werden statistische Analysen durchgeführt und auf dem Dashboard als Charts dargestellt.
    `,
    right: "/img/truegotham-architecture.png"
  },
  Motivation: {
    left: `
Comics wie Batman oder Sin City und Krimis wie der Tatort haben mich schon immer fasziniert. Mit truegotham wollte ich einfach mal ausprobieren, wie man echte Polizeimeldungen in so eine düstere, fiktive Welt übertragen kann. Das Projekt ist für mich die perfekte Spielwiese, um neue GenAI-Technologien an echten Daten zu testen und dabei ein bisschen mit der Atmosphäre zu experimentieren.

Als Software-Entwickler ist das Ganze für mich auch eine Art interaktive Weiterbildung. Ich kann hier unter realen Bedingungen mit Prompt Engineering oder Agentic AI rumspielen und schauen, was die neuesten Modelle so drauf haben. So bleibe ich technisch am Ball und habe gleichzeitig eine Plattform, auf der ich neue Ansätze ohne großen Overhead einfach mal ausprobieren kann.
`,
    right: `&nbsp;`
  },
  TechStack: {
    left: `
Das technische Herzstück von truegotham bildet ein robustes Backend auf Basis von Kotlin und Spring Boot, kombiniert mit einem extrem leichtgewichtigen Vanilla-Frontend aus purem HTML, CSS und JavaScript. Die Intelligenz des Systems wird durch OpenAI-Modelle wie GPT-5 und GPT-Image-1 ermöglicht, die sowohl für die Textanalyse als auch für die generative Bildbeschreibung und -erstellung verantwortlich sind.
    
Für die interaktive Visualisierung auf der Karte kommen OpenStreetMap und die Leaflet-Bibliothek zum Einsatz, während die Daten persistent in einer H2-Datenbank und im Dateisystem gesichert werden. Containerisierung mit Docker und Kubernetes sowie automatisierte CI/CD-Pipelines via GitHub Actions sorgen für ein reibungsloses Deployment. Entwickelt wurde das Projekt in IntelliJ und Antigravity IDE mit Unterstützung von GitHub Copilot und Gemini.
    `,
    right: `/img/techstack.png`
  }
}