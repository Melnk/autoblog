create table public_vehicle_reports (
    id uuid primary key,
    vehicle_id uuid not null,
    public_token varchar(96) not null,
    status varchar(16) not null,
    created_at timestamp with time zone not null,
    updated_at timestamp with time zone not null,
    constraint fk_public_vehicle_reports_vehicle foreign key (vehicle_id) references vehicles (id),
    constraint uk_public_vehicle_reports_public_token unique (public_token)
);

create index idx_public_vehicle_reports_public_token on public_vehicle_reports (public_token);
create index idx_public_vehicle_reports_vehicle_id on public_vehicle_reports (vehicle_id);
create index idx_public_vehicle_reports_status on public_vehicle_reports (status);
