-- Admin and User Seed Data
INSERT INTO users (id, name, email, phone, password, avatar, role, status, created_at) VALUES
(1, 'System Admin', 'admin@sportbooking.com', '+85512345678', '$2a$10$W2pEippYulSaHU81s98UB.Ys1/.4lNBF.kWI0kqjXfh1xxX8pW1a2', NULL, 'ADMIN', 'ACTIVE', NOW()),
(2, 'John Doe', 'user@sportbooking.com', '+85587654321', '$2a$10$kEHSL0VX6I6XRJnmRfWaFueDqjkEdQy0TB4G/bJxs6iERdMXwCnPy', NULL, 'USER', 'ACTIVE', NOW());

-- Categories Seed Data
INSERT INTO categories (id, name, image, description, status) VALUES
(1, 'Football', '/img/categories/football.jpg', 'Standard 11-a-side and 7-a-side outdoor natural and artificial turf football fields.', 'ACTIVE'),
(2, 'Futsal', '/img/categories/futsal.jpg', 'High-quality indoor and covered 5-a-side futsal pitches with tournament-grade turf.', 'ACTIVE'),
(3, 'Badminton', '/img/categories/badminton.jpg', 'Air-conditioned indoor badminton courts with BWF-standard synthetic rubber flooring.', 'ACTIVE'),
(4, 'Tennis', '/img/categories/tennis.jpg', 'Professional outdoor hard courts and clay courts with full evening lighting.', 'ACTIVE'),
(5, 'Basketball', '/img/categories/basketball.jpg', 'Indoor hardwood and outdoor acrylic basketball courts with regulation hoops.', 'ACTIVE'),
(6, 'Volleyball', '/img/categories/volleyball.jpg', 'Standard indoor volleyball courts and outdoor beach sand volleyball courts.', 'ACTIVE');

-- Fields Seed Data
INSERT INTO fields (id, category_id, name, description, price_per_slot, peak_price, peak_start_time, slot_minutes, open_time, close_time, deposit_percent, status, created_at) VALUES
-- Football fields
(1, 1, 'National Arena Field A (7-a-side)', 'Premium FIFA-standard synthetic turf, floodlights, team dugouts, and spectator benches.', 35.00, 45.00, '17:00:00', 60, '06:00:00', '23:00:00', 20, 'ACTIVE', NOW()),
(2, 1, 'National Arena Field B (11-a-side)', 'Full-size natural grass football pitch suitable for formal friendly matches and tournaments.', 60.00, 80.00, '17:00:00', 60, '06:00:00', '22:00:00', 30, 'ACTIVE', NOW()),
(3, 1, 'East Coast Turf Field (7-a-side)', 'All-weather 7-a-side football ground with modern lighting and perimeter netting.', 30.00, 40.00, '18:00:00', 60, '07:00:00', '23:00:00', 0, 'ACTIVE', NOW()),

-- Futsal fields
(4, 2, 'Futsal Pitch Pro 1', 'Covered court with cushioned artificial turf, electronic scoreboard, and ventilation.', 25.00, 32.00, '17:00:00', 60, '07:00:00', '23:00:00', 0, 'ACTIVE', NOW()),
(5, 2, 'Futsal Pitch Pro 2', 'Covered rubberized surface designed for fast-paced 5-a-side futsal play.', 25.00, 32.00, '17:00:00', 60, '07:00:00', '23:00:00', 0, 'ACTIVE', NOW()),

-- Badminton courts
(6, 3, 'Badminton Court 1 (VIP AC)', 'Fully air-conditioned court with Taraflex sports flooring and glare-free LED lighting.', 12.00, 16.00, '17:00:00', 60, '06:00:00', '22:00:00', 0, 'ACTIVE', NOW()),
(7, 3, 'Badminton Court 2', 'Tournament-grade synthetic mat court with comfortable viewer seating area.', 10.00, 14.00, '17:00:00', 60, '06:00:00', '22:00:00', 0, 'ACTIVE', NOW()),
(8, 3, 'Badminton Court 3', 'Indoor court ideal for casual play and private coaching sessions.', 10.00, 14.00, '17:00:00', 60, '06:00:00', '22:00:00', 0, 'ACTIVE', NOW()),

-- Tennis courts
(9, 4, 'Center Tennis Court (Hardcourt)', 'US Open style acrylic hard court with high-intensity nighttime LED floodlights.', 20.00, 28.00, '17:00:00', 60, '06:00:00', '22:00:00', 20, 'ACTIVE', NOW()),
(10, 4, 'Tennis Court B (Clay)', 'Well-maintained red clay surface providing excellent shock absorption.', 22.00, 30.00, '17:00:00', 60, '06:00:00', '21:00:00', 20, 'ACTIVE', NOW()),

-- Basketball courts
(11, 5, 'Indoor Hardwood Basketball Court', 'Maple hardwood flooring with breakaway rims, shot clocks, and electronic scoreboard.', 40.00, 55.00, '17:00:00', 60, '07:00:00', '22:00:00', 20, 'ACTIVE', NOW()),
(12, 5, 'Outdoor Streetball Court', 'Shock-absorbing outdoor acrylic court with weather-proof fiberglass backboards.', 18.00, 24.00, '17:00:00', 60, '06:00:00', '22:00:00', 0, 'ACTIVE', NOW()),

-- Volleyball courts
(13, 6, 'Volleyball Indoor Arena', 'High-ceiling indoor court with non-slip flooring and adjustable net heights.', 20.00, 26.00, '17:00:00', 60, '07:00:00', '22:00:00', 0, 'ACTIVE', NOW()),
(14, 6, 'Beach Sand Volleyball Court', 'Fine deep-sand beach volleyball court with rinse showers and tropical ambient lighting.', 16.00, 22.00, '16:00:00', 60, '07:00:00', '22:00:00', 0, 'ACTIVE', NOW());

-- Field Images Seed Data
INSERT INTO field_images (field_id, image_path, is_main, sort_order) VALUES
(1, '/img/fields/football-1.jpg', TRUE, 0),
(2, '/img/fields/football-2.jpg', TRUE, 0),
(3, '/img/fields/football-3.jpg', TRUE, 0),
(4, '/img/fields/futsal-1.jpg', TRUE, 0),
(5, '/img/fields/futsal-2.jpg', TRUE, 0),
(6, '/img/fields/badminton-1.jpg', TRUE, 0),
(7, '/img/fields/badminton-2.jpg', TRUE, 0),
(8, '/img/fields/badminton-3.jpg', TRUE, 0),
(9, '/img/fields/tennis-1.jpg', TRUE, 0),
(10, '/img/fields/tennis-2.jpg', TRUE, 0),
(11, '/img/fields/basketball-1.jpg', TRUE, 0),
(12, '/img/fields/basketball-2.jpg', TRUE, 0),
(13, '/img/fields/volleyball-1.jpg', TRUE, 0),
(14, '/img/fields/volleyball-2.jpg', TRUE, 0);

-- Settings Seed Data
INSERT INTO settings (setting_key, setting_value) VALUES
('maxAdvanceDays', '14'),
('minLeadHours', '2'),
('holdMinutes', '15'),
('cancelCutoffHours', '6'),
('maxActiveBookingsPerUser', '3'),
('shopName', 'SportZone Arena'),
('khqrMerchantBakongId', 'sportzone@devb'),
('khqrMerchantName', 'SportZone Arena Phnom Penh'),
('khqrMerchantCity', 'Phnom Penh'),
('currency', 'USD');
