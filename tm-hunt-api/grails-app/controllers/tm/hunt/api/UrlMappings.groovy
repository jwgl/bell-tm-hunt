package tm.hunt.api

class UrlMappings {

    static mappings = {

        group "/settings", {
            "/types"(resources: 'type')
            "/subtypes"(resources: 'subtype')
            "/tasks"(resources: 'reviewTask')
            "/experts"(resources: 'expert')
            "/teams"(resources: 'team')
            "/checkers"(resources: 'checker')
        }

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

        "/approvers"(resources: 'approver', includes: []) {
            "/applications"(resources: 'applicationApproval', includes: ['index']) {
                "/workitems"(resources: 'applicationApproval', includes: ['show', 'patch'])
            }
        }
    }
}
