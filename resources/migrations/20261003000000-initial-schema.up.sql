CREATE TABLE customers (
  id         TEXT PRIMARY KEY,
  name       TEXT NOT NULL,
  email      TEXT,
  kind       TEXT NOT NULL,
  tax_id     TEXT NOT NULL,
  status     TEXT NOT NULL DEFAULT 'active',
  created_at TEXT NOT NULL,
  updated_at TEXT NOT NULL,
  CONSTRAINT uq_customers_email  UNIQUE (email),
  CONSTRAINT uq_customers_tax_id UNIQUE (tax_id)
);

--;;
CREATE TABLE customer_phones (
  customer_id TEXT    NOT NULL REFERENCES customers(id) ON DELETE CASCADE,
  position    INTEGER NOT NULL,
  phone       TEXT    NOT NULL,
  PRIMARY KEY (customer_id, position)
);

--;;
CREATE TABLE stock_items (
  id            TEXT PRIMARY KEY,
  name          TEXT NOT NULL,
  quantity      INTEGER NOT NULL,
  cost          INTEGER NOT NULL,
  default_price INTEGER NOT NULL,
  available     INTEGER NOT NULL,
  brand         TEXT,
  acquired_at   TEXT,
  serial        TEXT,
  notes         TEXT,
  status        TEXT NOT NULL DEFAULT 'active',
  created_at    TEXT NOT NULL,
  updated_at    TEXT NOT NULL
);

--;;
CREATE TABLE financial_movements (
  id           TEXT PRIMARY KEY,
  type         TEXT NOT NULL,
  direction    TEXT NOT NULL,
  amount       INTEGER NOT NULL,
  date         TEXT NOT NULL,
  counterparty TEXT,
  description  TEXT,
  shared       INTEGER NOT NULL,
  status       TEXT NOT NULL DEFAULT 'active',
  created_at   TEXT NOT NULL,
  updated_at   TEXT NOT NULL
);

--;;
CREATE TABLE financial_movement_items (
  movement_id   TEXT    NOT NULL REFERENCES financial_movements(id) ON DELETE CASCADE,
  position      INTEGER NOT NULL,
  stock_item_id TEXT    NOT NULL REFERENCES stock_items(id),
  quantity      INTEGER NOT NULL,
  unit_price    INTEGER NOT NULL,
  PRIMARY KEY (movement_id, position)
);

--;;
CREATE TABLE installments (
  movement_id TEXT    NOT NULL REFERENCES financial_movements(id) ON DELETE CASCADE,
  number      INTEGER NOT NULL,
  due_date    TEXT    NOT NULL,
  amount      INTEGER NOT NULL,
  status      TEXT    NOT NULL,
  responsible TEXT,
  paid_at     TEXT,
  PRIMARY KEY (movement_id, number)
);
