drop view if exists full_name_search_view;

create view full_name_search_view as
select entity.url,
       entity.name,
       entity.english,
       entity.alternative,
       entity.page,
       entity.type,
       entity.is_hidden_entity,
       books.source_acronym as book_acronym,
       books.name           as book_name,
       books.english_name   as book_english_name,
       books.type           as book_type
from (select species.url,
             species.name,
             species.english,
             species.alternative,
             species.source_page as page,
             'SPECIES'::text     as type,
             species.source,
             species.is_hidden_entity
      from species
      union
      select spell.url,
             spell.name,
             spell.english,
             spell.alternative,
             spell.source_page as page,
             'SPELL'::text     as type,
             spell.source,
             spell.is_hidden_entity
      from spell
      union
      select feat.url,
             feat.name,
             feat.english,
             feat.alternative,
             feat.source_page as page,
             'FEAT'::text     as type,
             feat.source,
             feat.is_hidden_entity
      from feat
      union
      select background.url,
             background.name,
             background.english,
             background.alternative,
             background.source_page as page,
             'BACKGROUND'::text     as type,
             background.source,
             background.is_hidden_entity
      from background
      union
      select bestiary.url,
             bestiary.name,
             bestiary.english,
             bestiary.alternative,
             bestiary.source_page as page,
             'BESTIARY'::text     as type,
             bestiary.source,
             bestiary.is_hidden_entity
      from bestiary) entity
         join books on entity.source::text = books.source_acronym::text
where entity.is_hidden_entity = false;
