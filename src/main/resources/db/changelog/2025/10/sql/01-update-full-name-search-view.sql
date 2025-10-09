CREATE OR REPLACE VIEW full_name_search_view AS
SELECT entity.url,
       entity.name,
       entity.english,
       entity.alternative,
       entity.page,
       entity.type,
       entity.is_hidden_entity,
       books.source_acronym AS book_acronym,
       books.name           AS book_name,
       books.english_name   AS book_english_name,
       books.type           AS book_type
FROM (
         SELECT species.url,
                species.name,
                species.english,
                species.alternative,
                species.source_page AS page,
                'SPECIES'::text     AS type,
                 species.source,
                species.is_hidden_entity
         FROM species
         UNION
         SELECT spell.url,
                spell.name,
                spell.english,
                spell.alternative,
                spell.source_page AS page,
                'SPELL'::text     AS type,
                 spell.source,
                spell.is_hidden_entity
         FROM spell
         UNION
         SELECT feat.url,
                feat.name,
                feat.english,
                feat.alternative,
                feat.source_page AS page,
                'FEAT'::text     AS type,
                 feat.source,
                feat.is_hidden_entity
         FROM feat
         UNION
         SELECT background.url,
                background.name,
                background.english,
                background.alternative,
                background.source_page AS page,
                'BACKGROUND'::text     AS type,
                 background.source,
                background.is_hidden_entity
         FROM background
         UNION
         SELECT bestiary.url,
                bestiary.name,
                bestiary.english,
                bestiary.alternative,
                bestiary.source_page AS page,
                'BESTIARY'::text     AS type,
                 bestiary.source,
                bestiary.is_hidden_entity
         FROM bestiary
         UNION
         SELECT magic_item.url,
                magic_item.name,
                magic_item.english,
                magic_item.alternative,
                magic_item.source_page AS page,
                'MAGIC_ITEM'::text     AS type,
                 magic_item.source,
                magic_item.is_hidden_entity
         FROM magic_item
         UNION
         SELECT item.url,
                item.name,
                item.english,
                item.alternative,
                item.source_page AS page,
                'ITEM'::text AS type,
                 item.source,
                item.is_hidden_entity
         FROM item
         UNION
         SELECT glossary.url,
                glossary.name,
                glossary.english,
                glossary.alternative,
                glossary.source_page AS page,
                'GLOSSARY'::text     AS type,
                 glossary.source,
                glossary.is_hidden_entity
         FROM glossary
         UNION
         SELECT class.url,
                class.name,
                class.english,
                class.alternative,
                class.source_page AS page,
                'CLASS'::text     AS type,
             class.source,
                class.is_hidden_entity
         FROM class
     ) entity
         JOIN books ON entity.source::text = books.source_acronym::text
WHERE entity.is_hidden_entity = false;
