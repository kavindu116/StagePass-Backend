-- =====================================================================
-- DEV-ONLY seed data. Loaded only in the "dev" profile:
--   spring.flyway.locations=classpath:db/migration,classpath:db/seed
-- Event slugs match the Next.js mock ids, so the frontend URLs keep working.
-- =====================================================================

-- Organizer + demo customer (Google accounts -> no password needed for seed)
INSERT INTO users (email, full_name, role, auth_provider, provider_id, email_verified)
VALUES ('organizer@stagepass.dev', 'Demo Organizer', 'ORGANIZER', 'GOOGLE', 'seed-organizer', TRUE),
       ('customer@stagepass.dev',  'Kavindu Lakshan', 'CUSTOMER', 'GOOGLE', 'seed-customer',  TRUE);

INSERT INTO venues (name, address, city, capacity)
VALUES ('[Venue]',        '[Street address], Colombo 07', 'Colombo', 160),
       ('[Beach venue]',  '[Street address], Negombo',    'Negombo', 160);

-- 10 rows (A–J) x 16 seats, aisles after seats 4 and 12 (pos_x leaves a gap)
INSERT INTO venue_seats (venue_id, row_label, seat_number, pos_x, pos_y)
SELECT v.id,
       chr(64 + r)                                  AS row_label,
       s                                            AS seat_number,
       s + CASE WHEN s >= 13 THEN 2 WHEN s >= 5 THEN 1 ELSE 0 END AS pos_x,
       r                                            AS pos_y
FROM venues v
CROSS JOIN generate_series(1, 10) AS r
CROSS JOIN generate_series(1, 16) AS s;

-- Events
INSERT INTO events (organizer_id, venue_id, slug, title, short_label, category, description,
                    tint_color, status, sale_mode, sales_start_at, published_at)
SELECT (SELECT id FROM users WHERE email = 'organizer@stagepass.dev'),
       (SELECT id FROM venues WHERE name = e.venue),
       e.slug, e.title, e.short_label, e.category,
       '[Event description — artist bio, what to expect, set length, supporting acts.]',
       e.tint, e.status, e.sale_mode, now() - interval '7 days', now() - interval '7 days'
FROM (VALUES
    ('artist-live-colombo', '[Artist] Live in Concert', 'LIVE',     'CONCERT',  '#4B2E83', 'PUBLISHED', 'DIRECT', '[Venue]'),
    ('jazz-night',          '[Jazz Night] Sessions',    'JAZZ',     'CONCERT',  '#1F5C57', 'PUBLISHED', 'DIRECT', '[Venue]'),
    ('t20-final',           '[Team A] vs [Team B]',     'T20',      'SPORTS',   '#7A2E2E', 'PUBLISHED', 'DIRECT', '[Venue]'),
    ('standup-tour',        '[Comedian] Tour',          'STAND-UP', 'THEATRE',  '#7A5A12', 'PUBLISHED', 'DIRECT', '[Venue]'),
    ('beach-party',         '[DJ] Beach Party',         'EDM',      'CONCERT',  '#22408A', 'PUBLISHED', 'DIRECT', '[Beach venue]'),
    ('play-title',          '[Play title]',             'DRAMA',    'THEATRE',  '#5A2A5E', 'PUBLISHED', 'DIRECT', '[Venue]'),
    ('music-festival',      '[Music Festival] 2026',    'FEST',     'FESTIVAL', '#2E5A2A', 'PUBLISHED', 'QUEUE',  '[Venue]'),
    ('movie-premiere',      '[Movie] Premiere',         'IMAX',     'MOVIE',    '#33363F', 'PUBLISHED', 'DIRECT', '[Venue]')
) AS e(slug, title, short_label, category, tint, status, sale_mode, venue);

-- Shows: two nights each, Asia/Colombo times
INSERT INTO shows (event_id, label, starts_at, doors_open_at)
SELECT ev.id, n.label,
       (d.day + n.offset_days + time '19:00') AT TIME ZONE 'Asia/Colombo',
       (d.day + n.offset_days + time '18:00') AT TIME ZONE 'Asia/Colombo'
FROM events ev
JOIN (VALUES
    ('artist-live-colombo', date '2026-12-14'),
    ('jazz-night',          date '2026-10-25'),
    ('t20-final',           date '2026-11-03'),
    ('standup-tour',        date '2026-10-19'),
    ('beach-party',         date '2026-11-30'),
    ('play-title',          date '2026-11-07'),
    ('music-festival',      date '2026-12-21'),
    ('movie-premiere',      date '2026-10-23')
) AS d(slug, day) ON d.slug = ev.slug
CROSS JOIN (VALUES ('Night 1', 0), ('Night 2', 1)) AS n(label, offset_days);

-- Tiers (same four for every event)
INSERT INTO tiers (event_id, name, price, color, sort_order)
SELECT ev.id, t.name, t.price, t.color, t.sort_order
FROM events ev
CROSS JOIN (VALUES
    ('VIP',     15000.00, '#FFB02E', 1),
    ('Gold',     8000.00, '#B4A9FF', 2),
    ('Silver',   5000.00, '#6FD6A2', 3),
    ('General',  3500.00, '#7CB8FF', 4)
) AS t(name, price, color, sort_order);

-- Inventory: every venue seat x every show, tier by row (A-B VIP, C-E Gold, F-H Silver, I-J General)
INSERT INTO show_seats (show_id, venue_seat_id, tier_id)
SELECT sh.id, vs.id, t.id
FROM shows sh
JOIN events ev       ON ev.id = sh.event_id
JOIN venue_seats vs  ON vs.venue_id = ev.venue_id
JOIN tiers t         ON t.event_id = ev.id
                    AND t.name = CASE
                                   WHEN vs.row_label IN ('A', 'B')      THEN 'VIP'
                                   WHEN vs.row_label IN ('C', 'D', 'E') THEN 'Gold'
                                   WHEN vs.row_label IN ('F', 'G', 'H') THEN 'Silver'
                                   ELSE 'General'
                                 END;

-- Movie premiere is sold out: mark every seat SOLD
UPDATE show_seats ss
SET status = 'SOLD', sold_at = now()
FROM shows sh JOIN events ev ON ev.id = sh.event_id
WHERE ss.show_id = sh.id AND ev.slug = 'movie-premiere';

UPDATE shows sh SET status = 'SOLD_OUT'
FROM events ev WHERE ev.id = sh.event_id AND ev.slug = 'movie-premiere';

INSERT INTO promo_codes (code, discount_type, discount_value, max_uses)
VALUES ('WELCOME10', 'PERCENT', 10, 500);
