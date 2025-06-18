CREATE TABLE supplier
(
    id      SERIAL PRIMARY KEY,
    name    VARCHAR(255) NOT NULL,
    address TEXT         NOT NULL,
    phone   VARCHAR(50)
);

CREATE TABLE part
(
    id             SERIAL PRIMARY KEY,
    article_number VARCHAR(100) NOT NULL UNIQUE,
    name           VARCHAR(255) NOT NULL
);

CREATE TABLE part_price
(
    id          SERIAL PRIMARY KEY,
    supplier_id INTEGER        NOT NULL REFERENCES supplier (id),
    part_id     INTEGER        NOT NULL REFERENCES part (id),
    price       NUMERIC(12, 2) NOT NULL,
    start_date  DATE           NOT NULL
);

CREATE TABLE purchase
(
    id            SERIAL PRIMARY KEY,
    supplier_id   INTEGER NOT NULL REFERENCES supplier (id),
    part_id       INTEGER NOT NULL REFERENCES part (id),
    purchase_date DATE    NOT NULL,
    quantity      INTEGER NOT NULL
);
