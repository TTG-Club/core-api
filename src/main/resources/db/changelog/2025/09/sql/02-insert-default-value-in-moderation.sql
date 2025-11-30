insert into admin (url, created_at, updated_at, username, section_type, status_type)
SELECT url, created_at, updated_at, username, 'BACKGROUND', 'PUBLIC'
from background;

insert into admin (url, created_at, updated_at, username, section_type, status_type)
SELECT url, created_at, updated_at, username, 'FEAT', 'PUBLIC'
from feat;

insert into admin (url, created_at, updated_at, username, section_type, status_type)
SELECT url, created_at, updated_at, username, 'SPELL', 'PUBLIC'
from spell;

insert into admin (url, created_at, updated_at, username, section_type, status_type)
SELECT url, created_at, updated_at, username, 'BESTIARY', 'PUBLIC'
from bestiary;

insert into admin (url, created_at, updated_at, username, section_type, status_type)
SELECT url, created_at, updated_at, username, 'MAGIC_ITEM', 'PUBLIC'
from magic_item;

insert into admin (url, created_at, updated_at, username, section_type, status_type)
SELECT url, created_at, updated_at, username, 'ITEM', 'PUBLIC'
from item;

insert into admin (url, created_at, updated_at, username, section_type, status_type)
SELECT url, created_at, updated_at, username, 'GLOSSARY', 'PUBLIC'
from glossary;

insert into admin (url, created_at, updated_at, username, section_type, status_type)
SELECT url, created_at, updated_at, username, 'CLASS', 'PUBLIC'
from class;