
update spell sp
set upper = s.new_upper
    from (SELECT url, to_jsonb(array_agg(trim(elem)))::text as new_upper
      FROM spell,
           unnest(string_to_array(replace((upper)::text, '"', ''),
                                  '\n')) AS elem
      group by url) s
where upper is not null and sp.url = s.url;

update spell sp
set description = s.new_description
    from (SELECT url, to_jsonb(array_agg(trim(elem)))::text as new_description
      FROM spell,
           unnest(string_to_array(replace((description)::text, '"', ''),
                                  '\n')) AS elem
      group by url) s
where description is not null and sp.url = s.url;

update species spec
set description = s.new_description
    from (SELECT species.url, to_jsonb(array_agg(trim(elem)))::text as new_description
      FROM species,
           unnest(string_to_array(replace((species.description)::text, '"', ''),
                                  '\n')) AS elem
      group by url) s
where   description is not null and spec.url = s.url;

UPDATE species spec SET features  = s.new_features FROM (select url, jsonb_agg(jsonb_build_object(
        'url', feature_elem -> 'url',
        'name', feature_elem -> 'name',
        'english', feature_elem -> 'english',
        'description', (SELECT to_jsonb(array_agg(trim(elem)))::text
                        FROM unnest(string_to_array(replace((feature_elem -> 'description')::text, '"', ''),
                                                    '\n')) AS elem)
                                                                               )) AS new_features
                                                         FROM species,
                                                              jsonb_array_elements(features) AS feature_elem GROUP BY url) s
WHERE features IS NOT NULL AND spec.url = s.url;