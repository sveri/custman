CREATE TABLE address (
id bigserial NOT NULL PRIMARY KEY,
country character varying(255),
state character varying(255),
city character varying(255),
street character varying(255),
house_number character varying(30),
plz character varying(30),
adress_type character varying(30),
users_id bigint,
customer_id bigint
);

ALTER TABLE address OWNER TO patman;

CREATE INDEX address_users_index ON address USING btree (users_id);

CREATE INDEX address_customer_index ON address USING btree (customer_id);


ALTER TABLE address ADD CONSTRAINT user_or_customer_must_be_set
CHECK ( users_id IS NOT NULL OR customer_id IS NOT NULL) NOT VALID;

ALTER TABLE address ADD COLUMN handy1 CHARACTER VARYING(100);

ALTER TABLE address ADD COLUMN handy2 CHARACTER VARYING(100);

ALTER TABLE address ADD COLUMN landline CHARACTER VARYING(100);

ALTER TABLE address ADD COLUMN email CHARACTER VARYING(100);