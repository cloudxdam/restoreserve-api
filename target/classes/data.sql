INSERT INTO restaurant_tables (name, max_pax, status, location)
VALUES ('Mesa 1', 4, 'AVAILABLE', 'SALON');

INSERT INTO restaurant_tables (name, max_pax, status, location)
VALUES ('Mesa 2', 2, 'RESERVED', 'TERRACE');

INSERT INTO restaurant_tables (name, max_pax, status, location)
VALUES ('Mesa 3', 6, 'AVAILABLE', 'SALON');

INSERT INTO restaurant_tables (name, max_pax, status, location)
VALUES ('Mesa 4', 8, 'RESERVED', 'TERRACE');

 -- test credentials-> username: armymoves - password: 1234
INSERT INTO users (name, telephone, email, user_name, password, role, active)
VALUES ('Armiche Santana','6123456789','armiche@gmail.com','armymoves', '$2a$12$UfijVlkvrmwpTGERWsfsCe.R5ZIPaJPCP7TmjsnRMYtE8USwgsixi','ROLE_USER',true
);

-- test credentials-> username: admin - password: Admin1234
INSERT INTO users (name, telephone, email, user_name, password, role, active)
VALUES ('Acaymo Bello','600000000','acaymo@gmail.com','admin', '$2a$12$lAUz1WTEed/7SOsy.ew.OehjCeESngVWkgkc0fqRgZqeo/W4AwM..','ROLE_ADMIN',true
);

INSERT INTO reservations (user_id, table_id, reservation_date, created_at, number_of_guests, status)
VALUES (1, 1,'2026-05-16T20:00:00', CURRENT_TIMESTAMP, 2,'CONFIRMED'
);

INSERT INTO reservations (user_id, table_id, reservation_date, created_at, number_of_guests, status) 
VALUES (2, 2, '2026-05-16T15:00:00', CURRENT_TIMESTAMP, 2, 'CONFIRMED'
);