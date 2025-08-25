-- Drop columns now sourced from auth.users
alter table if exists user_info
  drop column if exists email,
  drop column if exists display_name;


