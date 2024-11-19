INSERT INTO ttg.books (source_acronym, name, alt_name, english_name, description, type, year)
VALUES ('PHB', 'ПХБ', '', 'PHB', 'Player Hands Book', 'OFFICIAL', 2024);

INSERT INTO ttg.sources (page, created_at, id, updated_at, book_info_id) VALUES (155, '2024-11-07 20:10:00.731818', 1, '2024-11-07 20:10:00.731818', 'PHB');
INSERT INTO ttg.sources (page, created_at, id, updated_at, book_info_id) VALUES (156, '2024-11-07 20:10:00.808714', 2, '2024-11-07 20:10:00.808714', 'PHB');
INSERT INTO ttg.sources (page, created_at, id, updated_at, book_info_id) VALUES (181, '2024-11-11 22:17:31.755532', 3, '2024-11-11 22:17:31.755532', 'PHB');
INSERT INTO ttg.sources (page, created_at, id, updated_at, book_info_id) VALUES (181, '2024-11-11 22:17:31.796086', 4, '2024-11-11 22:17:31.796086', 'PHB');
INSERT INTO ttg.sources (page, created_at, id, updated_at, book_info_id) VALUES (181, '2024-11-11 22:17:41.462760', 5, '2024-11-11 22:17:41.462760', 'PHB');
INSERT INTO ttg.sources (page, created_at, id, updated_at, book_info_id) VALUES (181, '2024-11-11 22:17:41.464760', 6, '2024-11-11 22:17:41.464760', 'PHB');
INSERT INTO ttg.sources (page, created_at, id, updated_at, book_info_id) VALUES (202, '2024-11-11 22:22:20.761617', 7, '2024-11-11 22:22:20.761617', 'PHB');
INSERT INTO ttg.sources (page, created_at, id, updated_at, book_info_id) VALUES (202, '2024-11-11 22:22:20.764618', 8, '2024-11-11 22:22:20.764618', 'PHB');
INSERT INTO ttg.sources (page, created_at, id, updated_at, book_info_id) VALUES (202, '2024-11-11 22:22:20.766620', 9, '2024-11-11 22:22:20.767619', 'PHB');

SET FOREIGN_KEY_CHECKS = 0;
INSERT INTO ttg.species_gallery (species_id, gallery_url) VALUES
                                                          ('aasimar', 'https://img.ttg.club/races/Aasimar.webp'),
                                                          ('aasimar', 'https://i.pinimg.com/originals/2c/4e/ed/2c4eed0d3017600ae4cd05babbada000.png'),
                                                          ('aasimar', 'https://www.worldanvil.com/uploads/images/1e9bea922bdf306b2b6857d9fee6208c.png'),
                                                          ('gnome', 'https://img.ttg.club/races/Gnome.webp'),
                                                          ('gnome', 'https://i.pinimg.com/originals/2c/4e/ed/2c4eed0d3017600ae4cd05babbada000.png'),
                                                          ('gnome', 'https://www.worldanvil.com/uploads/images/1e9bea922bdf306b2b6857d9fee6208c.png'),
                                                          ('forest-gnome', 'https://img.ttg.club/races/ForestGnome.webp'),
                                                          ('forest-gnome', 'https://i.pinimg.com/originals/2c/4e/ed/2c4eed0d3017600ae4cd05babbada000.png'),
                                                          ('forest-gnome', 'https://www.worldanvil.com/uploads/images/1e9bea922bdf306b2b6857d9fee6208c.png'),
                                                          ('rock-gnome', 'https://img.ttg.club/races/RockGnome.webp'),
                                                          ('rock-gnome', 'https://i.pinimg.com/originals/2c/4e/ed/2c4eed0d3017600ae4cd05babbada000.png'),
                                                          ('rock-gnome', 'https://www.worldanvil.com/uploads/images/1e9bea922bdf306b2b6857d9fee6208c.png');
SET FOREIGN_KEY_CHECKS = 1;

INSERT INTO ttg.species (climb, dark_vision, fly, is_hidden_entity, speed, swim, created_at, source, updated_at, alternative, description, english, image_url, name, parent_id, url, size, type) VALUES (0, 60, 0, false, 30, 0, '2024-11-07 20:10:00.839712', 1, '2024-11-07 20:10:00.850710', 'Angel-blooded', 'Aasimar are mortals with celestial heritage. They have traits hinting at their divine ancestry, like glowing eyes or metallic freckles.', 'Aasimar', 'https:/ttg.club/aasimar/picture', 'Aasimar', 'aasimar', 'aasimar', 'MEDIUM', 'HUMANOID');
INSERT INTO ttg.species (climb, dark_vision, fly, is_hidden_entity, speed, swim, created_at, source, updated_at, alternative, description, english, image_url, name, parent_id, url, size, type) VALUES (0, 60, 0, false, 30, 0, '2024-11-11 22:17:31.827950', 3, '2024-11-11 22:17:31.841945', 'Little People', 'Гномы – волшебные существа, созданные богами изобретений, иллюзий и подземной жизни. Они скрытные и обладают уникальными магическими способностями.', 'Gnome', 'https:/ttg.club/gnome/picture', 'Гном', 'gnome', 'gnome', 'SMALL', 'HUMANOID');
INSERT INTO ttg.species (climb, dark_vision, fly, is_hidden_entity, speed, swim, created_at, source, updated_at, alternative, description, english, image_url, name, parent_id, url, size, type) VALUES (0, 60, 0, false, 30, 0, '2024-11-11 22:17:41.470760', 5, '2024-11-11 22:17:45.808278', 'Woodland Trickster', 'Лесные гномы живут в лесах и наделены магией иллюзий, чтобы защищаться и выживать.', 'Forest Gnome', 'https:/ttg.club/gnome/forest/picture', 'Лесной Гном', 'gnome', 'forest-gnome', 'SMALL', 'HUMANOID');
INSERT INTO ttg.species (climb, dark_vision, fly, is_hidden_entity, speed, swim, created_at, source, updated_at, alternative, description, english, image_url, name, parent_id, url, size, type) VALUES (0, 60, 0, false, 30, 0, '2024-11-11 22:22:20.776619', 7, '2024-11-11 22:22:40.628185', 'Tinker Gnome', 'Горные гномы – изобретательные создания, способные создавать крошечные механические устройства.', 'Rock Gnome', 'https:/ttg.club/gnome/rock/picture', 'Горный Гном', 'gnome', 'rock-gnome', 'SMALL', 'HUMANOID');

