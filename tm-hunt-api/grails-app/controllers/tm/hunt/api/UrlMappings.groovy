package tm.hunt.api

class UrlMappings {

    static mappings = {
        "/types"(resources: 'type')
        "/subtypes"(resources: 'subtype')
        "/tasks"(resources: 'reviewTask')

        "/teachers"(resources: 'teacher', includes: []) {
            "/applications"(resources: 'application') {
                "/checkers"(controller: 'application', action: 'checkers', method: 'GET')
            }
        }
    }
}
