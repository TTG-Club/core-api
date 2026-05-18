-- ============================================================
-- Migration: URL as PK -> UUID as PK
-- URL remains as a unique, indexed, mutable field for routing
-- ============================================================

-- Enable uuid extension if not already enabled
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- ============================================================
-- STEP 1: Add UUID columns to all entity tables
-- ============================================================

ALTER TABLE species ADD COLUMN IF NOT EXISTS id UUID;
ALTER TABLE class ADD COLUMN IF NOT EXISTS id UUID;
ALTER TABLE spell ADD COLUMN IF NOT EXISTS id UUID;
ALTER TABLE feat ADD COLUMN IF NOT EXISTS id UUID;
ALTER TABLE background ADD COLUMN IF NOT EXISTS id UUID;
ALTER TABLE bestiary ADD COLUMN IF NOT EXISTS id UUID;
ALTER TABLE glossary ADD COLUMN IF NOT EXISTS id UUID;
ALTER TABLE item ADD COLUMN IF NOT EXISTS id UUID;
ALTER TABLE magic_item ADD COLUMN IF NOT EXISTS id UUID;

-- ============================================================
-- STEP 2: Generate UUIDs for all existing records
-- ============================================================

UPDATE species SET id = gen_random_uuid() WHERE id IS NULL;
UPDATE class SET id = gen_random_uuid() WHERE id IS NULL;
UPDATE spell SET id = gen_random_uuid() WHERE id IS NULL;
UPDATE feat SET id = gen_random_uuid() WHERE id IS NULL;
UPDATE background SET id = gen_random_uuid() WHERE id IS NULL;
UPDATE bestiary SET id = gen_random_uuid() WHERE id IS NULL;
UPDATE glossary SET id = gen_random_uuid() WHERE id IS NULL;
UPDATE item SET id = gen_random_uuid() WHERE id IS NULL;
UPDATE magic_item SET id = gen_random_uuid() WHERE id IS NULL;

-- ============================================================
-- STEP 3: Set UUID columns NOT NULL and DEFAULT
-- ============================================================

ALTER TABLE species ALTER COLUMN id SET NOT NULL;
ALTER TABLE class ALTER COLUMN id SET NOT NULL;
ALTER TABLE spell ALTER COLUMN id SET NOT NULL;
ALTER TABLE feat ALTER COLUMN id SET NOT NULL;
ALTER TABLE background ALTER COLUMN id SET NOT NULL;
ALTER TABLE bestiary ALTER COLUMN id SET NOT NULL;
ALTER TABLE glossary ALTER COLUMN id SET NOT NULL;
ALTER TABLE item ALTER COLUMN id SET NOT NULL;
ALTER TABLE magic_item ALTER COLUMN id SET NOT NULL;

ALTER TABLE species ALTER COLUMN id SET DEFAULT gen_random_uuid();
ALTER TABLE class ALTER COLUMN id SET DEFAULT gen_random_uuid();
ALTER TABLE spell ALTER COLUMN id SET DEFAULT gen_random_uuid();
ALTER TABLE feat ALTER COLUMN id SET DEFAULT gen_random_uuid();
ALTER TABLE background ALTER COLUMN id SET DEFAULT gen_random_uuid();
ALTER TABLE bestiary ALTER COLUMN id SET DEFAULT gen_random_uuid();
ALTER TABLE glossary ALTER COLUMN id SET DEFAULT gen_random_uuid();
ALTER TABLE item ALTER COLUMN id SET DEFAULT gen_random_uuid();
ALTER TABLE magic_item ALTER COLUMN id SET DEFAULT gen_random_uuid();

-- ============================================================
-- STEP 4: Add UUID columns to join tables
-- ============================================================

-- spell_species_affiliation
ALTER TABLE spell_species_affiliation ADD COLUMN IF NOT EXISTS spell_id UUID;
ALTER TABLE spell_species_affiliation ADD COLUMN IF NOT EXISTS species_affiliation_id UUID;

-- spell_class_affiliation
ALTER TABLE spell_class_affiliation ADD COLUMN IF NOT EXISTS spell_id UUID;
ALTER TABLE spell_class_affiliation ADD COLUMN IF NOT EXISTS class_affiliation_id UUID;

