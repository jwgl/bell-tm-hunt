package tm.hunt.web

class UrlMappings {

    static mappings = {

        "/types"(resources: 'type', includes: ['index'])
        "/tasks"(resources: 'reviewTask', includes: ['index'])
        "/teachers"(resources: 'teacher', includes: []) {
            "/projects"(resources: 'project', includes: ['index'])
        }

        "/"(view:"/index")
        "500"(view:'/error')
        "404"(view:'/notFound')
    }
}
