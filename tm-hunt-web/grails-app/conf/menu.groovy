menuGroup 'main', {
    hunt 70, {
        application         60, 'PERM_HUNT_WRITE', '/web/hunt/teachers/${userId}/applications'
        settings            70, {
            type                71, 'PERM_HUNT_ADMIN', '/web/hunt/types'
            task                72, 'PERM_HUNT_ADMIN', '/web/hunt/tasks'
        }
    }
}
