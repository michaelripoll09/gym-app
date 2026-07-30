alter table workout_set_logs add column load_kg numeric(7,2) check (load_kg >= 0);
