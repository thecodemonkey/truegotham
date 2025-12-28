export const ABOUT_CNT = {
    About: {
        left: `
truegotham is an interactive dashboard that visualizes police press releases almost in real-time. The goal of the project is to transform dry text notifications into an atmospheric and understandable presentation using modern AI technologies. Current incidents from the Dortmund region (as of now) are captured and graphically processed.

Technically, the system is based on an automated workflow: A crawler regularly collects new reports from the Presseportal. The reports are then processed by a multi-stage AI pipeline. During this process, texts are classified, locations extracted, geocoded, and summaries created. Additionally, the AI generates matching, atmospheric images, which are displayed on a map in a modern web interface together with the analyzed data. Furthermore, statistical analyses are performed and displayed as charts on the dashboard.
    `,
        right: "/img/truegotham-architecture.png"
    },
    Motivation: {
        left: `
Comics like Batman or Sin City and crime thrillers like "Tatort" have always fascinated me. With truegotham, I simply wanted to try out how real police reports could be transferred into such a dark, fictional world. For me, the project is the perfect playground to test new GenAI technologies on real data and experiment a bit with the atmosphere.

As a software developer, the whole thing is also a kind of interactive further education for me. I can play around with prompt engineering or agentic AI under real conditions and see what the latest models are capable of. This keeps me on the ball technically and at the same time gives me a platform where I can simply try out new approaches without much overhead.
`,
        right: `&nbsp;`
    },
    TechStack: {
        left: `
The technical core of truegotham is a robust backend based on Kotlin and Spring Boot, combined with an extremely lightweight vanilla frontend made of pure HTML, CSS, and JavaScript. The intelligence of the system is enabled by OpenAI models like GPT-5 and GPT-Image-1, which are responsible for both text analysis and generative image description and creation.
    
For interactive visualization on the map, OpenStreetMap and the Leaflet library are used, while data is persistently stored in an H2 database and the file system. Containerization with Docker and Kubernetes, as well as automated CI/CD pipelines via GitHub Actions, ensure smooth deployment. The project was developed in IntelliJ and Antigravity IDE with support from GitHub Copilot and Gemini.
    `,
        right: `/img/techstack.png`
    }
}