-- spell_subclass_affiliation
ALTER TABLE spell_subclass_affiliation ADD COLUMN IF NOT EXISTS spell_id UUID;
ALTER TABLE spell_subclass_affiliation ADD COLUMN IF NOT EXISTS subclass_affiliation_id UUID;

-- spell_lineage_affiliation
ALTER TABLE spell_lineage_affiliation ADD COLUMN IF NOT EXISTS spell_id UUID;
ALTER TABLE spell_lineage_affiliation ADD COLUMN IF NOT EXISTS lineage_affiliation_id UUID;

-- spell_feat_affiliation (already uses spell_id/feat_id names but stores url strings)
ALTER TABLE spell_feat_affiliation ADD COLUMN IF NOT EXISTS spell_uuid UUID;
ALTER TABLE spell_feat_affiliation ADD COLUMN IF NOT EXISTS feat_uuid UUID;

-- species self-reference
ALTER TABLE species ADD COLUMN IF NOT EXISTS parent_id UUID;

-- class self-reference
ALTER TABLE class ADD COLUMN IF NOT EXISTS parent_id UUID;

-- ============================================================
-- STEP 5: Populate UUID columns in join tables
-- ============================================================

UPDATE spell_species_affiliation jt
SET spell_id = s.id FROM spell s WHERE jt.spell_url = s.url;

UPDATE spell_species_affiliation jt
SET species_affiliation_id = sp.id FROM species sp WHERE jt.species_affiliation_url = sp.url;

UPDATE spell_class_affiliation jt
SET spell_id = s.id FROM spell s WHERE jt.spell_url = s.url;

UPDATE spell_class_affiliation jt
SET class_affiliation_id = c.id FROM class c WHERE jt.class_affiliation_url = c.url;

UPDATE spell_subclass_affiliation jt
SET spell_id = s.id FROM spell s WHERE jt.spell_url = s.url;

UPDATE spell_subclass_affiliation jt
SET subclass_affiliation_id = c.id FROM class c WHERE jt.subclass_affiliation_url = c.url;

UPDATE spell_lineage_affiliation jt
SET spell_id = s.id FROM spell s WHERE jt.spell_url = s.url;

UPDATE spell_lineage_affiliation jt
SET lineage_affiliation_id = sp.id FROM species sp WHERE jt.lineage_affiliation_url = sp.url;

UPDATE spell_feat_affiliation jt
SET spell_uuid = s.id FROM spell s WHERE jt.spell_id = s.url;

UPDATE spell_feat_affiliation jt
SET feat_uuid = f.id FROM feat f WHERE jt.feat_id = f.url;

-- species parent
UPDATE species child
SET parent_id = parent.id FROM species parent WHERE child.parent_url = parent.url;

-- class parent
UPDATE class child
SET parent_id = parent.id FROM class parent WHERE child.parent_url = parent.url;

-- ============================================================
-- STEP 6: Drop old FK constraints
-- ============================================================

ALTER TABLE spell_species_affiliation DROP CONSTRAINT IF EXISTS fk_spespeaff_on_spell;
ALTER TABLE spell_species_affiliation DROP CONSTRAINT IF EXISTS fk_spespeaff_on_species;

ALTER TABLE spell_class_affiliation DROP CONSTRAINT IF EXISTS fk_spell_class_affiliation_spell;
ALTER TABLE spell_class_affiliation DROP CONSTRAINT IF EXISTS fk_spell_class_affiliation_class;

ALTER TABLE spell_subclass_affiliation DROP CONSTRAINT IF EXISTS fk_spell_subclass_affiliation_spell;
ALTER TABLE spell_subclass_affiliation DROP CONSTRAINT IF EXISTS fk_spell_subclass_affiliation_subclass;

ALTER TABLE spell_lineage_affiliation DROP CONSTRAINT IF EXISTS fk_spell_lineage_affiliation_spell;
ALTER TABLE spell_lineage_affiliation DROP CONSTRAINT IF EXISTS fk_spell_lineage_affiliation_species;

ALTER TABLE spell_feat_affiliation DROP CONSTRAINT IF EXISTS fk_spell_feat_spell;
ALTER TABLE spell_feat_affiliation DROP CONSTRAINT IF EXISTS fk_spell_feat_feat;

