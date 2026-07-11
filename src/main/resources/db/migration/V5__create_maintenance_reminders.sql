create table maintenance_reminders (
    id uuid primary key,
    vehicle_id uuid not null,
    title varchar(240) not null,
    description text,
    type varchar(32) not null,
    due_date date,
    due_odometer_km integer,
    status varchar(32) not null,
    created_at timestamp with time zone not null,
    updated_at timestamp with time zone not null,
    completed_at timestamp with time zone,
    cancelled_at timestamp with time zone,
    constraint fk_maintenance_reminders_vehicle foreign key (vehicle_id) references vehicles (id)
);

create index idx_maintenance_reminders_vehicle_id on maintenance_reminders (vehicle_id);
create index idx_maintenance_reminders_vehicle_status on maintenance_reminders (vehicle_id, status);
create index idx_maintenance_reminders_due_date on maintenance_reminders (due_date);
create index idx_maintenance_reminders_due_odometer_km on maintenance_reminders (due_odometer_km);
