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
            "/tasks"(resources: 'taskPublic', includes: ['index'])
        }

        "/checkers"(resources: 'checker', includes: []) {
            "/applications"(resources: 'applicationCheck', includes: ['index']) {
                "/workitems"(resources: 'applicationCheck', includes: ['show', 'patch'])
                "/approvers"(controller: 'applicationCheck', action: 'approvers', method: 'GET')
            }
            "/tasks"(resources: 'taskChecker', includes: ['index'])
        }

        "/approvers"(resources: 'approver', includes: []) {
            "/applications"(resources: 'applicationApproval', excludes: ['show', 'patch']) {
                "/workitems"(resources: 'applicationApproval', includes: ['show', 'patch'])
            }
            "/tasks"(resources: 'taskApproval', includes: ['index'])
        }

        "/experts"(resources: 'expert', includes: []) {
            "/reviews"(resources: 'expertReview')
        }

        "500"(view: '/error')
        "404"(view: '/notFound')
    }
}
