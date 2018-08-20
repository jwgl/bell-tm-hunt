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

        "/checkers"(resources: 'checker', includes: []) {
            "/applications"(resources: 'applicationCheck', includes: ['index']) {
                "/workitems"(resources: 'applicationCheck', includes: ['show', 'patch'])
                "/approvers"(controller: 'applicationCheck', action: 'approvers', method: 'GET')
            }
        }
    }
}
