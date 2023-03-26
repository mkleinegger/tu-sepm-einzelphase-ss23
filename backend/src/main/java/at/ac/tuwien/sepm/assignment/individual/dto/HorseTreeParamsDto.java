package at.ac.tuwien.sepm.assignment.individual.dto;

/**
 * DTO to encapsulate parameters for family tree generation.
 *
 * @param numberOfGenerations The number of generations that should be loaded.
 */
public record HorseTreeParamsDto(
    Integer numberOfGenerations
) {

}
