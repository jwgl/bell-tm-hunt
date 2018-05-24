package tm.hunt.api

class UrlMappings {

    static mappings = {
        "/types"(resources: 'type')
        "/subtypes"(resources: 'subtype')
        "/tasks"(resources: 'reviewTask')

        "/teachers"(resources: 'teacher', includes: []) {
            "/projects"(resources: 'Project')
        }

        "500"(view: '/error')
        "404"(view: '/notFound')
    }
}
