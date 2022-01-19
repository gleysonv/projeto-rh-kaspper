create table if not exists candidato
(
    cd_candidato  int auto_increment
        primary key,
    nm_candidato  varchar(50) null,
    vr_pretencao  int         null,
    aq_curriculum blob        null,
    cd_perfil     int         null
);

create table if not exists perfil
(
    cd_perfil     int auto_increment
        primary key,
    ds_perfil     varchar(50)  null,
    ds_observacao varchar(255) null
);

create table if not exists vaga
(
    cd_vaga   int auto_increment
        primary key,
    ds_vaga   varchar(50) null,
    tp_vaga   int         null,
    cd_perfil int         null
);


insert into perfil (cd_perfil, ds_perfil, ds_observacao)
values (1,'Junior','Desenvolvedor com at 2 anos de experincia');

insert into perfil (cd_perfil, ds_perfil, ds_observacao)
values (2,'Pleno','Desenvolvedor com mais de 2 anos de experincia');

insert into perfil (cd_perfil, ds_perfil, ds_observacao)
values (3,'Senior','Desenvolvedor com mais de 7 anos de experincia');


