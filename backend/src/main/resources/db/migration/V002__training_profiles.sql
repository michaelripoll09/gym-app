create table training_profiles (
    id uuid primary key,
    user_id uuid not null unique references users(id),
    experience_level varchar(32) not null,
    primary_profile varchar(32) not null,
    goal varchar(128) not null,
    availability_band varchar(32) not null,
    available_days_per_week integer not null,
    session_duration_minutes integer not null
);

create table profile_secondary_interests (
    training_profile_id uuid not null references training_profiles(id) on delete cascade,
    profile_code varchar(32) not null,
    primary key (training_profile_id, profile_code)
);
