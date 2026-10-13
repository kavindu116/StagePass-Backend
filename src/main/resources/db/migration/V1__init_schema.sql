-- =====================================================================
-- Stagepass — V1 initial schema (PostgreSQL 16+)
--
-- Design rules
--   * Seat HOLDS live in Redis (SET NX EX 600), not in this database.
--     The DB only records the final truth: AVAILABLE / SOLD / BLOCKED.
--   * Double-selling is blocked twice:
--       1) UPDATE show_seats ... WHERE status = 'AVAILABLE' (+ version)
--       2) partial UNIQUE index on tickets(show_seat_id) for live tickets
--   * Money is NUMERIC(12,2) in LKR, never float.
--   * Every table that changes has created_at / updated_at.
-- =====================================================================

-- ---------------------------------------------------------------------
-- Helper: keep updated_at fresh
-- ---------------------------------------------------------------------
CREATE OR REPLACE FUNCTION set_updated_at() RETURNS trigger AS $$
BEGIN
    NEW.updated_at := now();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- ---------------------------------------------------------------------
-- USERS
-- ---------------------------------------------------------------------
CREATE TABLE users (
    id                BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    email             VARCHAR(255) NOT NULL,
    password_hash     VARCHAR(100),                      -- NULL for Google-only accounts
    full_name         VARCHAR(120) NOT NULL,
    phone             VARCHAR(20),
    city              VARCHAR(60),
    role              VARCHAR(20)  NOT NULL DEFAULT 'CUSTOMER',
    auth_provider     VARCHAR(20)  NOT NULL DEFAULT 'LOCAL',
    provider_id       VARCHAR(255),                      -- Google "sub"
    email_verified    BOOLEAN      NOT NULL DEFAULT FALSE,
    notify_booking    BOOLEAN      NOT NULL DEFAULT TRUE,
    notify_on_sale    BOOLEAN      NOT NULL DEFAULT TRUE,
    notify_reminder   BOOLEAN      NOT NULL DEFAULT FALSE,
    enabled           BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at        TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at        TIMESTAMPTZ  NOT NULL DEFAULT now(),

    CONSTRAINT chk_users_role     CHECK (role IN ('CUSTOMER', 'ORGANIZER', 'ADMIN')),
    CONSTRAINT chk_users_provider CHECK (auth_provider IN ('LOCAL', 'GOOGLE')),
    CONSTRAINT chk_users_local_pw CHECK (auth_provider <> 'LOCAL' OR password_hash IS NOT NULL)
);
CREATE UNIQUE INDEX ux_users_email          ON users (lower(email));
CREATE UNIQUE INDEX ux_users_provider       ON users (auth_provider, provider_id) WHERE provider_id IS NOT NULL;
CREATE TRIGGER trg_users_updated BEFORE UPDATE ON users FOR EACH ROW EXECUTE FUNCTION set_updated_at();

-- ---------------------------------------------------------------------
-- VENUES + physical seat layout (built once per venue in the admin
-- "Seat Layout Builder", reused by every event at that venue)
-- ---------------------------------------------------------------------
CREATE TABLE venues (
    id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name        VARCHAR(150) NOT NULL,
    address     VARCHAR(255) NOT NULL,
    city        VARCHAR(60)  NOT NULL,
    latitude    NUMERIC(9,6),
    longitude   NUMERIC(9,6),
    capacity    INTEGER,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at  TIMESTAMPTZ  NOT NULL DEFAULT now()
);
CREATE INDEX ix_venues_city ON venues (city);
CREATE TRIGGER trg_venues_updated BEFORE UPDATE ON venues FOR EACH ROW EXECUTE FUNCTION set_updated_at();

CREATE TABLE venue_seats (
    id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    venue_id    BIGINT      NOT NULL REFERENCES venues (id) ON DELETE CASCADE,
    section     VARCHAR(40) NOT NULL DEFAULT 'MAIN',
    row_label   VARCHAR(5)  NOT NULL,
    seat_number SMALLINT    NOT NULL,
    pos_x       SMALLINT    NOT NULL,           -- grid position for drawing the map
    pos_y       SMALLINT    NOT NULL,
    accessible  BOOLEAN     NOT NULL DEFAULT FALSE,

    CONSTRAINT ux_venue_seat UNIQUE (venue_id, section, row_label, seat_number),
    CONSTRAINT chk_seat_number CHECK (seat_number > 0)
);
CREATE INDEX ix_venue_seats_venue ON venue_seats (venue_id);

