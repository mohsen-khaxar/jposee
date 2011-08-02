ALTER TABLE eeuser ADD email VARCHAR(255);
ALTER TABLE eeuser ADD verified char(1) DEFAULT 'N';
ALTER TABLE eeuser CHANGE active active char(1) DEFAULT 'N';
ALTER TABLE eeuser CHANGE deleted deleted char(1) DEFAULT 'N';

INSERT INTO sysconfig (id, value, readPerm) VALUES ('SCHEMA_REVISION.eeuser', '432', 'admin');

