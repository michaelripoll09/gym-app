create table body_measurements (
    id uuid primary key,
    user_id uuid not null references users(id) on delete cascade,
    recorded_on date not null,
    weight_kg numeric(6, 2) not null,
    waist_cm numeric(6, 2),
    hip_cm numeric(6, 2),
    chest_cm numeric(6, 2),
    created_at timestamptz not null default now(),
    unique (user_id, recorded_on)
);
