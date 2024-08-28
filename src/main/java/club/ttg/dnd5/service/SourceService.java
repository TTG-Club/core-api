package club.ttg.dnd5.service;

import club.ttg.dnd5.model.Source;

import java.util.Optional;

public interface SourceService {
    Optional<Source> findSource(String source);
}