ALTER TABLE species DROP CONSTRAINT IF EXISTS species_parent_url_fkey;
ALTER TABLE species DROP CONSTRAINT IF EXISTS fk_species_parent;

ALTER TABLE class DROP CONSTRAINT IF EXISTS class_parent_url_fkey;
ALTER TABLE class DROP CONSTRAINT IF EXISTS fk_class_parent;

-- ============================================================
-- STEP 7: Drop old PK constraints
-- ============================================================

ALTER TABLE species DROP CONSTRAINT IF EXISTS species_pkey;
ALTER TABLE class DROP CONSTRAINT IF EXISTS class_pkey;
ALTER TABLE spell DROP CONSTRAINT IF EXISTS spell_pkey;
ALTER TABLE feat DROP CONSTRAINT IF EXISTS feat_pkey;
ALTER TABLE background DROP CONSTRAINT IF EXISTS background_pkey;
ALTER TABLE bestiary DROP CONSTRAINT IF EXISTS bestiary_pkey;
ALTER TABLE glossary DROP CONSTRAINT IF EXISTS glossary_pkey;
ALTER TABLE item DROP CONSTRAINT IF EXISTS item_pkey;
ALTER TABLE magic_item DROP CONSTRAINT IF EXISTS magic_item_pkey;

ALTER TABLE spell_species_affiliation DROP CONSTRAINT IF EXISTS spell_species_affiliation_pkey;
ALTER TABLE spell_species_affiliation DROP CONSTRAINT IF EXISTS pk_spell_species_affiliation;
ALTER TABLE spell_class_affiliation DROP CONSTRAINT IF EXISTS pk_spell_class_affiliation;
ALTER TABLE spell_subclass_affiliation DROP CONSTRAINT IF EXISTS pk_spell_subclass_affiliation;
ALTER TABLE spell_lineage_affiliation DROP CONSTRAINT IF EXISTS pk_spell_lineage_affiliation;
ALTER TABLE spell_feat_affiliation DROP CONSTRAINT IF EXISTS pk_spell_feat_affiliation;

-- ============================================================
-- STEP 8: Add new PK constraints on entity tables
-- ============================================================

ALTER TABLE species ADD CONSTRAINT species_pkey PRIMARY KEY (id);
ALTER TABLE class ADD CONSTRAINT class_pkey PRIMARY KEY (id);
ALTER TABLE spell ADD CONSTRAINT spell_pkey PRIMARY KEY (id);
ALTER TABLE feat ADD CONSTRAINT feat_pkey PRIMARY KEY (id);
ALTER TABLE background ADD CONSTRAINT background_pkey PRIMARY KEY (id);
ALTER TABLE bestiary ADD CONSTRAINT bestiary_pkey PRIMARY KEY (id);
ALTER TABLE glossary ADD CONSTRAINT glossary_pkey PRIMARY KEY (id);
ALTER TABLE item ADD CONSTRAINT item_pkey PRIMARY KEY (id);
ALTER TABLE magic_item ADD CONSTRAINT magic_item_pkey PRIMARY KEY (id);

-- ============================================================
-- STEP 9: Add UNIQUE constraints on url columns
-- ============================================================

ALTER TABLE species ADD CONSTRAINT uq_species_url UNIQUE (url);
ALTER TABLE class ADD CONSTRAINT uq_class_url UNIQUE (url);
ALTER TABLE spell ADD CONSTRAINT uq_spell_url UNIQUE (url);
ALTER TABLE feat ADD CONSTRAINT uq_feat_url UNIQUE (url);
ALTER TABLE background ADD CONSTRAINT uq_background_url UNIQUE (url);
ALTER TABLE bestiary ADD CONSTRAINT uq_bestiary_url UNIQUE (url);
ALTER TABLE glossary ADD CONSTRAINT uq_glossary_url UNIQUE (url);
ALTER TABLE item ADD CONSTRAINT uq_item_url UNIQUE (url);
ALTER TABLE magic_item ADD CONSTRAINT uq_magic_item_url UNIQUE (url);

-- ============================================================
-- STEP 10: Drop old url columns from join tables, finalize UUID columns
-- ============================================================

