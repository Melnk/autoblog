create table user_accounts (
    id uuid primary key,
    email varchar(320) not null,
    password_hash varchar(255) not null,
    display_name varchar(240),
    status varchar(32) not null,
    created_at timestamp with time zone not null,
    updated_at timestamp with time zone not null,
    constraint uk_user_accounts_email unique (email)
);

create index idx_user_accounts_email on user_accounts (email);

create table vehicle_access (
    id uuid primary key,
    vehicle_id uuid not null,
    user_id uuid not null,
    role varchar(32) not null,
    created_at timestamp with time zone not null,
    constraint fk_vehicle_access_vehicle foreign key (vehicle_id) references vehicles (id),
    constraint fk_vehicle_access_user foreign key (user_id) references user_accounts (id),
    constraint uk_vehicle_access_vehicle_user unique (vehicle_id, user_id)
);

create index idx_vehicle_access_vehicle_id on vehicle_access (vehicle_id);
create index idx_vehicle_access_user_id on vehicle_access (user_id);
create index idx_vehicle_access_vehicle_role on vehicle_access (vehicle_id, role);
