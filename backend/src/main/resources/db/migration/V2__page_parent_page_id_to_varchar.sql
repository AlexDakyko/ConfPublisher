-- Меняем тип parent_page_id с BIGINT на VARCHAR(100)
-- USING необходим, чтобы Postgres знал, как конвертировать существующие значения
ALTER TABLE page
    ALTER COLUMN parent_page_id TYPE VARCHAR(100) USING parent_page_id::VARCHAR;