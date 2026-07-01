create table vehicles (
    id uuid primary key,
    vin varchar(17) not null,
    make varchar(120),
    model varchar(120),
    generation varchar(120),
    model_year integer,
    engine varchar(120),
    transmission varchar(64),
    trim_name varchar(120),
    market varchar(8) not null default 'RU',
    created_at timestamp with time zone not null,
    updated_at timestamp with time zone not null,
    constraint uk_vehicles_vin unique (vin)
);

create index idx_vehicles_vin on vehicles (vin);

create table vehicle_events (
    id uuid primary key,
    vehicle_id uuid not null,
    sequence_number bigint not null,
    event_type varchar(32) not null,
    event_date date not null,
    odometer_km integer,
    title varchar(240) not null,
    description varchar(2000),
    cost_amount numeric(19, 2),
    cost_currency varchar(3) not null default 'RUB',
    service_name varchar(240),
    payload text,
    previous_event_hash varchar(64),
    event_hash varchar(64) not null,
    created_at timestamp with time zone not null,
    constraint fk_vehicle_events_vehicle foreign key (vehicle_id) references vehicles (id),
    constraint uk_vehicle_events_vehicle_sequence unique (vehicle_id, sequence_number)
);

create index idx_vehicle_events_vehicle_id on vehicle_events (vehicle_id);
create index idx_vehicle_events_vehicle_sequence on vehicle_events (vehicle_id, sequence_number);
create index idx_vehicle_events_event_hash on vehicle_events (event_hash);
