create table event_attachments (
    id uuid primary key,
    vehicle_id uuid not null,
    event_id uuid not null,
    type varchar(32) not null,
    visibility varchar(16) not null,
    original_filename varchar(255) not null,
    content_type varchar(120) not null,
    size_bytes bigint not null,
    checksum_sha256 varchar(64) not null,
    storage_key varchar(512) not null,
    description text,
    created_at timestamp with time zone not null,
    constraint fk_event_attachments_vehicle foreign key (vehicle_id) references vehicles (id),
    constraint fk_event_attachments_event foreign key (event_id) references vehicle_events (id),
    constraint uk_event_attachments_storage_key unique (storage_key)
);

create index idx_event_attachments_vehicle_id on event_attachments (vehicle_id);
create index idx_event_attachments_event_id on event_attachments (event_id);
create index idx_event_attachments_visibility on event_attachments (visibility);
create index idx_event_attachments_checksum_sha256 on event_attachments (checksum_sha256);
