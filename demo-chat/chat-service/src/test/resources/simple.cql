DROP KEYSPACE IF EXISTS chat;

CREATE KEYSPACE chat WITH replication={'class' : 'SimpleStrategy', 'replication_factor':1};
USE chat;

CREATE TABLE chat_user(
id UUID,
handle varchar,
name varchar,
PRIMARY KEY(id, handle));

CREATE TABLE chat_message(
id UUID,
user_id UUID,
room_id UUID,
text    varchar,
visible Boolean,
PRIMARY KEY (room_id, visible));
