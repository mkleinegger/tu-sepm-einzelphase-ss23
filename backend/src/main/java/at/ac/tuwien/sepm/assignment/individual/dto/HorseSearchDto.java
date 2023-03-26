package at.ac.tuwien.sepm.assignment.individual.dto;

import at.ac.tuwien.sepm.assignment.individual.type.Sex;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

/**
 * DTO to bundle the query parameters used in searching horses.
 * Each field can be null, in which case this field is not filtered by.
 *
 * @param name        substring of the horse's name
 * @param description substring of the horse's description
 * @param bornBefore  date horses need to be born before
 * @param sex         sex of the horse
 * @param ownerName   substring of the owner's name
 * @param limit       the maximum number of horses to return, even if there are more matches
 */
public record HorseSearchDto(
    String name,
    String description,
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    LocalDate bornBefore,
    Sex sex,
    String ownerName,
    Integer limit
) {
}
