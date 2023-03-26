package at.ac.tuwien.sepm.assignment.individual.dto;

import at.ac.tuwien.sepm.assignment.individual.type.Sex;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

/**
 * DTO to encapsulate properties to create a horse.
 * {@code owner}, {@code mother}, {@code father} and {@code description}
 * can be null, whereas {@code name}, {@code dateOfBirth}, {@code sex} and {@code id}
 * are required fields.
 *
 * @param name        Name of the horse
 * @param description Description of the horse
 * @param dateOfBirth Date of birth of the horse
 * @param sex         Sex of the horse (Male, Female)
 * @param owner       Owner of the horse
 * @param mother      Mother of the horse
 * @param father      father of the horse
 */
public record HorseCreateDto(
    String name,
    String description,
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    LocalDate dateOfBirth,
    Sex sex,
    OwnerDto owner,
    HorseDetailDto mother,
    HorseDetailDto father
) {
  public Long ownerId() {
    return owner == null
        ? null
        : owner.id();
  }

  public Long fatherId() {
    return father == null
        ? null
        : father.id();
  }

  public Long motherId() {
    return mother == null
        ? null
        : mother.id();
  }
}
