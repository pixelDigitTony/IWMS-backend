-- Complete database schema initialization
-- This replaces all previous migrations with a single consolidated setup

-- =====================================================
-- USER MANAGEMENT SCHEMA
-- =====================================================

-- Main user information table
create table if not exists user_info (
  id uuid primary key default gen_random_uuid(),
  supabase_user_id uuid unique,
  approved boolean not null default false,
  organization_id uuid,
  company_name varchar(255),
  roles text[], -- Array of role names (SUPER_ADMIN, ORG_ADMIN, etc.)
  privileges text[], -- Array of privilege strings (users.manage, etc.)
  warehouse_scopes uuid[], -- Array of warehouse UUIDs the user can access
  created_at timestamptz not null default now()
);

-- =====================================================
-- INDEXES FOR PERFORMANCE
-- =====================================================

-- User info indexes
create index if not exists idx_user_info_supabase_id on user_info(supabase_user_id);
create index if not exists idx_user_info_organization_id on user_info(organization_id);
create index if not exists idx_user_info_company_name on user_info(company_name);
create index if not exists idx_user_info_approved on user_info(approved);
create index if not exists idx_user_info_created_at on user_info(created_at);

-- =====================================================
-- ROLE AND PRIVILEGE ENUM VALUES
-- =====================================================

-- Note: These are represented as string arrays in the roles and privileges columns
-- Valid roles: SUPER_ADMIN, ORG_ADMIN, WAREHOUSE_MANAGER, INVENTORY_CONTROLLER, OPERATOR, VIEWER, AUDITOR
-- Valid privileges: users.manage, roles.manage, warehouses.manage, products.manage, inventory.view, etc.

-- =====================================================
-- INITIAL DATA (Optional - uncomment if needed)
-- =====================================================

-- Example: Create a default super admin user (uncomment and modify as needed)
-- insert into user_info (supabase_user_id, approved, roles, privileges, created_at)
-- values ('your-supabase-user-id-here', true, array['SUPER_ADMIN'], array['users.manage', 'roles.manage'], now());

-- =====================================================
-- CONSTRAINTS AND VALIDATION
-- =====================================================

-- Ensure array columns have defaults (empty arrays)
alter table user_info alter column roles set default '{}';
alter table user_info alter column privileges set default '{}';
alter table user_info alter column warehouse_scopes set default '{}';
