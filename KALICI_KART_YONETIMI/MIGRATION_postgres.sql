-- Örnek PostgreSQL şeması: saved_payment_methods tablosu
CREATE TABLE IF NOT EXISTS saved_payment_methods (
  spm_id BIGSERIAL PRIMARY KEY,
  user_id BIGINT NOT NULL,
  brand VARCHAR(20),
  last4 VARCHAR(4),
  label VARCHAR(80),
  sealed_card TEXT NOT NULL,
  active BOOLEAN NOT NULL DEFAULT TRUE,
  created_at TIMESTAMPTZ NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_spm_user ON saved_payment_methods(user_id);
CREATE INDEX IF NOT EXISTS idx_spm_user_active ON saved_payment_methods(user_id, active);


