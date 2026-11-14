-- Адрес записи (url) — первичный ключ таблиц каталога. Чтобы адрес можно было сменить
-- одним UPDATE, все внешние ключи на эти таблицы (связи заклинаний с классами и видами,
-- родитель подкласса и происхождения, связи магических предметов и т. д.) получают
-- ON UPDATE CASCADE. Ключи берутся из каталога базы: их набор и имена на стендах
-- исторически расходятся.
DO $$
DECLARE
    fk record;
    definition text;
BEGIN
    FOR fk IN
        SELECT con.conname                   AS name,
               con.conrelid::regclass        AS child,
               pg_get_constraintdef(con.oid) AS def
        FROM pg_constraint con
        WHERE con.contype = 'f'
          AND con.confupdtype <> 'c'
          AND con.confrelid::regclass::text IN ('spell', 'bestiary', '"class"', 'class', 'species', 'feat',
                                                 'background', 'item', 'magic_item', 'glossary',
                                                 'bastion_facility')
    LOOP
        definition := regexp_replace(fk.def, ' ON UPDATE (NO ACTION|RESTRICT|SET NULL|SET DEFAULT)', '');
        definition := regexp_replace(definition, '(REFERENCES [^)]*\))', '\1 ON UPDATE CASCADE');
        EXECUTE format('ALTER TABLE %s DROP CONSTRAINT %I', fk.child, fk.name);
        EXECUTE format('ALTER TABLE %s ADD CONSTRAINT %I %s', fk.child, fk.name, definition);
    END LOOP;
END
$$;
