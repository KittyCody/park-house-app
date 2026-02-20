UPDATE tickets
SET floor_id = (SELECT id FROM floors LIMIT 1)
WHERE floor_id IS NULL;

ALTER TABLE tickets
    ALTER COLUMN floor_id SET NOT NULL;