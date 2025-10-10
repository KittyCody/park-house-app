-- 1) remove any authorizations for that client
DELETE FROM oauth2_authorization
WHERE registered_client_id IN (
    SELECT id FROM oauth2_registered_client WHERE client_id = 'parking-client'
);

-- 2) remove any consents for that client (if you use them)
DELETE FROM oauth2_authorization_consent
WHERE registered_client_id IN (
    SELECT id FROM oauth2_registered_client WHERE client_id = 'parking-client'
);

-- 3) finally, remove the client row
DELETE FROM oauth2_registered_client WHERE client_id = 'parking-client';
