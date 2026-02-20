ALTER TABLE tickets
    ADD COLUMN floor_id integer;

ALTER TABLE tickets
    ADD CONSTRAINT fk_tickets_floor
        FOREIGN KEY (floor_id) REFERENCES floors (id);

CREATE INDEX ix_tickets_floor_id ON tickets (floor_id);