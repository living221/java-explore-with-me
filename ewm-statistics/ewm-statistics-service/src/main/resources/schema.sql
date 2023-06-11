DROP TABLE IF EXISTS endpoint_hit CASCADE;

CREATE TABLE IF NOT EXISTS endpoint_hit
(
    id        BIGINT GENERATED BY DEFAULT AS IDENTITY,
    app       VARCHAR(255)                NOT NULL,
    uri       VARCHAR(512)                NOT NULL,
    ip        VARCHAR(45)                 NOT NULL,
    timestamp TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    CONSTRAINT pk_endpoint_hit PRIMARY KEY (id)
);