-- Spring Authorization Server tables (PostgreSQL)

create table if not exists oauth2_registered_client
(
    id                            varchar(100) primary key,
    client_id                     varchar(100)  not null unique,
    client_id_issued_at           timestamp     not null,
    client_secret                 varchar(200),
    client_secret_expires_at      timestamp,
    client_name                   varchar(200)  not null,
    client_authentication_methods varchar(1000) not null,
    authorization_grant_types     varchar(1000) not null,
    redirect_uris                 varchar(1000),
    scopes                        varchar(1000) not null,
    client_settings               varchar(2000) not null,
    token_settings                varchar(2000) not null
);

create table if not exists oauth2_authorization_consent
(
    registered_client_id varchar(100)  not null,
    principal_name       varchar(200)  not null,
    authorities          varchar(1000) not null,
    primary key (registered_client_id, principal_name)
);

create table if not exists oauth2_authorization
(
    id                            varchar(100) primary key,
    registered_client_id          varchar(100) not null,
    principal_name                varchar(200) not null,
    authorization_grant_type      varchar(100) not null,
    authorized_scopes             varchar(1000),
    attributes                    text,

    state                         varchar(500),

    authorization_code_value      text,
    authorization_code_issued_at  timestamp,
    authorization_code_expires_at timestamp,
    authorization_code_metadata   text,

    access_token_value            text,
    access_token_issued_at        timestamp,
    access_token_expires_at       timestamp,
    access_token_metadata         text,
    access_token_type             varchar(100),
    access_token_scopes           varchar(1000),

    oidc_id_token_value           text,
    oidc_id_token_issued_at       timestamp,
    oidc_id_token_expires_at      timestamp,
    oidc_id_token_metadata        text,

    refresh_token_value           text,
    refresh_token_issued_at       timestamp,
    refresh_token_expires_at      timestamp,
    refresh_token_metadata        text,

    user_code_value               text,
    user_code_issued_at           timestamp,
    user_code_expires_at          timestamp,
    user_code_metadata            text,

    device_code_value             text,
    device_code_issued_at         timestamp,
    device_code_expires_at        timestamp,
    device_code_metadata          text
);

create index if not exists idx_oauth2_auth_registered_client_id on oauth2_authorization (registered_client_id);
create index if not exists idx_oauth2_auth_principal_name on oauth2_authorization (principal_name);
create index if not exists idx_oauth2_auth_state on oauth2_authorization (state);
create index if not exists idx_oauth2_auth_auth_code on oauth2_authorization (authorization_code_value);
create index if not exists idx_oauth2_auth_access_token on oauth2_authorization (access_token_value);
create index if not exists idx_oauth2_auth_refresh_token on oauth2_authorization (refresh_token_value);
create index if not exists idx_oauth2_auth_user_code on oauth2_authorization (user_code_value);
create index if not exists idx_oauth2_auth_device_code on oauth2_authorization (device_code_value);
