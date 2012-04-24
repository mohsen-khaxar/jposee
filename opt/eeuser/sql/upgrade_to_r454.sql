drop table if exists eeuser_perms;

create table eeuser_roles (
    eeuser bigint not null, 
    role bigint not null, 
    primary key (eeuser, role)
) ENGINE=InnoDB;

create table role (
    id bigint not null auto_increment, 
    name varchar(64) not null unique, 
    primary key (id)
) ENGINE=InnoDB;

create table role_perms (
    role bigint not null, 
    name varchar(64) not null, 
    primary key (role, name)
) ENGINE=InnoDB;

alter table eeuser_roles add index FKUserRolesUser (eeuser), add constraint FKUserRolesUser foreign key (eeuser) references eeuser (id);
alter table eeuser_roles add index FKUserRolesRole (role), add constraint FKUserRolesRole foreign key (role) references role (id);
alter table role_perms add index FKRolePermissions (role), add constraint FKRolePermissions foreign key (role) references role (id);

DELETE FROM sysconfig WHERE id LIKE 'perm.%';

INSERT INTO sysconfig (id, value) VALUES ('perm.gl.read', 'General Ledger Read');
INSERT INTO sysconfig (id, value) VALUES ('perm.gl.write', 'General Ledger Write');
INSERT INTO sysconfig (id, value) VALUES ('perm.gl.post', 'General Ledger Post');
INSERT INTO sysconfig (id, value) VALUES ('perm.gl.checkpoint', 'General Ledger Checkpoint');
INSERT INTO sysconfig (id, value) VALUES ('perm.gl.summarize', 'General Ledger Summarize');
INSERT INTO sysconfig (id, value) VALUES ('perm.gl.grant', 'General Ledger Grant');

INSERT INTO sysconfig (id, value) VALUES ('perm.jcard.read', 'Read permission on jcard entities');
INSERT INTO sysconfig (id, value) VALUES ('perm.jcard.write', 'Write permission on jcard entities');
INSERT INTO sysconfig (id, value) VALUES ('perm.tranlog', 'Transaction Log');

INSERT INTO sysconfig (id, value) VALUES ('perm.login', 'Login');
INSERT INTO sysconfig (id, value) VALUES ('perm.systemstatus', 'Access to System Status');
INSERT INTO sysconfig (id, value) VALUES ('perm.sysconfig', 'View and edit System Configuration');
INSERT INTO sysconfig (id, value) VALUES ('perm.users.read', 'Read permission on Users');
INSERT INTO sysconfig (id, value) VALUES ('perm.users.write', 'Write permission on Users');

INSERT INTO sysconfig (id, value) VALUES ('perm.search.card', 'Search for a card functionality');
INSERT INTO sysconfig (id, value) VALUES ('perm.search.cardproduct', 'Search for a card product functionality');
INSERT INTO sysconfig (id, value) VALUES ('perm.card.information', 'Access to card information');
INSERT INTO sysconfig (id, value) VALUES ('perm.card.balance', 'Access to card balance');
INSERT INTO sysconfig (id, value) VALUES ('perm.card.transactions', 'Access to transactions made by card');
INSERT INTO sysconfig (id, value) VALUES ('perm.balances', 'Balances view');
INSERT INTO sysconfig (id, value) VALUES ('perm.transactions', 'Transactions view');


INSERT INTO role (name) VALUES ('Operator');
SET @operator = (SELECT id FROM role WHERE name = 'Operator');  
INSERT INTO role (name) VALUES ('System Administrator');
SET @sysadmin = (SELECT id FROM role WHERE name = 'System Administrator');
INSERT INTO role (name) VALUES ('jCard Administrator');
SET @jcardadmin = (SELECT id FROM role WHERE name = 'jCard Administrator');
INSERT INTO role (name) VALUES ('Book keeper');
SET @bookkeeper = (SELECT id FROM role WHERE name = 'Book keeper');
INSERT INTO role (name) VALUES ('Accountant');
SET @accountant = (SELECT id FROM role WHERE name = 'Accountant');
INSERT INTO role (name) VALUES ('Admin');
SET @admin = (SELECT id FROM role WHERE name = 'Admin');

INSERT INTO role_perms (role, name) VALUES (@operator, 'login');
INSERT INTO role_perms (role, name) VALUES (@operator, 'systemstatus');

INSERT INTO role_perms (role, name) VALUES (@sysadmin, 'login');
INSERT INTO role_perms (role, name) VALUES (@sysadmin, 'sysconfig');
INSERT INTO role_perms (role, name) VALUES (@sysadmin, 'users.read');
INSERT INTO role_perms (role, name) VALUES (@sysadmin, 'users.write');

INSERT INTO role_perms (role, name) VALUES (@jcardadmin, 'login');
INSERT INTO role_perms (role, name) VALUES (@jcardadmin, 'jcard.read');
INSERT INTO role_perms (role, name) VALUES (@jcardadmin, 'jcard.write');
INSERT INTO role_perms (role, name) VALUES (@jcardadmin, 'tranlog');
INSERT INTO role_perms (role, name) VALUES (@jcardadmin, 'gl.read');

