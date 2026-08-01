create table progress_goals (
    id uuid primary key,
    user_id uuid not null references users(id) on delete cascade,
    goal_type varchar(32) not null check (goal_type in ('BODY_WEIGHT', 'EXERCISE_LOAD')),
    exercise_name varchar(255),
    target_value numeric(7,2) not null check (target_value > 0),
    target_date date,
    status varchar(32) not null default 'ACTIVE' check (status in ('ACTIVE', 'COMPLETED')),
    created_at timestamptz not null default now(),
    completed_at timestamptz
);
