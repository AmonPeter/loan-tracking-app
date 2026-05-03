UPDATE users
SET role = 'ADMIN'
WHERE lower(email) = 'user@example.com';
