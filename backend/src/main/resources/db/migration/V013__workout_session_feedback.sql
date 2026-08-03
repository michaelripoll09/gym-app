alter table workout_sessions add column perceived_exertion integer check (perceived_exertion between 1 and 10);
alter table workout_sessions add column note text;
