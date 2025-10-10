ALTER TABLE oauth2_registered_client
    ADD COLUMN IF NOT EXISTS post_logout_redirect_uris varchar(1000);