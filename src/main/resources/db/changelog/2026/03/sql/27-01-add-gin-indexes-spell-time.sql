CREATE INDEX IF NOT EXISTS idx_spell_casting_time_gin ON spell USING gin (casting_time);
CREATE INDEX IF NOT EXISTS idx_spell_duration_gin ON spell USING gin (duration);
