ALTER TABLE species ADD COLUMN IF NOT EXISTS sizes JSONB;

UPDATE species s SET sizes = ur.result from (
SELECT
    url,
    jsonb_agg(json_object) AS result
FROM (
         SELECT
             url,
             jsonb_build_object(
                     'type',
                     CASE split_part(part, ' ', 1)
                         WHEN 'Неопределенный' THEN 'UNDEFINED'
                         WHEN 'Крошечный' THEN 'TINY'
                         WHEN 'Маленький' THEN 'SMALL'
                         WHEN 'Средний' THEN 'MEDIUM'
                         WHEN 'Большой' THEN 'LARGE'
                         WHEN 'Огромный' THEN 'HUGE'
                         WHEN 'Громадный' THEN 'GARGANTUAN'
                         ELSE split_part(part, ' ', 1) -- если слово не найдено в перечислении
                         END,
                     'from', (regexp_matches(part, '(\d+)'))[1]::int,
                     'to', (regexp_matches(part, '\d+-(\d+)'))[1]::int
             ) AS json_object
         FROM
             species,
             regexp_split_to_table(text, '\s*или\s*') AS part
     ) AS subquery
GROUP BY url) as ur where ur.url = s.url and text is not null;