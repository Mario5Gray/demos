DROP KEYSPACE IF EXISTS chat;

CREATE KEYSPACE chat WITH replication={'class' : 'SimpleStrategy', 'replication_factor':1};
USE chat;

CREATE TABLE chat_message(
id UUID,
user_id UUID,
room_id UUID,
text    varchar,
timestamp timestamp,
visible Boolean,
PRIMARY KEY (room_id));
