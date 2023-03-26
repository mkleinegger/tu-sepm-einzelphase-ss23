package at.ac.tuwien.sepm.assignment.individual.dto;

import at.ac.tuwien.sepm.assignment.individual.type.Sex;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

/**
 * Class for Horse DTOs
 * Contains all common properties. {@code owner} and {@code description} can
 * be null, whereas {@code name}, {@code dateOfBirth}, {@code sex} and {@code id}
 * are required fields.
 *
 * @param id          id of the horse
 * @param name        Name of the horse
 * @param description Description of the horse
 * @param dateOfBirth Date of birth of the Horse
 * @param sex         Sex of the Horse (Male, Female)
 * @param owner       Owner of the horse
 */
public record HorseListDto(
    Long id,
    String name,
    String description,
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    LocalDate dateOfBirth,
    Sex sex,
    OwnerDto owner
) {
  public Long ownerId() {
    return owner == null
        ? null
        : owner.id();
  }
}
