package com.iwms.iwms.domain.auth;

/**
 * System roles used for coarse-grained authorization and UI gating.
 */
public enum Role {
    SUPER_ADMIN,
    ORG_ADMIN,
    WAREHOUSE_MANAGER,
    INVENTORY_CONTROLLER,
    OPERATOR,
    VIEWER,
    AUDITOR
}


