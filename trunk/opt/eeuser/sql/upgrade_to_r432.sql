ALTER TABLE eeuser ADD email VARCHAR(255);
ALTER TABLE eeuser ADD verified char(1) DEFAULT 'N';
ALTER TABLE eeuser CHANGE active active char(1) DEFAULT 'N';
ALTER TABLE eeuser CHANGE deleted deleted char(1) DEFAULT 'N';

DELETE FROM sysconfig WHERE id = 'SCHEMA_REVISION.eeuser';
INSERT INTO sysconfig (id, value, readPerm) VALUES ('SCHEMA_REVISION.eeuser', '432', 'admin');

