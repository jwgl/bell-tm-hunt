package tm.hunt.api

class UrlMappings {

    static mappings = {

        group "/settings", {
            "/types"(resources: 'type')
            "/subtypes"(resources: 'subtype')
            "/experts"(resources: 'expert')
            "/teams"(resources: 'team')
            "/checkers"(resources: 'checker')
            "/tasks"(resources: 'reviewTask') {
                "/projects"(resources: 'projectSelect'){
                    "/attachments"(controller: 'projectSelect', action: 'attachments', method: 'GET')
                }
            }
        }

        "/teachers"(resources: 'teacher', includes: []) {
            "/applications"(resources: 'application') {
                "/checkers"(controller: 'application', action: 'checkers', method: 'GET')
                "/attachments"(controller: 'application', action: 'attachments', method: 'GET')
                collection {
                    "/upload"(controller: 'application', action: 'upload', method: 'POST')
                }
            }
            "/tasks"(resources: 'taskPublic', includes: ['index', 'show'])
        }

        "/checkers"(resources: 'checker', includes: []) {
            "/applications"(resources: 'applicationCheck', includes: ['index']) {
                "/workitems"(resources: 'applicationCheck', includes: ['show', 'patch'])
                "/approvers"(controller: 'applicationCheck', action: 'approvers', method: 'GET')
                "/attachments"(controller: 'applicationCheck', action: 'attachments', method: 'GET')
            }
            "/tasks"(resources: 'taskChecker', includes: ['index', 'show'])
        }

        "/approvers"(resources: 'approver', includes: []) {
            "/applications"(resources: 'applicationApproval', excludes: ['show', 'patch']) {
                "/workitems"(resources: 'applicationApproval', includes: ['show', 'patch'])
                "/attachments"(controller: 'applicationApproval', action: 'attachments', method: 'GET')
            }
            "/tasks"(resources: 'taskApproval', includes: ['index', 'show']) {
                "/projects"(resources: 'applicationAdministration') {
                    "/attachments"(controller: 'applicationAdministration', action: 'attachments', method: 'GET')
                    "/expertReviews"(controller: 'applicationAdministration', action: 'expertReviews', method: 'GET')
                }
            }
        }

        "/experts"(resources: 'expert', includes: []) {
            "/reviews"(resources: 'expertReview')
        }

        "500"(view: '/error')
        "404"(view: '/notFound')
    }
}
