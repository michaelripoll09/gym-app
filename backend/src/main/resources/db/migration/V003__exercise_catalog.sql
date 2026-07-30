create table exercises (
    id uuid primary key,
    source_name varchar(128) not null,
    source_external_id varchar(128) not null,
    source_commit varchar(64) not null,
    name varchar(255) not null,
    spanish_instructions text not null,
    published boolean not null default false,
    unique (source_name, source_external_id)
);

create table exercise_training_profiles (
    exercise_id uuid not null references exercises(id) on delete cascade,
    profile_code varchar(32) not null,
    primary key (exercise_id, profile_code)
);
