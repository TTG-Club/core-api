package club.ttg.dnd5.utills.converters.spell;

import club.ttg.dnd5.dto.base.NameBasedDTO;
import club.ttg.dnd5.dto.book.SourceBookDTO;
import club.ttg.dnd5.dto.spell.component.MagicSchoolDto;
import club.ttg.dnd5.model.book.Book;
import club.ttg.dnd5.model.book.TypeBook;
import club.ttg.dnd5.model.spell.component.MagicSchool;
import club.ttg.dnd5.utills.converters.EntityToDtoConverter;

import java.util.ArrayList;

public class MagicSchoolConverter implements EntityToDtoConverter<MagicSchool, MagicSchoolDto> {

    @Override
    public MagicSchoolDto convertToDto(MagicSchool entity) {
        // Create SourceBookDTO if Book is not null
        SourceBookDTO sourceBookDTO = new SourceBookDTO();
        if (entity.getBook() != null) {
            sourceBookDTO.setUrl(entity.getBook().getUrl());
            sourceBookDTO.setName(new NameBasedDTO(entity.getBook().getSourceAcronym(), "", new ArrayList<>(), ""));
            sourceBookDTO.setDescription(entity.getBook().getDescription());
            sourceBookDTO.setYear(entity.getBook().getBookDate()); // Set the year from bookDate
            sourceBookDTO.setType(entity.getBook().getType() != null ? entity.getBook().getType().name() : null); // Handle enum type
        }

        // Return MagicSchoolDTO with the transformed fields
        return MagicSchoolDto.builder()
                .name(new NameBasedDTO(entity.getMagicSchooName(), entity.getEnglishName(), new ArrayList<>(), ""))
                .description(entity.getMagicSchoolDescription())
                .source(sourceBookDTO)
                .build();
    }

    @Override
    public MagicSchool convertToEntity(MagicSchoolDto dto) {
        MagicSchool school = new MagicSchool();

        // Handle name field (MagicSchoolDTO -> MagicSchool entity)
        if (dto.getName() != null) {
            school.setMagicSchooName(dto.getName().getName());
            school.setEnglishName("");
        }

        school.setMagicSchoolDescription(dto.getDescription());
        if (dto.getSource() != null) {
            Book book = new Book();
            book.setSourceAcronym(dto.getSource().getName().getName()); // Set source acronym
            book.setUrl(dto.getSource().getUrl());
            book.setDescription(dto.getSource().getDescription());
            book.setBookDate(dto.getSource().getYear()); // Set bookDate as the year
            if (dto.getSource().getType() != null) {
                try {
                    book.setType(TypeBook.parse(dto.getSource().getType()));
                } catch (IllegalArgumentException e) {
                    book.setType(null);
                }
            }
            school.setBook(book);
        }
        return school;
    }
}