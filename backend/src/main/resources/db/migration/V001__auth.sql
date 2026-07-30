create table users (
    id uuid primary key,
    email varchar(320) not null unique,
    password_hash varchar(255) not null,
    created_at timestamptz not null
);

create table consents (
    id uuid primary key,
    user_id uuid not null references users(id),
    consent_type varchar(64) not null,
    accepted_at timestamptz not null
);