INSERT INTO role_perms (role, name) VALUES (@bookkeeper, 'login');
INSERT INTO role_perms (role, name) VALUES (@bookkeeper, 'gl.read');
INSERT INTO role_perms (role, name) VALUES (@bookkeeper, 'tranlog');
INSERT INTO role_perms (role, name) VALUES (@bookkeeper, 'search.card');
INSERT INTO role_perms (role, name) VALUES (@bookkeeper, 'search.cardproduct');
INSERT INTO role_perms (role, name) VALUES (@bookkeeper, 'card.information');
INSERT INTO role_perms (role, name) VALUES (@bookkeeper, 'card.transactions');
INSERT INTO role_perms (role, name) VALUES (@bookkeeper, 'card.balance');
INSERT INTO role_perms (role, name) VALUES (@bookkeeper, 'balances');
INSERT INTO role_perms (role, name) VALUES (@bookkeeper, 'transactions');

INSERT INTO role_perms (role, name) VALUES (@accountant, 'login');
INSERT INTO role_perms (role, name) VALUES (@accountant, 'gl.read');
INSERT INTO role_perms (role, name) VALUES (@accountant, 'gl.write');
INSERT INTO role_perms (role, name) VALUES (@accountant, 'gl.post');
INSERT INTO role_perms (role, name) VALUES (@accountant, 'tranlog');
INSERT INTO role_perms (role, name) VALUES (@accountant, 'search.card');
INSERT INTO role_perms (role, name) VALUES (@accountant, 'search.cardproduct');
INSERT INTO role_perms (role, name) VALUES (@accountant, 'card.information');
INSERT INTO role_perms (role, name) VALUES (@accountant, 'card.transactions');
INSERT INTO role_perms (role, name) VALUES (@accountant, 'card.balance');
INSERT INTO role_perms (role, name) VALUES (@accountant, 'balances');
INSERT INTO role_perms (role, name) VALUES (@accountant, 'transactions');

INSERT INTO role_perms (role, name) VALUES (@admin, 'login');
INSERT INTO role_perms (role, name) VALUES (@admin, 'systemstatus');
INSERT INTO role_perms (role, name) VALUES (@admin, 'sysconfig');
INSERT INTO role_perms (role, name) VALUES (@admin, 'users.read');
INSERT INTO role_perms (role, name) VALUES (@admin, 'users.write');
INSERT INTO role_perms (role, name) VALUES (@admin, 'gl.read');
INSERT INTO role_perms (role, name) VALUES (@admin, 'gl.write');
INSERT INTO role_perms (role, name) VALUES (@admin, 'gl.post');
INSERT INTO role_perms (role, name) VALUES (@admin, 'gl.checkpoint');
INSERT INTO role_perms (role, name) VALUES (@admin, 'gl.summarize');
INSERT INTO role_perms (role, name) VALUES (@admin, 'gl.grant');
INSERT INTO role_perms (role, name) VALUES (@admin, 'jcard.read');
INSERT INTO role_perms (role, name) VALUES (@admin, 'jcard.write');
INSERT INTO role_perms (role, name) VALUES (@admin, 'tranlog');
INSERT INTO role_perms (role, name) VALUES (@admin, 'search.card');
INSERT INTO role_perms (role, name) VALUES (@admin, 'search.cardproduct');
INSERT INTO role_perms (role, name) VALUES (@admin, 'card.information');
INSERT INTO role_perms (role, name) VALUES (@admin, 'card.balance');
INSERT INTO role_perms (role, name) VALUES (@admin, 'card.transactions');
INSERT INTO role_perms (role, name) VALUES (@admin, 'balances');
INSERT INTO role_perms (role, name) VALUES (@admin, 'transactions');


INSERT INTO role (name) VALUES ('Call Center');
SET @callcenter = (SELECT id FROM role WHERE name = 'Call Center');
INSERT INTO role (name) VALUES ('Call Center Admin');
SET @callcenteradmin = (SELECT id FROM role WHERE name = 'Call Center Admin');
INSERT INTO role (name) VALUES ('CardProduct Manager');
SET @cardprodmgr = (SELECT id FROM role WHERE name = 'CardProduct Manager');

INSERT INTO role_perms (role, name) VALUES (@callcenter, 'login');
INSERT INTO role_perms (role, name) VALUES (@callcenter, 'search.card');
INSERT INTO role_perms (role, name) VALUES (@callcenter, 'card.balance');
INSERT INTO role_perms (role, name) VALUES (@callcenter, 'card.transactions');
INSERT INTO role_perms (role, name) VALUES (@callcenter, 'gl.read');

INSERT INTO role_perms (role, name) VALUES (@callcenteradmin, 'login');
INSERT INTO role_perms (role, name) VALUES (@callcenteradmin, 'search.card');
INSERT INTO role_perms (role, name) VALUES (@callcenteradmin, 'card.information');
INSERT INTO role_perms (role, name) VALUES (@callcenteradmin, 'card.balance');
INSERT INTO role_perms (role, name) VALUES (@callcenteradmin, 'card.transactions');
INSERT INTO role_perms (role, name) VALUES (@callcenteradmin, 'gl.read');
INSERT INTO role_perms (role, name) VALUES (@callcenteradmin, 'gl.write');

INSERT INTO role_perms (role, name) VALUES (@cardprodmgr, 'login');
INSERT INTO role_perms (role, name) VALUES (@cardprodmgr, 'search.cardproduct');
INSERT INTO role_perms (role, name) VALUES (@cardprodmgr, 'card.transactions');
INSERT INTO role_perms (role, name) VALUES (@cardprodmgr, 'gl.read');

-- Update SCHEMA_REVISION.eeuser
DELETE FROM sysconfig WHERE id = 'SCHEMA_REVISION.eeuser';
INSERT INTO sysconfig (id, value, readPerm) VALUES ('SCHEMA_REVISION.eeuser', '454', 'admin');

