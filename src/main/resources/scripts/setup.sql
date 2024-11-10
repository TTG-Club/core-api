INSERT INTO books (source_acronym, name, alt_name, english_name, description, type, year)
VALUES ('PHB', 'ПХБ', 'S', 'PHB', 'Player Hands Book', 'OFFICIAL', 2024);

INSERT INTO sources (id, created_at, updated_at, page, book_info_id) VALUES (1, '2024-11-07 20:10:00.731818', '2024-11-07 20:10:00.731818', 155, 'PHB');
INSERT INTO sources (id, created_at, updated_at, page, book_info_id) VALUES (2, '2024-11-07 20:10:00.808714', '2024-11-07 20:10:00.808714', 156, 'PHB');

INSERT INTO species (url, created_at, updated_at, alternative, description, english, image_url, is_hidden_entity, name, climb, dark_vision, fly, size, speed, swim, type, parent_id, source) VALUES ('assimar', '2024-11-07 20:10:00.839712', '2024-11-07 20:10:00.850710', 'Angel-blooded', 'Aasimar are mortals with celestial heritage. They have traits hinting at their divine ancestry, like glowing eyes or metallic freckles.', 'Aasimar', 'assimar/picture', false, 'Aasimar', 0, 60, 0, 'MEDIUM', 30, 0, 'HUMANOID', 'assimar', 1);
INSERT INTO species_features (url, created_at, updated_at, alternative, description, english, image_url, is_hidden_entity, name, feature_description, source, species_url) VALUES ('assimar/light-bearer', '2024-11-07 20:10:00.820711', '2024-11-07 20:10:00.847710', 'Light Carrier', 'You know the Light cantrip, allowing you to emit light to brighten your surroundings.', 'Light Bearer', 'assimar/picture', false, 'Light Bearer', 'This feature allows the aasimar to use the Light cantrip at will.', 2, 'assimar');

INSERT INTO entity_tags (entity_url, tag_value, tag_key) VALUES ('assimar/light-bearer', 'racial feature', 'aasimar');
INSERT INTO entity_tags (entity_url, tag_value, tag_key) VALUES ('assimar/light-bearer', 'illumination', 'light');
INSERT INTO entity_tags (entity_url, tag_value, tag_key) VALUES ('assimar/light-bearer', 'cantrip', 'magic');
