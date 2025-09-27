INSERT INTO user_groups (name, description, created_at, updated_at)
VALUES ('Team A','Alpha team', NOW(), NOW())
ON DUPLICATE KEY UPDATE name = VALUES(name);

INSERT INTO user_groups (name, description, created_at, updated_at)
VALUES ('Team B','Beta team', NOW(), NOW())
ON DUPLICATE KEY UPDATE name = VALUES(name);

INSERT INTO user_groups (name, description, created_at, updated_at)
VALUES ('Default','Default group', NOW(), NOW())
ON DUPLICATE KEY UPDATE name = VALUES(name);

INSERT INTO task (title, description, status, type, assigned_to, group_id, created_on, updated_on)
SELECT 'Task A1','Visible only to Team A','OPEN','TASK',NULL,
       (SELECT id FROM user_groups WHERE name='Team A'), NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM task WHERE title='Task A1');

INSERT INTO task (title, description, status, type, assigned_to, group_id, created_on, updated_on)
SELECT 'Task B1','Visible only to Team B','OPEN','BUG',NULL,
       (SELECT id FROM user_groups WHERE name='Team B'), NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM task WHERE title='Task B1');