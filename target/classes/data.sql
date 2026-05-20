INSERT INTO restaurant_tables (name, max_pax, status, location, is_vip)
VALUES ('Mesa 1', 4, 'AVAILABLE', 'SALON', false);

INSERT INTO restaurant_tables (name, max_pax, status, location, is_vip)
VALUES ('Mesa 2', 2, 'RESERVED', 'TERRACE', false);

INSERT INTO restaurant_tables (name, max_pax, status, location, is_vip)
VALUES ('Mesa 3', 6, 'AVAILABLE', 'SALON', false);

INSERT INTO restaurant_tables (name, max_pax, status, location, is_vip)
VALUES ('Mesa 4', 8, 'RESERVED', 'TERRACE', false);

 -- test credentials-> username: armymoves - password: 1234
INSERT INTO users (name, telephone, email, user_name, password, role, active, penalization_points, user_status)
VALUES ('Armiche Santana','6123456789','armiche@gmail.com','armymoves', '$2a$12$UfijVlkvrmwpTGERWsfsCe.R5ZIPaJPCP7TmjsnRMYtE8USwgsixi','ROLE_USER',true, 0, 'ACTIVE');

-- test credentials-> username: admin - password: Admin1234
INSERT INTO users (name, telephone, email, user_name, password, role, active, penalization_points, user_status)
VALUES ('Acaymo Bello','600000000','acaymo@gmail.com','admin', '$2a$12$lAUz1WTEed/7SOsy.ew.OehjCeESngVWkgkc0fqRgZqeo/W4AwM..','ROLE_ADMIN',true, 0, 'ACTIVE');

INSERT INTO reservations (user_id, table_id, reservation_date, created_at, number_of_guests, status)
VALUES (1, 1,'2026-05-16T20:00:00', CURRENT_TIMESTAMP, 2,'CONFIRMED');

INSERT INTO reservations (user_id, table_id, reservation_date, created_at, number_of_guests, status) 
VALUES (2, 2, '2026-05-16T15:00:00', CURRENT_TIMESTAMP, 2, 'CONFIRMED');