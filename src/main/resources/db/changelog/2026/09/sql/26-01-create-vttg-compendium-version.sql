create table vttg_compendium_version
(
    id         smallint    not null,
    version    int         not null,
    updated_at timestamptz,
    updated_by varchar(255),
    constraint pk_vttg_compendium_version primary key (id),
    constraint ck_vttg_compendium_version_single_row check (id = 1),
    constraint ck_vttg_compendium_version_non_negative check (version >= 0)
);

insert into vttg_compendium_version (id, version) values (1, 35);
