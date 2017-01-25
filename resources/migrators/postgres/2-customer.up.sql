CREATE TABLE customer (
id bigserial NOT NULL PRIMARY KEY,
first_name character varying(50),
last_name character varying(50),
birthday timestamp without time zone NOT NULL,
gender character varying(50),
users_id bigint NOT NULL
);

ALTER TABLE customer OWNER TO custman;

CREATE INDEX customer_users_index ON customer USING btree (users_id);
