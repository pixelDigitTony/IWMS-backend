package com.iwms.iwms.domain.auth;

import java.time.OffsetDateTime;
import java.util.Set;
import java.util.UUID;

/**
 * Domain User model aligned with Authentication & Authorization spec.
 * Links to Supabase Auth user by supabaseUserId.
 */
public class User {
    private UUID id;
    private UUID supabaseUserId;
    private String email;
    private String displayName;
    private boolean approved;
    private boolean superAdmin;
    private Set<Role> roles;
    private Set<String> privileges; // store as strings for flexibility
    private Set<UUID> warehouseScopes; // warehouses the user can access
    private UUID organizationId;      // organization scope
    private OffsetDateTime createdAt;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getSupabaseUserId() { return supabaseUserId; }
    public void setSupabaseUserId(UUID supabaseUserId) { this.supabaseUserId = supabaseUserId; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }

    public boolean isApproved() { return approved; }
    public void setApproved(boolean approved) { this.approved = approved; }

    public boolean isSuperAdmin() { return superAdmin; }
    public void setSuperAdmin(boolean superAdmin) { this.superAdmin = superAdmin; }

    public Set<Role> getRoles() { return roles; }
    public void setRoles(Set<Role> roles) { this.roles = roles; }

    public Set<String> getPrivileges() { return privileges; }
    public void setPrivileges(Set<String> privileges) { this.privileges = privileges; }

    public Set<UUID> getWarehouseScopes() { return warehouseScopes; }
    public void setWarehouseScopes(Set<UUID> warehouseScopes) { this.warehouseScopes = warehouseScopes; }

    public UUID getOrganizationId() { return organizationId; }
    public void setOrganizationId(UUID organizationId) { this.organizationId = organizationId; }

    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
}


