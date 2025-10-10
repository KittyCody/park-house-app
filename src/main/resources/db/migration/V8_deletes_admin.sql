DELETE FROM user_roles ur
WHERE ur.user_id = (SELECT id FROM users WHERE username = 'admin_ph');

DELETE FROM users WHERE username = 'admin_ph';