-- spell_species_affiliation
ALTER TABLE spell_species_affiliation DROP COLUMN spell_url;
ALTER TABLE spell_species_affiliation DROP COLUMN species_affiliation_url;
ALTER TABLE spell_species_affiliation ALTER COLUMN spell_id SET NOT NULL;
ALTER TABLE spell_species_affiliation ALTER COLUMN species_affiliation_id SET NOT NULL;
ALTER TABLE spell_species_affiliation ADD CONSTRAINT pk_spell_species_affiliation PRIMARY KEY (spell_id, species_affiliation_id);

-- spell_class_affiliation
ALTER TABLE spell_class_affiliation DROP COLUMN spell_url;
ALTER TABLE spell_class_affiliation DROP COLUMN class_affiliation_url;
ALTER TABLE spell_class_affiliation ALTER COLUMN spell_id SET NOT NULL;
ALTER TABLE spell_class_affiliation ALTER COLUMN class_affiliation_id SET NOT NULL;
ALTER TABLE spell_class_affiliation ADD CONSTRAINT pk_spell_class_affiliation PRIMARY KEY (spell_id, class_affiliation_id);

-- spell_subclass_affiliation
ALTER TABLE spell_subclass_affiliation DROP COLUMN spell_url;
ALTER TABLE spell_subclass_affiliation DROP COLUMN subclass_affiliation_url;
ALTER TABLE spell_subclass_affiliation ALTER COLUMN spell_id SET NOT NULL;
ALTER TABLE spell_subclass_affiliation ALTER COLUMN subclass_affiliation_id SET NOT NULL;
ALTER TABLE spell_subclass_affiliation ADD CONSTRAINT pk_spell_subclass_affiliation PRIMARY KEY (spell_id, subclass_affiliation_id);

-- spell_lineage_affiliation
ALTER TABLE spell_lineage_affiliation DROP COLUMN spell_url;
ALTER TABLE spell_lineage_affiliation DROP COLUMN lineage_affiliation_url;
ALTER TABLE spell_lineage_affiliation ALTER COLUMN spell_id SET NOT NULL;
ALTER TABLE spell_lineage_affiliation ALTER COLUMN lineage_affiliation_id SET NOT NULL;
ALTER TABLE spell_lineage_affiliation ADD CONSTRAINT pk_spell_lineage_affiliation PRIMARY KEY (spell_id, lineage_affiliation_id);

-- spell_feat_affiliation
ALTER TABLE spell_feat_affiliation DROP COLUMN spell_id;
ALTER TABLE spell_feat_affiliation DROP COLUMN feat_id;
ALTER TABLE spell_feat_affiliation RENAME COLUMN spell_uuid TO spell_id;
ALTER TABLE spell_feat_affiliation RENAME COLUMN feat_uuid TO feat_id;
ALTER TABLE spell_feat_affiliation ALTER COLUMN spell_id SET NOT NULL;
ALTER TABLE spell_feat_affiliation ALTER COLUMN feat_id SET NOT NULL;
ALTER TABLE spell_feat_affiliation ADD CONSTRAINT pk_spell_feat_affiliation PRIMARY KEY (spell_id, feat_id);

-- Drop old parent_url columns
ALTER TABLE species DROP COLUMN IF EXISTS parent_url;
ALTER TABLE class DROP COLUMN IF EXISTS parent_url;

-- ============================================================
-- STEP 11: Add new FK constraints
-- ============================================================

ALTER TABLE spell_species_affiliation
    ADD CONSTRAINT fk_spell_species_spell FOREIGN KEY (spell_id) REFERENCES spell(id) ON DELETE CASCADE;
ALTER TABLE spell_species_affiliation
    ADD CONSTRAINT fk_spell_species_species FOREIGN KEY (species_affiliation_id) REFERENCES species(id) ON DELETE CASCADE;

ALTER TABLE spell_class_affiliation
    ADD CONSTRAINT fk_spell_class_spell FOREIGN KEY (spell_id) REFERENCES spell(id) ON DELETE CASCADE;
ALTER TABLE spell_class_affiliation
    ADD CONSTRAINT fk_spell_class_class FOREIGN KEY (class_affiliation_id) REFERENCES class(id) ON DELETE CASCADE;

