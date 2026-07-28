alter table workout_plan_exercises add column rest_seconds integer not null default 60 check (rest_seconds > 0);