-- ---------------------------------------------------------------------
-- EVENTS, SHOWS (one event can run several nights) and price TIERS
-- ---------------------------------------------------------------------
CREATE TABLE events (
    id                     BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    organizer_id           BIGINT       NOT NULL REFERENCES users (id),
    venue_id               BIGINT       NOT NULL REFERENCES venues (id),
    slug                   VARCHAR(120) NOT NULL UNIQUE,      -- used in URLs: /events/{slug}
    title                  VARCHAR(200) NOT NULL,
    short_label            VARCHAR(20),                        -- big word on the card art
    category               VARCHAR(20)  NOT NULL,
    description            TEXT,
    poster_url             VARCHAR(500),
    tint_color             CHAR(7),                             -- '#4B2E83'
    age_restriction        VARCHAR(20)  NOT NULL DEFAULT 'ALL_AGES',
    status                 VARCHAR(20)  NOT NULL DEFAULT 'DRAFT',
    sale_mode              VARCHAR(20)  NOT NULL DEFAULT 'DIRECT',  -- QUEUE = waiting room
    max_tickets_per_order  SMALLINT     NOT NULL DEFAULT 6,
    sales_start_at         TIMESTAMPTZ,
    sales_end_at           TIMESTAMPTZ,
    published_at           TIMESTAMPTZ,
    created_at             TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at             TIMESTAMPTZ  NOT NULL DEFAULT now(),

    CONSTRAINT chk_events_category  CHECK (category IN ('CONCERT', 'FESTIVAL', 'MOVIE', 'SPORTS', 'THEATRE')),
    CONSTRAINT chk_events_status    CHECK (status IN ('DRAFT', 'PUBLISHED', 'CANCELLED', 'COMPLETED')),
    CONSTRAINT chk_events_sale_mode CHECK (sale_mode IN ('DIRECT', 'QUEUE')),
    CONSTRAINT chk_events_max_tix   CHECK (max_tickets_per_order BETWEEN 1 AND 20),
    CONSTRAINT chk_events_sales_win CHECK (sales_end_at IS NULL OR sales_start_at IS NULL OR sales_end_at > sales_start_at)
);
CREATE INDEX ix_events_listing   ON events (status, category);
CREATE INDEX ix_events_organizer ON events (organizer_id);
CREATE INDEX ix_events_venue     ON events (venue_id);
CREATE TRIGGER trg_events_updated BEFORE UPDATE ON events FOR EACH ROW EXECUTE FUNCTION set_updated_at();

CREATE TABLE shows (
    id             BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    event_id       BIGINT      NOT NULL REFERENCES events (id) ON DELETE CASCADE,
    label          VARCHAR(60),                          -- 'Night 1'
    starts_at      TIMESTAMPTZ NOT NULL,
    doors_open_at  TIMESTAMPTZ,
    ends_at        TIMESTAMPTZ,
    status         VARCHAR(20) NOT NULL DEFAULT 'SCHEDULED',
    created_at     TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at     TIMESTAMPTZ NOT NULL DEFAULT now(),

    CONSTRAINT chk_shows_status CHECK (status IN ('SCHEDULED', 'SOLD_OUT', 'CANCELLED', 'COMPLETED')),
    CONSTRAINT chk_shows_doors  CHECK (doors_open_at IS NULL OR doors_open_at <= starts_at),
    CONSTRAINT chk_shows_end    CHECK (ends_at IS NULL OR ends_at > starts_at)
);
CREATE INDEX ix_shows_event_time ON shows (event_id, starts_at);
CREATE INDEX ix_shows_starts_at  ON shows (starts_at);
CREATE TRIGGER trg_shows_updated BEFORE UPDATE ON shows FOR EACH ROW EXECUTE FUNCTION set_updated_at();

