menuGroup 'main', {
    hunt 70, {
        application         60, 'PERM_HUNT_WRITE', '/web/hunt/teachers/${userId}/applications'
        applicationCheck    61, 'PERM_HUNT_CHECK', '/web/hunt/checkers/${userId}/applications'
        applicationApproval 62, 'PERM_HUNT_ADMIN', '/web/hunt/approvers/${userId}/applications'
    }
    settings 90, {
        type                51, 'PERM_HUNT_ADMIN', '/web/hunt/settings/types'
        task                52, 'PERM_HUNT_ADMIN', '/web/hunt/settings/tasks'
        checker             53, 'PERM_HUNT_ADMIN', '/web/hunt/settings/checkers'
        expert              54, 'PERM_HUNT_ADMIN', '/web/hunt/settings/experts'
    }
}
