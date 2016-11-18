-- :name create-user! :! :n
-- :doc creates a new user record
INSERT INTO users
(id, pass)
VALUES (:id, :pass)

-- :name update-user! :! :n
-- :doc update an existing user record
UPDATE users
SET first_name = :first_name, last_name = :last_name, email = :email
WHERE id = :id

-- :name get-user :? :1
-- :doc retrieve a user given the id.
SELECT * FROM users
WHERE id = :id

-- :name delete-user! :! :n
-- :doc delete a user given the id
DELETE FROM users
WHERE id = :id

-- :name save-file! :! :n
-- :doc saves a file to the database
INSERT INTO files
(owner, type, name, data)
VALUES (:owner, :type, :name, :data)

-- :name list-thumbnails :? :*
-- :doc selects thumbnail names for the given gallery owner
SELECT owner, name FROM files
WHERE owner = :owner
AND name LIKE 'thumb-%'

-- :name get-image
-- :doc retrive image data by name
SELECT type, data FROM files
WHERE name = :name

-- :name select-gallery-previews
-- :doc selects a thumbanail for each user gallery
WITH summary AS (
SELECT f.owner, f.name, ROW_NUMBER() OVER(PARTITION BY f.owner ORDER BY f.name DESC) AS rk
FROM files f WHERE name like 'thumb-%')
SELECT s.*
FROM summary s
WHERE s.rk = 1

-- :name delete-file! :! :n
-- :doc deletes the file with the given name and owner
DELETE FROM files
WHERE name = :name
AND owner = :owner
