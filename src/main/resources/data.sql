INSERT INTO restaurant_tables (name, max_pax, status, location)
VALUES ('Mesa 1', 4, 'AVAILABLE', 'SALON');

INSERT INTO restaurant_tables (name, max_pax, status, location)
VALUES ('Mesa 2', 2, 'RESERVED', 'TERRACE');

INSERT INTO restaurant_tables (name, max_pax, status, location)
VALUES ('Mesa 3', 6, 'AVAILABLE', 'SALON');

INSERT INTO restaurant_tables (name, max_pax, status, location)
VALUES ('Mesa 4', 8, 'RESERVED', 'TERRACE');

INSERT INTO users (name, telephone, email, password, role, active)
VALUES ('Armiche Santana','6123456789','armiche@gmail.com','1234','ROLE_USER',true
);

INSERT INTO users (name, telephone, email, password, role, active)
VALUES ('Acaymo Bello','600000000','acaymo@gmail.com','Admin1234','ROLE_ADMIN',true
);

INSERT INTO reservations (user_id, table_id, reservation_date, created_at, number_of_guests, status)
VALUES (1, 1,'2026-05-16T20:00:00', CURRENT_TIMESTAMP, 2,'CONFIRMED'
);

INSERT INTO reservations (user_id, table_id, reservation_date, created_at, number_of_guests, status) 
VALUES (2, 2, '2026-05-16T15:00:00', CURRENT_TIMESTAMP, 2, 'CONFIRMED'
);