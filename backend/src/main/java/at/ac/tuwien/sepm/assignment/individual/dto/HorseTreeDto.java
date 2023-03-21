package at.ac.tuwien.sepm.assignment.individual.dto;

import at.ac.tuwien.sepm.assignment.individual.type.Sex;

import java.time.LocalDate;

public record HorseTreeDto(
    Long id,
    String name,
    LocalDate dateOfBirth,
    Sex sex,
    Integer generation,
    HorseTreeDto mother,
    HorseTreeDto father
) {

}
