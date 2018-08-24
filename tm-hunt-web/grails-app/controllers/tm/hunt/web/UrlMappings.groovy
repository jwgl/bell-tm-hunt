package tm.hunt.web

class UrlMappings {

    static mappings = {

        group "/settings", {
            "/types"(resources: 'type', includes: ['index'])
            "/subtypes"(resources: 'subtype', includes: ['index'])
            "/tasks"(resources: 'reviewTask', includes: ['index'])
            "/experts"(resources: 'expert', includes: ['index'])
            "/checkers"(resources: 'checker', includes: ['index'])
        }

        "/teachers"(resources: 'teacher', includes: []) {
            "/applications"(resources: 'application', includes: ['index'])
        }

        "/checkers"(resources: 'checker', includes: []) {
            "/applications"(resources: 'applicationCheck', includes: ['index'])
        }

        "/approvers"(resources: 'approver', includes: []) {
            "/applications"(resources: 'applicationApproval', includes: ['index'])
        }

        "/"(view:"/index")
        "500"(view:'/error')
        "404"(view:'/notFound')
    }
}
