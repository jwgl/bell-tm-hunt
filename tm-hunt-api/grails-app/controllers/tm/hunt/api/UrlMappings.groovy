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
                "/upload"(controller: 'reviewTask', action: 'upload', method: 'POST')
                "/projects"(resources: 'projectSelect')
            }
        }

        "/tasks"(resources: 'reviewTask', includes: []) {
            collection {
                "/upload"(controller: 'reviewTask', action: 'upload', method: 'POST')
            }
        }

        "/teachers"(resources: 'teacher', includes: []) {
            "/applications"(resources: 'application') {
                "/checkers"(controller: 'application', action: 'checkers', method: 'GET')
                collection {
                    "/upload"(controller: 'application', action: 'upload', method: 'POST')
                }
            }
            "/tasks"(resources: 'taskPublic', includes: ['index', 'show'])
            "/infoChanges"(resources: 'infoChange') {
                "/checkers"(controller: 'infoChange', action: 'checkers', method: 'GET')
                collection {
                    "/project"(controller: 'infoChange', action: 'findProject', method: 'GET')
                    "/upload"(controller: 'infoChange', action: 'upload', method: 'POST')
                }
            }
        }

        "/checkers"(resources: 'checker', includes: []) {
            "/applications"(resources: 'applicationCheck', includes: ['index']) {
                "/workitems"(resources: 'applicationCheck', includes: ['show', 'patch'])
                "/approvers"(controller: 'applicationCheck', action: 'approvers', method: 'GET')
            }
            "/tasks"(resources: 'taskChecker', includes: ['index', 'show'])
            "/infoChanges"(resources: 'infoChangeCheck', includes: ['index']) {
                "/workitems"(resources: 'infoChangeCheck', includes: ['show', 'patch'])
                "/approvers"(controller: 'applicationCheck', action: 'approvers', method: 'GET')
            }
            "/projects"(resources: 'projectDepartment')
            "/principalChanges"(resources: 'principalChange') {
                "/checkers"(controller: 'principalChange', action: 'checkers', method: 'GET')
                collection {
                    "/upload"(controller: 'principalChange', action: 'upload', method: 'POST')
                }
            }
        }

        "/approvers"(resources: 'approver', includes: []) {
            "/applications"(resources: 'applicationApproval', excludes: ['show', 'patch']) {
                "/workitems"(resources: 'applicationApproval', includes: ['show', 'patch'])
            }
            "/tasks"(resources: 'taskApproval', includes: ['index', 'show']) {
                "/projects"(resources: 'applicationAdministration') {
                    "/expertReviews"(controller: 'applicationAdministration', action: 'expertReviews', method: 'GET')
                }
            }
            "/infoChanges"(resources: 'infoChangeApproval', includes: ['index']) {
                "/workitems"(resources: 'infoChangeApproval', includes: ['show', 'patch'])
                "/reviewers"(controller: 'infoChangeApproval', action: 'reviewers', method: 'GET')
            }
            "/funds"(resources: 'funds') {
                collection {
                    "/upload"(controller: 'funds', action: 'importData', method: 'POST')
                }
            }
        }

        "/experts"(resources: 'expert', includes: []) {
            "/reviews"(resources: 'expertReview')
            "/tasks"(resources: 'taskExpert', includes: ['index', 'show'])
        }

        "/directors"(resources: 'director', includes: []) {
            "/infoChanges"(resources: 'infoChangeReview', includes: ['index']) {
                "/workitems"(resources: 'infoChangeReview', includes: ['show', 'patch'])
                "/reviewers"(controller: 'infoChangeReview', action: 'reviewers', method: 'GET')
            }
        }

        "/projects"(resources: 'projects')

        "/attachments"(resources: 'attachment', includes: ['index', 'show'])

        "500"(view: '/error')
        "404"(view: '/notFound')
    }
}