INSERT INTO ttg.species_features (is_hidden_entity, created_at, source, updated_at, alternative, description, english, feature_description, image_url, name, species_url, url) VALUES (false, '2024-11-11 22:17:31.805949', 4, '2024-11-11 22:17:31.836948', null, 'Преимущество на спасброски Интеллекта, Мудрости и Харизмы.', 'Gnomish Cunning', 'Гномы обладают хитростью, позволяющей им противостоять ментальным эффектам.', null, 'Гномья Хитрость', 'gnome', 'gnome/gnomish-cunning');
INSERT INTO ttg.species_features (is_hidden_entity, created_at, source, updated_at, alternative, description, english, feature_description, image_url, name, species_url, url) VALUES (false, '2024-11-11 22:22:20.771620', 8, '2024-11-11 22:22:20.777618', null, 'Вы получаете удвоенное мастерство при проверке Истории, касающейся магических, алхимических или технологических объектов.', 'Artificer''s Lore', 'Горные гномы хорошо разбираются в магических и технологических предметах.', null, 'Артифициерские знания', 'rock-gnome', 'rock-gnome/artificer-lore');
INSERT INTO ttg.species_features (is_hidden_entity, created_at, source, updated_at, alternative, description, english, feature_description, image_url, name, species_url, url) VALUES (false, '2024-11-07 20:10:00.820711', 2, '2024-11-07 20:10:00.847710', 'Light Carrier', 'You know the Light cantrip, allowing you to emit light to brighten your surroundings.', 'Light Bearer', 'This feature allows the aasimar to use the Light cantrip at will.', 'aasimar/picture', 'Light Bearer', 'aasimar', 'aasimar/light-bearer');
INSERT INTO ttg.species_features (is_hidden_entity, created_at, source, updated_at, alternative, description, english, feature_description, image_url, name, species_url, url) VALUES (false, '2024-11-11 22:17:41.468761', 6, '2024-11-11 22:17:41.471761', null, 'Вы знаете заговор Малая Иллюзия.', 'Natural Illusionist', 'Лесные гномы умеют использовать Малую Иллюзию для маскировки и защиты.', null, 'Природный Иллюзионист', 'forest-gnome', 'forest-gnome/natural-illusionist');
INSERT INTO ttg.species_features (is_hidden_entity, created_at, source, updated_at, alternative, description, english, feature_description, image_url, name, species_url, url) VALUES (false, '2024-11-11 22:22:20.773619', 9, '2024-11-11 22:22:20.777618', null, 'Вы можете создавать небольшие механические устройства, такие как игрушка или музыкальная шкатулка.', 'Tinker', 'Горные гномы могут создавать механические устройства для различных целей.', null, 'Изобретатель', 'rock-gnome', 'rock-gnome/tinker');

INSERT INTO ttg.entity_tags (entity_url, tag_key, tag_value) VALUES ('aasimar/light-bearer', 'raciua', 'aasimar');
INSERT INTO ttg.entity_tags (entity_url, tag_key, tag_value) VALUES ('aasimar/light-bearer', 'light', 'aasimar');
INSERT INTO ttg.entity_tags (entity_url, tag_key, tag_value) VALUES ('aasimar/light-bearer', 'magic', 'aasimar');
INSERT INTO ttg.entity_tags (entity_url, tag_key, tag_value) VALUES ('forest-gnome/natural-illusionist', 'gnome', 'forest-gnome');
INSERT INTO ttg.entity_tags (entity_url, tag_key, tag_value) VALUES ('forest-gnome/natural-illusionist', 'magic', 'forest-gnome');
INSERT INTO ttg.entity_tags (entity_url, tag_key, tag_value) VALUES ('gnome/gnomish-cunning', 'gnome', 'gnome');
INSERT INTO ttg.entity_tags (entity_url, tag_key, tag_value) VALUES ('gnome/gnomish-cunning', 'magic', 'gnome');
INSERT INTO ttg.entity_tags (entity_url, tag_key, tag_value) VALUES ('rock-gnome/artificer-lore', 'gnome', 'rock-gnome');
INSERT INTO ttg.entity_tags (entity_url, tag_key, tag_value) VALUES ('rock-gnome/artificer-lore', 'skill', 'rock-gnome');
INSERT INTO ttg.entity_tags (entity_url, tag_key, tag_value) VALUES ('rock-gnome/tinker', 'craft', 'rock-gnome');
INSERT INTO ttg.entity_tags (entity_url, tag_key, tag_value) VALUES ('rock-gnome/tinker', 'gnome', 'rock-gnome');
