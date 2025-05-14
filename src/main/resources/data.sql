-- H2 SQL for Hotel Booking Application (INSERTS ONLY)

INSERT INTO users VALUES
  ('11111111-1111-1111-1111-111111111111', 'Alice', 'alice@example.com', 'pass123', '1990-01-01', '1234567890', 'ADMIN', CURRENT_TIMESTAMP),
  ('22222222-2222-2222-2222-222222222222', 'Bob', 'bob@example.com', 'pass456', '1985-05-05', '0987654321', 'CUSTOMER', CURRENT_TIMESTAMP),
  ('33333333-3333-3333-3333-333333333333', 'Charlie', 'charlie@example.com', 'pass789', '1992-03-15', '1112223333', 'HOTEL_OWNER', CURRENT_TIMESTAMP),
  ('44444444-4444-4444-4444-444444444444', 'Diana', 'diana@example.com', 'pass321', '1988-07-22', '2223334444', 'CUSTOMER', CURRENT_TIMESTAMP),
  ('55555555-5555-5555-5555-555555555555', 'Eve', 'eve@example.com', 'pass654', '1995-12-10', '3334445555', 'CUSTOMER', CURRENT_TIMESTAMP);

INSERT INTO amenity VALUES
  ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 'WiFi', 'Free wireless internet', 'wifi.png'),
  ('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', 'Pool', 'Swimming pool', 'pool.png'),
  ('cccccccc-cccc-cccc-cccc-cccccccccccc', 'Gym', 'Fitness center', 'gym.png'),
  ('dddddddd-dddd-dddd-dddd-dddddddddddd', 'Spa', 'Relaxing spa', 'spa.png'),
  ('eeeeeeee-eeee-eeee-eeee-eeeeeeeeeeee', 'Restaurant', 'In-house restaurant', 'restaurant.png');

INSERT INTO hotel VALUES
  ('aaaa1111-1111-1111-1111-111111111111', '33333333-3333-3333-3333-333333333333', 'Grand Hotel', 'A luxury hotel', '123 Main St', 'Metropolis', 'StateX', 'CountryY', 12.971599, 77.594566, CURRENT_TIMESTAMP, TRUE),
  ('bbbb2222-2222-2222-2222-222222222222', '33333333-3333-3333-3333-333333333333', 'City Inn', 'Comfortable city hotel', '456 City Rd', 'Gotham', 'StateY', 'CountryY', 13.082680, 80.270718, CURRENT_TIMESTAMP, TRUE),
  ('cccc3333-3333-3333-3333-333333333333', '33333333-3333-3333-3333-333333333333', 'Beach Resort', 'Resort by the beach', '789 Beach Ave', 'Coastline', 'StateZ', 'CountryY', 9.931233, 76.267304, CURRENT_TIMESTAMP, TRUE),
  ('dddd4444-4444-4444-4444-444444444444', '33333333-3333-3333-3333-333333333333', 'Mountain Lodge', 'Lodge in the mountains', '321 Hilltop', 'Highland', 'StateA', 'CountryY', 27.175015, 78.042155, CURRENT_TIMESTAMP, FALSE),
  ('eeee5555-5555-5555-5555-555555555555', '33333333-3333-3333-3333-333333333333', 'Airport Hotel', 'Hotel near the airport', '654 Runway Rd', 'Aerotown', 'StateB', 'CountryY', 19.076090, 72.877426, CURRENT_TIMESTAMP, TRUE);

