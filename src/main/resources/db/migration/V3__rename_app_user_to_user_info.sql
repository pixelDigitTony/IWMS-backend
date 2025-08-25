-- Rename app_user table to user_info and update related index names
do $$
begin
    if exists (
        select 1 from information_schema.tables 
        where table_schema = 'public' and table_name = 'app_user'
    ) then
        alter table public.app_user rename to user_info;
    end if;
end $$;

-- Rename index if it exists
do $$
begin
    if exists (
        select 1 from pg_class c
        join pg_namespace n on n.oid = c.relnamespace
        where c.relkind = 'i' and c.relname = 'idx_app_user_supabase_id' and n.nspname = 'public'
    ) then
        alter index public.idx_app_user_supabase_id rename to idx_user_info_supabase_id;
    end if;
end $$;


