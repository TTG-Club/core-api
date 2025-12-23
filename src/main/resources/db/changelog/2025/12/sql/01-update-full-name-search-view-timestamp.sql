DROP VIEW IF EXISTS full_name_search_view;
CREATE VIEW full_name_search_view AS
SELECT entity.url,
       entity.name,
       entity.english,
       entity.alternative,
       entity.page,
       entity.type,
       entity.is_hidden_entity,
       source.acronym AS acronym,
       source.name    AS source_name,
       source.english AS source_english,
       source.type    AS source_type,
       entity.created_at,
       entity.updated_at
FROM (
         SELECT species.url,
                species.name,
                species.english,
                species.alternative,
                species.source_page AS page,
                'SPECIES'::text     AS type,
                species.source,
                species.is_hidden_entity,
                species.created_at,
                species.updated_at
         FROM species

         UNION ALL
         SELECT spell.url,
                spell.name,
                spell.english,
                spell.alternative,
                spell.source_page AS page,
                'SPELL'::text     AS type,
                spell.source,
                spell.is_hidden_entity,
                spell.created_at,
                spell.updated_at
         FROM spell

         UNION ALL
         SELECT feat.url,
                feat.name,
                feat.english,
                feat.alternative,
                feat.source_page AS page,
                'FEAT'::text     AS type,
                feat.source,
                feat.is_hidden_entity,
                feat.created_at,
                feat.updated_at
         FROM feat

         UNION ALL
         SELECT background.url,
                background.name,
                background.english,
                background.alternative,
                background.source_page AS page,
                'BACKGROUND'::text AS type,
                background.source,
                background.is_hidden_entity,
                background.created_at,
                background.updated_at
         FROM background

         UNION ALL
         SELECT bestiary.url,
                bestiary.name,
                bestiary.english,
                bestiary.alternative,
                bestiary.source_page AS page,
                'BESTIARY'::text   AS type,
                bestiary.source,
                bestiary.is_hidden_entity,
                bestiary.created_at,
                bestiary.updated_at
         FROM bestiary

         UNION ALL
         SELECT magic_item.url,
                magic_item.name,
                magic_item.english,
                magic_item.alternative,
                magic_item.source_page AS page,
                'MAGIC_ITEM'::text AS type,
                magic_item.source,
                magic_item.is_hidden_entity,
                magic_item.created_at,
                magic_item.updated_at
         FROM magic_item

         UNION ALL
         SELECT item.url,
                item.name,
                item.english,
                item.alternative,
                item.source_page AS page,
                'ITEM'::text     AS type,
                item.source,
                item.is_hidden_entity,
                item.created_at,
                item.updated_at
         FROM item

         UNION ALL
         SELECT glossary.url,
                glossary.name,
                glossary.english,
                glossary.alternative,
                glossary.source_page AS page,
                'GLOSSARY'::text AS type,
                glossary.source,
                glossary.is_hidden_entity,
                glossary.created_at,
                glossary.updated_at
         FROM glossary

         UNION ALL
         SELECT class.url,
                class.name,
                class.english,
                class.alternative,
                class.source_page AS page,
                'CLASS'::text    AS type,
                class.source,
                class.is_hidden_entity,
                class.created_at,
                class.updated_at
         FROM class
     ) entity
         JOIN source
              ON entity.source::text = source.acronym::text
WHERE entity.is_hidden_entity = false;