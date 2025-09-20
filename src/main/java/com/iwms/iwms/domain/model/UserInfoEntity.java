package com.iwms.iwms.domain.model;

import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import com.iwms.iwms.domain.model.auth.Privilege;
import com.iwms.iwms.domain.model.auth.Role;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "user_info")
public class UserInfoEntity {

    @Id
    private UUID id;

    // Email and display name are stored in auth.users; not duplicated here

    @Column(name = "supabase_user_id", unique = true)
    private UUID supabaseUserId;

    @Column(nullable = false)
    private boolean approved;

    @Column(name = "roles", columnDefinition = "text[]")
    private String[] rolesArray;

    @Column(name = "privileges", columnDefinition = "text[]")
    private String[] privilegesArray;

    @Column(name = "warehouse_scopes", columnDefinition = "uuid[]")
    private UUID[] warehouseScopesArray;

    @Column(name = "organization_id")
    private UUID organizationId;

    @Column(name = "company_name")
    private String companyName;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    // No email/displayName fields here

    public UUID getSupabaseUserId() { return supabaseUserId; }
    public void setSupabaseUserId(UUID supabaseUserId) { this.supabaseUserId = supabaseUserId; }

    public boolean isApproved() { return approved; }
    public void setApproved(boolean approved) { this.approved = approved; }

    public Set<Role> getRoles() {
        if (rolesArray == null) return Set.of();
        return Arrays.stream(rolesArray)
                .<Role>map(this::parseRole)
                .filter(java.util.Objects::nonNull)
                .collect(Collectors.toSet());
    }
    
    private Role parseRole(String roleString) {
        if (roleString == null || roleString.trim().isEmpty()) {
            return null;
        }
        
        try {
            // First try to parse as plain enum value
            return Role.valueOf(roleString.trim());
        } catch (IllegalArgumentException e) {
            // If that fails, try to parse as JSON and extract the role
            try {
                if (roleString.trim().startsWith("{") && roleString.trim().endsWith("}")) {
                    // It's a JSON object, try to extract the role value
                    com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                    com.fasterxml.jackson.databind.JsonNode jsonNode = mapper.readTree(roleString);
                    
                    // Look for common JSON structures
                    String roleValue = null;
                    if (jsonNode.has("role")) {
                        roleValue = jsonNode.get("role").asText();
                    } else if (jsonNode.has("name")) {
                        roleValue = jsonNode.get("name").asText();
                    } else if (jsonNode.isTextual()) {
                        roleValue = jsonNode.asText();
                    }
                    
                    if (roleValue != null && !roleValue.trim().isEmpty()) {
                        return Role.valueOf(roleValue.trim());
                    }
                }
            } catch (Exception jsonException) {
                // JSON parsing failed, ignore
            }
            
            // If all parsing attempts fail, return null (will be filtered out)
            return null;
        }
    }

    public void setRoles(Set<Role> roles) {
        if (roles == null) {
            this.rolesArray = new String[0];
        } else {
            this.rolesArray = roles.stream()
                    .map(Enum::name)
                    .toArray(String[]::new);
        }
    }

    public boolean isSuperAdmin() {
        return getRoles().contains(Role.SUPER_ADMIN);
    }

    public Set<String> getPrivileges() {
        return privilegesArray != null ? Set.of(privilegesArray) : Set.of();
    }

    public void setPrivileges(Set<String> privileges) {
        if (privileges == null) {
            this.privilegesArray = new String[0];
        } else {
            this.privilegesArray = privileges.toArray(new String[0]);
        }
    }

    public Set<UUID> getWarehouseScopes() {
        return warehouseScopesArray != null ? Set.of(warehouseScopesArray) : Set.of();
    }

    public void setWarehouseScopes(Set<UUID> warehouseScopes) {
        if (warehouseScopes == null) {
            this.warehouseScopesArray = new UUID[0];
        } else {
            this.warehouseScopesArray = warehouseScopes.toArray(new UUID[0]);
        }
    }

    public UUID getOrganizationId() { return organizationId; }
    public void setOrganizationId(UUID organizationId) { this.organizationId = organizationId; }

    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }

    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
}


