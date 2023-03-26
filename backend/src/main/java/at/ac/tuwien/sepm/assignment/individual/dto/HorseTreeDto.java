package at.ac.tuwien.sepm.assignment.individual.dto;

import at.ac.tuwien.sepm.assignment.individual.type.Sex;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

/**
 * DTO to encapsulate the family tree of a horse.
 *
 * @param id          id of the horse
 * @param name        Name of the horse
 * @param dateOfBirth Date of birth of the horse
 * @param sex         Sex of the horse
 * @param generation  Generation of this horse
 * @param mother      Mother of this horse
 * @param father      Father of this horse
 */
public record HorseTreeDto(
    Long id,
    String name,
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    LocalDate dateOfBirth,
    Sex sex,
    Integer generation,
    HorseTreeDto mother,
    HorseTreeDto father
) {

}
