package tm.hunt.web

class UrlMappings {

    static mappings = {

        "/types"(resources: 'type', includes: ['index'])
        "/tasks"(resources: 'reviewTask', includes: ['index'])
        "/teachers"(resources: 'teacher', includes: []) {
            "/applications"(resources: 'application', includes: ['index'])
        }

        "/checkers"(resources: 'checker', includes: []) {
            "/applications"(resources: 'applicationCheck', includes: ['index'])
        }

        "/"(view:"/index")
        "500"(view:'/error')
        "404"(view:'/notFound')
    }
}