ALTER TABLE spell_subclass_affiliation
    ADD CONSTRAINT fk_spell_subclass_spell FOREIGN KEY (spell_id) REFERENCES spell(id) ON DELETE CASCADE;
ALTER TABLE spell_subclass_affiliation
    ADD CONSTRAINT fk_spell_subclass_subclass FOREIGN KEY (subclass_affiliation_id) REFERENCES class(id) ON DELETE CASCADE;

ALTER TABLE spell_lineage_affiliation
    ADD CONSTRAINT fk_spell_lineage_spell FOREIGN KEY (spell_id) REFERENCES spell(id) ON DELETE CASCADE;
ALTER TABLE spell_lineage_affiliation
    ADD CONSTRAINT fk_spell_lineage_species FOREIGN KEY (lineage_affiliation_id) REFERENCES species(id) ON DELETE CASCADE;

ALTER TABLE spell_feat_affiliation
    ADD CONSTRAINT fk_spell_feat_spell FOREIGN KEY (spell_id) REFERENCES spell(id) ON DELETE CASCADE;
ALTER TABLE spell_feat_affiliation
    ADD CONSTRAINT fk_spell_feat_feat FOREIGN KEY (feat_id) REFERENCES feat(id) ON DELETE CASCADE;

ALTER TABLE species
    ADD CONSTRAINT fk_species_parent FOREIGN KEY (parent_id) REFERENCES species(id) ON DELETE SET NULL;

ALTER TABLE class
    ADD CONSTRAINT fk_class_parent FOREIGN KEY (parent_id) REFERENCES class(id) ON DELETE SET NULL;

-- ============================================================
-- STEP 12: Recreate indexes on join tables
-- ============================================================

DROP INDEX IF EXISTS ix_sca_spell;
DROP INDEX IF EXISTS ix_sca_class;
DROP INDEX IF EXISTS ix_ssa_spell;
DROP INDEX IF EXISTS ix_ssa_subclass;
DROP INDEX IF EXISTS ix_sla_spell;
DROP INDEX IF EXISTS ix_sla_lineage;
DROP INDEX IF EXISTS idx_spell_feat_spell;
DROP INDEX IF EXISTS idx_spell_feat_feat;

CREATE INDEX ix_spell_species_spell ON spell_species_affiliation(spell_id);
CREATE INDEX ix_spell_species_species ON spell_species_affiliation(species_affiliation_id);
CREATE INDEX ix_spell_class_spell ON spell_class_affiliation(spell_id);
CREATE INDEX ix_spell_class_class ON spell_class_affiliation(class_affiliation_id);
CREATE INDEX ix_spell_subclass_spell ON spell_subclass_affiliation(spell_id);
CREATE INDEX ix_spell_subclass_subclass ON spell_subclass_affiliation(subclass_affiliation_id);
CREATE INDEX ix_spell_lineage_spell ON spell_lineage_affiliation(spell_id);
CREATE INDEX ix_spell_lineage_lineage ON spell_lineage_affiliation(lineage_affiliation_id);
CREATE INDEX ix_spell_feat_spell ON spell_feat_affiliation(spell_id);
CREATE INDEX ix_spell_feat_feat ON spell_feat_affiliation(feat_id);

-- ============================================================
-- STEP 13: Recreate full_name_search_view
-- ============================================================

DROP VIEW IF EXISTS full_name_search_view;
CREATE VIEW full_name_search_view AS
SELECT entity.id,
       entity.url,
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
         SELECT species.id,
                species.url,
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
         SELECT spell.id,
                spell.url,
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
         SELECT feat.id,
                feat.url,
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
         SELECT background.id,
                background.url,
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
         SELECT bestiary.id,
                bestiary.url,
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
         SELECT magic_item.id,
                magic_item.url,
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
         SELECT item.id,
                item.url,
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
         SELECT glossary.id,
                glossary.url,
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
         SELECT class.id,
                class.url,
                class.name,
                class.english,
                class.alternative,
                NULL::bigint     AS page,
                'CLASS'::text    AS type,
                class.source,
                class.is_hidden_entity,
                class.created_at,
                class.updated_at
         FROM class
     ) entity
         LEFT JOIN source ON entity.source = source.acronym;
