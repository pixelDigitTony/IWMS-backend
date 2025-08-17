-- Add approval and super-admin flags; link to Supabase user id
alter table if exists app_user
  add column if not exists supabase_user_id uuid unique,
  add column if not exists approved boolean not null default false,
  add column if not exists is_super_admin boolean not null default false;

create index if not exists idx_app_user_supabase_id on app_user(supabase_user_id);


