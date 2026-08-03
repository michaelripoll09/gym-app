create table active_workout_plans (
    user_id uuid primary key references users(id) on delete cascade,
    plan_id uuid not null unique references workout_plans(id) on delete cascade
);