CREATE TABLE tiers (
    id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    event_id    BIGINT        NOT NULL REFERENCES events (id) ON DELETE CASCADE,
    name        VARCHAR(40)   NOT NULL,          -- VIP, Gold, Silver, General
    price       NUMERIC(12,2) NOT NULL,
    currency    CHAR(3)       NOT NULL DEFAULT 'LKR',
    color       CHAR(7)       NOT NULL,          -- seat outline colour on the map
    sort_order  SMALLINT      NOT NULL DEFAULT 0,

    CONSTRAINT ux_tiers_event_name UNIQUE (event_id, name),
    CONSTRAINT chk_tiers_price     CHECK (price >= 0)
);

-- ---------------------------------------------------------------------
-- SHOW_SEATS — the sellable inventory: one row per seat per show.
-- Created when an event is published (venue_seats x shows).
-- ---------------------------------------------------------------------
CREATE TABLE show_seats (
    id             BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    show_id        BIGINT      NOT NULL REFERENCES shows (id) ON DELETE CASCADE,
    venue_seat_id  BIGINT      NOT NULL REFERENCES venue_seats (id),
    tier_id        BIGINT      NOT NULL REFERENCES tiers (id),
    status         VARCHAR(20) NOT NULL DEFAULT 'AVAILABLE',
    version        INTEGER     NOT NULL DEFAULT 0,      -- JPA @Version (optimistic lock)
    sold_at        TIMESTAMPTZ,
    updated_at     TIMESTAMPTZ NOT NULL DEFAULT now(),

    CONSTRAINT ux_show_seat         UNIQUE (show_id, venue_seat_id),
    CONSTRAINT chk_show_seat_status CHECK (status IN ('AVAILABLE', 'SOLD', 'BLOCKED')),
    CONSTRAINT chk_show_seat_sold   CHECK ((status = 'SOLD') = (sold_at IS NOT NULL))
);
CREATE INDEX ix_show_seats_show_status ON show_seats (show_id, status);
CREATE INDEX ix_show_seats_tier        ON show_seats (tier_id);
CREATE TRIGGER trg_show_seats_updated BEFORE UPDATE ON show_seats FOR EACH ROW EXECUTE FUNCTION set_updated_at();

-- ---------------------------------------------------------------------
-- PROMO CODES
-- ---------------------------------------------------------------------
CREATE TABLE promo_codes (
    id             BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    event_id       BIGINT        REFERENCES events (id) ON DELETE CASCADE,   -- NULL = site-wide
    code           VARCHAR(30)   NOT NULL,
    discount_type  VARCHAR(10)   NOT NULL,
    discount_value NUMERIC(12,2) NOT NULL,
    max_uses       INTEGER,
    used_count     INTEGER       NOT NULL DEFAULT 0,
    valid_from     TIMESTAMPTZ,
    valid_to       TIMESTAMPTZ,
    active         BOOLEAN       NOT NULL DEFAULT TRUE,
    created_at     TIMESTAMPTZ   NOT NULL DEFAULT now(),

    CONSTRAINT chk_promo_type    CHECK (discount_type IN ('PERCENT', 'FIXED')),
    CONSTRAINT chk_promo_value   CHECK (discount_value > 0 AND (discount_type <> 'PERCENT' OR discount_value <= 100)),
    CONSTRAINT chk_promo_uses    CHECK (max_uses IS NULL OR used_count <= max_uses),
    CONSTRAINT chk_promo_window  CHECK (valid_to IS NULL OR valid_from IS NULL OR valid_to > valid_from)
);
CREATE UNIQUE INDEX ux_promo_code ON promo_codes (upper(code));

