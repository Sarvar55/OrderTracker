alter table users add column role varchar(30) not null default 'CUSTOMER';
alter table users add constraint ck_users_role check (role in ('CUSTOMER', 'ADMIN'));