INSERT INTO room VALUES
  ('aaaaaaa1-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 'aaaa1111-1111-1111-1111-111111111111', 'DELUXE', 2, 150.00, 10),
  ('bbbbbbb2-bbbb-bbbb-bbbb-bbbbbbbbbbbb', 'bbbb2222-2222-2222-2222-222222222222', 'SINGLE', 1, 80.00, 5),
  ('ccccccc3-cccc-cccc-cccc-cccccccccccc', 'cccc3333-3333-3333-3333-333333333333', 'SUITE', 4, 300.00, 3),
  ('ddddddd4-dddd-dddd-dddd-dddddddddddd', 'dddd4444-4444-4444-4444-444444444444', 'DOUBLE', 2, 120.00, 8),
  ('eeeeeee5-eeee-eeee-eeee-eeeeeeeeeeee', 'eeee5555-5555-5555-5555-555555555555', 'FAMILY', 5, 200.00, 2);

INSERT INTO room_amenity VALUES
  ('aaaaaaa1-aaaa-aaaa-aaaa-aaaaaaaaaa01', 'aaaaaaa1-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa'),
  ('bbbbbbb2-bbbb-bbbb-bbbb-bbbbbbbbbb02', 'bbbbbbb2-bbbb-bbbb-bbbb-bbbbbbbbbbbb', 'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb'),
  ('ccccccc3-cccc-cccc-cccc-cccccccccc03', 'ccccccc3-cccc-cccc-cccc-cccccccccccc', 'cccccccc-cccc-cccc-cccc-cccccccccccc'),
  ('ddddddd4-dddd-dddd-dddd-dddddddddd04', 'ddddddd4-dddd-dddd-dddd-dddddddddddd', 'dddddddd-dddd-dddd-dddd-dddddddddddd'),
  ('eeeeeee5-eeee-eeee-eeee-eeeeeeeeee05', 'eeeeeee5-eeee-eeee-eeee-eeeeeeeeeeee', 'eeeeeeee-eeee-eeee-eeee-eeeeeeeeeeee');

INSERT INTO room_availability VALUES
  ('aaaaaaa1-aaaa-aaaa-aaaa-aaaaaaaaaa11', 'aaaaaaa1-aaaa-aaaa-aaaa-aaaaaaaaaaaa', '2024-06-01', 5),
  ('bbbbbbb2-bbbb-bbbb-bbbb-bbbbbbbbbb12', 'bbbbbbb2-bbbb-bbbb-bbbb-bbbbbbbbbbbb', '2024-06-02', 2),
  ('ccccccc3-cccc-cccc-cccc-cccccccccc13', 'ccccccc3-cccc-cccc-cccc-cccccccccccc', '2024-06-03', 1),
  ('ddddddd4-dddd-dddd-dddd-dddddddddd14', 'ddddddd4-dddd-dddd-dddd-dddddddddddd', '2024-06-04', 4),
  ('eeeeeee5-eeee-eeee-eeee-eeeeeeeeee15', 'eeeeeee5-eeee-eeee-eeee-eeeeeeeeeeee', '2024-06-05', 0);

INSERT INTO booking VALUES
  ('aaaaaaa1-aaaa-aaaa-aaaa-aaaaaaaaab01', '22222222-2222-2222-2222-222222222222', 'aaaaaaa1-aaaa-aaaa-aaaa-aaaaaaaaaaaa', '2024-06-10', '2024-06-12', 2, 300.00, 'CONFIRMED', CURRENT_TIMESTAMP),
  ('bbbbbbb2-bbbb-bbbb-bbbb-bbbbbbbbbb02', '33333333-3333-3333-3333-333333333333', 'bbbbbbb2-bbbb-bbbb-bbbb-bbbbbbbbbbbb', '2024-06-15', '2024-06-16', 1, 80.00, 'CANCELLED', CURRENT_TIMESTAMP),
  ('ccccccc3-cccc-cccc-cccc-cccccccccc03', '44444444-4444-4444-4444-444444444444', 'ccccccc3-cccc-cccc-cccc-cccccccccccc', '2024-06-20', '2024-06-22', 4, 600.00, 'CONFIRMED', CURRENT_TIMESTAMP),
  ('ddddddd4-dddd-dddd-dddd-dddddddddd04', '55555555-5555-5555-5555-555555555555', 'ddddddd4-dddd-dddd-dddd-dddddddddddd', '2024-06-25', '2024-06-27', 2, 240.00, 'PENDING', CURRENT_TIMESTAMP),
  ('eeeeeee5-eeee-eeee-eeee-eeeeeeeeee05', '11111111-1111-1111-1111-111111111111', 'eeeeeee5-eeee-eeee-eeee-eeeeeeeeeeee', '2024-07-01', '2024-07-03', 5, 400.00, 'CONFIRMED', CURRENT_TIMESTAMP);

INSERT INTO payment VALUES
  ('aaaaaaa1-aaaa-aaaa-aaaa-aaaaaaaaac01', 'aaaaaaa1-aaaa-aaaa-aaaa-aaaaaaaaab01', '22222222-2222-2222-2222-222222222222', 300.00, CURRENT_TIMESTAMP, 'CARD', 'SUCCESS'),
  ('bbbbbbb2-bbbb-bbbb-bbbb-bbbbbbbbbb02', 'bbbbbbb2-bbbb-bbbb-bbbb-bbbbbbbbbb02', '33333333-3333-3333-3333-333333333333', 80.00, CURRENT_TIMESTAMP, 'CASH', 'FAILED'),
  ('ccccccc3-cccc-cccc-cccc-cccccccccc03', 'ccccccc3-cccc-cccc-cccc-cccccccccc03', '44444444-4444-4444-4444-444444444444', 600.00, CURRENT_TIMESTAMP, 'CARD', 'SUCCESS'),
  ('ddddddd4-dddd-dddd-dddd-dddddddddd04', 'ddddddd4-dddd-dddd-dddd-dddddddddd04', '55555555-5555-5555-5555-555555555555', 240.00, CURRENT_TIMESTAMP, 'UPI', 'PENDING'),
  ('eeeeeee5-eeee-eeee-eeee-eeeeeeeeee05', 'eeeeeee5-eeee-eeee-eeee-eeeeeeeeee05', '11111111-1111-1111-1111-111111111111', 400.00, CURRENT_TIMESTAMP, 'CARD', 'SUCCESS');

INSERT INTO pricing_rule (rule_id, hotel_id, rule_type, rule_value, start_date, end_date) VALUES
  ('aaaaaaa1-aaaa-aaaa-aaaa-aaaaaaaaad01', 'aaaa1111-1111-1111-1111-111111111111', 'SEASONAL', '10% off', '2024-06-01', '2024-06-30'),
  ('bbbbbbb2-bbbb-bbbb-bbbb-bbbbbbbbbb02', 'bbbb2222-2222-2222-2222-222222222222', 'WEEKEND', '5% off', '2024-07-01', '2024-07-31'),
  ('ccccccc3-cccc-cccc-cccc-cccccccccc03', 'cccc3333-3333-3333-3333-333333333333', 'LAST_MINUTE', '15% off', '2024-08-01', '2024-08-15'),
  ('ddddddd4-dddd-dddd-dddd-dddddddddd04', 'dddd4444-4444-4444-4444-444444444444', 'DISCOUNT', '20% off', '2024-09-01', '2024-09-30'),
  ('eeeeeee5-eeee-eeee-eeee-eeeeeeeeee05', 'eeee5555-5555-5555-5555-555555555555', 'PEAK', 'No discount', '2024-10-01', '2024-10-31');

INSERT INTO review VALUES
  ('aaaaaaa1-aaaa-aaaa-aaaa-aaaaaaaaae01', '22222222-2222-2222-2222-222222222222', 'aaaa1111-1111-1111-1111-111111111111', 5, 'Great stay!', CURRENT_TIMESTAMP),
  ('bbbbbbb2-bbbb-bbbb-bbbb-bbbbbbbbbb02', '33333333-3333-3333-3333-333333333333', 'bbbb2222-2222-2222-2222-222222222222', 4, 'Nice location.', CURRENT_TIMESTAMP),
  ('ccccccc3-cccc-cccc-cccc-cccccccccc03', '44444444-4444-4444-4444-444444444444', 'cccc3333-3333-3333-3333-333333333333', 3, 'Good service.', CURRENT_TIMESTAMP),
  ('ddddddd4-dddd-dddd-dddd-dddddddddd04', '55555555-5555-5555-5555-555555555555', 'dddd4444-4444-4444-4444-444444444444', 2, 'Could be better.', CURRENT_TIMESTAMP),
  ('eeeeeee5-eeee-eeee-eeee-eeeeeeeeee05', '11111111-1111-1111-1111-111111111111', 'eeee5555-5555-5555-5555-555555555555', 5, 'Excellent!', CURRENT_TIMESTAMP);

INSERT INTO image VALUES
  ('aaaaaaa1-aaaa-aaaa-aaaa-aaaaaaaaaf01', 'HOTEL', 'aaaa1111-1111-1111-1111-111111111111', 'hotel1.png', CURRENT_TIMESTAMP),
  ('bbbbbbb2-bbbb-bbbb-bbbb-bbbbbbbbbb02', 'ROOM', 'bbbbbbb2-bbbb-bbbb-bbbb-bbbbbbbbbbbb', 'room2.png', CURRENT_TIMESTAMP),
  ('ccccccc3-cccc-cccc-cccc-cccccccccc03', 'AMENITY', 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 'wifi.png', CURRENT_TIMESTAMP),
  ('ddddddd4-dddd-dddd-dddd-dddddddddd04', 'HOTEL', 'cccc3333-3333-3333-3333-333333333333', 'hotel3.png', CURRENT_TIMESTAMP),
  ('eeeeeee5-eeee-eeee-eeee-eeeeeeeeee05', 'ROOM', 'eeeeeee5-eeee-eeee-eeee-eeeeeeeeeeee', 'room5.png', CURRENT_TIMESTAMP);