-- ---------------------------------------------------------------------
-- ORDERS — created PENDING when the user leaves checkout (seats already
-- held in Redis). Becomes CONFIRMED by the payment webhook, or EXPIRED by
-- the cleanup job when hold_expires_at passes.
-- ---------------------------------------------------------------------
CREATE TABLE orders (
    id               BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    public_ref       VARCHAR(20)   NOT NULL UNIQUE,          -- 'SP-7K3Q9X' shown to users
    user_id          BIGINT        NOT NULL REFERENCES users (id),
    show_id          BIGINT        NOT NULL REFERENCES shows (id),
    promo_code_id    BIGINT        REFERENCES promo_codes (id),
    status           VARCHAR(20)   NOT NULL DEFAULT 'PENDING',
    subtotal         NUMERIC(12,2) NOT NULL,
    service_fee      NUMERIC(12,2) NOT NULL DEFAULT 0,
    discount         NUMERIC(12,2) NOT NULL DEFAULT 0,
    total            NUMERIC(12,2) NOT NULL,
    currency         CHAR(3)       NOT NULL DEFAULT 'LKR',
    contact_name     VARCHAR(120)  NOT NULL,
    contact_email    VARCHAR(255)  NOT NULL,
    contact_phone    VARCHAR(20),
    idempotency_key  VARCHAR(64)   UNIQUE,                   -- client retry protection
    hold_expires_at  TIMESTAMPTZ   NOT NULL,                 -- mirrors the Redis TTL
    confirmed_at     TIMESTAMPTZ,
    created_at       TIMESTAMPTZ   NOT NULL DEFAULT now(),
    updated_at       TIMESTAMPTZ   NOT NULL DEFAULT now(),

    CONSTRAINT chk_orders_status CHECK (status IN ('PENDING', 'CONFIRMED', 'EXPIRED', 'FAILED', 'CANCELLED', 'REFUNDED')),
    CONSTRAINT chk_orders_money  CHECK (subtotal >= 0 AND service_fee >= 0 AND discount >= 0
                                        AND total = subtotal + service_fee - discount AND total >= 0),
    CONSTRAINT chk_orders_confirmed CHECK (status NOT IN ('CONFIRMED', 'REFUNDED') OR confirmed_at IS NOT NULL)
);
CREATE INDEX ix_orders_user_created ON orders (user_id, created_at DESC);
CREATE INDEX ix_orders_show         ON orders (show_id);
-- The expiry job scans only PENDING rows:
CREATE INDEX ix_orders_pending_expiry ON orders (hold_expires_at) WHERE status = 'PENDING';
CREATE TRIGGER trg_orders_updated BEFORE UPDATE ON orders FOR EACH ROW EXECUTE FUNCTION set_updated_at();

CREATE TABLE order_items (
    id             BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    order_id       BIGINT        NOT NULL REFERENCES orders (id) ON DELETE CASCADE,
    show_seat_id   BIGINT        NOT NULL REFERENCES show_seats (id),
    tier_id        BIGINT        NOT NULL REFERENCES tiers (id),
    price          NUMERIC(12,2) NOT NULL,          -- price snapshot at order time
    attendee_name  VARCHAR(120),

    CONSTRAINT ux_order_item_seat UNIQUE (order_id, show_seat_id),
    CONSTRAINT chk_order_item_price CHECK (price >= 0)
);
CREATE INDEX ix_order_items_seat ON order_items (show_seat_id);

-- ---------------------------------------------------------------------
-- PAYMENTS + webhook inbox (idempotency: the same gateway event is
-- processed exactly once thanks to UNIQUE (provider, provider_event_id))
-- ---------------------------------------------------------------------
CREATE TABLE payments (
    id                   BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    order_id             BIGINT        NOT NULL REFERENCES orders (id),
    provider             VARCHAR(20)   NOT NULL,
    provider_payment_id  VARCHAR(100),
    method               VARCHAR(20),
    amount               NUMERIC(12,2) NOT NULL,
    currency             CHAR(3)       NOT NULL DEFAULT 'LKR',
    status               VARCHAR(20)   NOT NULL DEFAULT 'INITIATED',
    card_last4           CHAR(4),                     -- display only, never the full number
    failure_reason       VARCHAR(255),
    created_at           TIMESTAMPTZ   NOT NULL DEFAULT now(),
    updated_at           TIMESTAMPTZ   NOT NULL DEFAULT now(),

    CONSTRAINT chk_payments_provider CHECK (provider IN ('PAYHERE', 'STRIPE')),
    CONSTRAINT chk_payments_method   CHECK (method IS NULL OR method IN ('CARD', 'WALLET', 'BANK')),
    CONSTRAINT chk_payments_status   CHECK (status IN ('INITIATED', 'SUCCEEDED', 'FAILED', 'REFUNDED')),
    CONSTRAINT chk_payments_amount   CHECK (amount >= 0)
);
CREATE INDEX ix_payments_order ON payments (order_id);
CREATE UNIQUE INDEX ux_payments_provider_ref ON payments (provider, provider_payment_id) WHERE provider_payment_id IS NOT NULL;
-- At most one successful payment per order:
CREATE UNIQUE INDEX ux_payments_one_success ON payments (order_id) WHERE status = 'SUCCEEDED';
CREATE TRIGGER trg_payments_updated BEFORE UPDATE ON payments FOR EACH ROW EXECUTE FUNCTION set_updated_at();

