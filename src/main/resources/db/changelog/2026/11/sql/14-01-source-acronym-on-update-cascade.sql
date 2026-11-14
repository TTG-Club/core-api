-- Акроним источника — первичный ключ source, на него ссылаются таблицы каталога.
-- Чтобы акроним можно было переименовать одним UPDATE, все внешние ключи на source
-- получают ON UPDATE CASCADE. Ключи берутся из каталога базы, а не перечисляются по
-- именам: на стендах их набор и имена исторически расходятся.
DO $$
DECLARE
    fk record;
    definition text;
BEGIN
    FOR fk IN
        SELECT con.conname                      AS name,
               con.conrelid::regclass           AS child,
               pg_get_constraintdef(con.oid)    AS def
        FROM pg_constraint con
        WHERE con.confrelid = 'source'::regclass
          AND con.contype = 'f'
          AND con.confupdtype <> 'c'
    LOOP
        definition := regexp_replace(fk.def, ' ON UPDATE (NO ACTION|RESTRICT|SET NULL|SET DEFAULT)', '');
        definition := regexp_replace(definition, '(REFERENCES [^)]*\))', '\1 ON UPDATE CASCADE');
        EXECUTE format('ALTER TABLE %s DROP CONSTRAINT %I', fk.child, fk.name);
        EXECUTE format('ALTER TABLE %s ADD CONSTRAINT %I %s', fk.child, fk.name, definition);
    END LOOP;
END
$$;
