alter table exercises
    add column source_file_sha256 varchar(64) not null default '',
    add column attribution text,
    add column review_status varchar(32) not null default 'PENDING_EDITORIAL_REVIEW';

alter table exercises
    add constraint exercises_review_status_check
    check (review_status in ('PENDING_EDITORIAL_REVIEW', 'APPROVED', 'REJECTED'));