CREATE TABLE payment_webhook_events (
    id                 BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    provider           VARCHAR(20)  NOT NULL,
    provider_event_id  VARCHAR(100) NOT NULL,
    payload            JSONB        NOT NULL,
    received_at        TIMESTAMPTZ  NOT NULL DEFAULT now(),
    processed_at       TIMESTAMPTZ,

    CONSTRAINT ux_webhook_event UNIQUE (provider, provider_event_id)
);

-- ---------------------------------------------------------------------
-- TICKETS — one per seat in a confirmed order. QR = signed JWT whose
-- "jti" is ticket.code; the gate scanner flips VALID -> USED atomically:
--   UPDATE tickets SET status='USED', checked_in_at=now()
--   WHERE code = :code AND status = 'VALID'
-- ---------------------------------------------------------------------
CREATE TABLE tickets (
    id              BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    code            UUID        NOT NULL UNIQUE DEFAULT gen_random_uuid(),
    order_id        BIGINT      NOT NULL REFERENCES orders (id),
    order_item_id   BIGINT      NOT NULL UNIQUE REFERENCES order_items (id),
    show_seat_id    BIGINT      NOT NULL REFERENCES show_seats (id),
    status          VARCHAR(20) NOT NULL DEFAULT 'VALID',
    checked_in_at   TIMESTAMPTZ,
    checked_in_by   BIGINT      REFERENCES users (id),
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),

    CONSTRAINT chk_tickets_status  CHECK (status IN ('VALID', 'USED', 'CANCELLED')),
    CONSTRAINT chk_tickets_checkin CHECK ((status = 'USED') = (checked_in_at IS NOT NULL))
);
-- Last line of defence against double-selling: a seat can have only one live ticket.
CREATE UNIQUE INDEX ux_tickets_live_seat ON tickets (show_seat_id) WHERE status IN ('VALID', 'USED');
CREATE INDEX ix_tickets_order ON tickets (order_id);

-- ---------------------------------------------------------------------
-- REFUND REQUESTS (user asks -> admin approves -> payment refunded)
-- ---------------------------------------------------------------------
CREATE TABLE refund_requests (
    id           BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    order_id     BIGINT       NOT NULL REFERENCES orders (id),
    requested_by BIGINT       NOT NULL REFERENCES users (id),
    reason       VARCHAR(500),
    status       VARCHAR(20)  NOT NULL DEFAULT 'REQUESTED',
    reviewed_by  BIGINT       REFERENCES users (id),
    reviewed_at  TIMESTAMPTZ,
    created_at   TIMESTAMPTZ  NOT NULL DEFAULT now(),

    CONSTRAINT chk_refund_status CHECK (status IN ('REQUESTED', 'APPROVED', 'REJECTED', 'REFUNDED'))
);
-- Only one open request per order:
CREATE UNIQUE INDEX ux_refund_open ON refund_requests (order_id) WHERE status IN ('REQUESTED', 'APPROVED');

-- ---------------------------------------------------------------------
-- OUTBOX — written in the SAME transaction as the business change, then
-- a relay publishes to Kafka (booking.confirmed, booking.cancelled ...).
-- Guarantees no "order confirmed but event never sent" bugs.
-- ---------------------------------------------------------------------
CREATE TABLE outbox_events (
    id              BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    aggregate_type  VARCHAR(40)  NOT NULL,      -- 'ORDER'
    aggregate_id    VARCHAR(40)  NOT NULL,      -- order id
    event_type      VARCHAR(60)  NOT NULL,      -- 'booking.confirmed'
    payload         JSONB        NOT NULL,
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT now(),
    published_at    TIMESTAMPTZ
);
CREATE INDEX ix_outbox_unpublished ON outbox_events (created_at) WHERE published_at IS NULL;
