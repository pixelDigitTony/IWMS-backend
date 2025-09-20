package com.iwms.iwms.domain.model.auth;

/**
 * Fine-grained permissions for actions. Expressed as authority strings.
 */
public enum Privilege {
    USERS_MANAGE("users.manage"),
    ROLES_MANAGE("roles.manage"),
    WAREHOUSES_MANAGE("warehouses.manage"),
    PRODUCTS_MANAGE("products.manage"),
    INVENTORY_VIEW("inventory.view"),
    ADJUSTMENTS_CREATE("adjustments.create"),
    TRANSFERS_CREATE("transfers.create"),
    TRANSFERS_APPROVE("transfers.approve"),
    RECEIVING_PROCESS("receiving.process"),
    SHIPPING_PROCESS("shipping.process"),
    COUNTS_PERFORM("counts.perform"),
    REPORTS_VIEW("reports.view"),
    ATTACHMENTS_UPLOAD("attachments.upload");

    private final String value;

    Privilege(String value) { this.value = value; }
    public String value() { return value; }
}


