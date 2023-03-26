package at.ac.tuwien.sepm.assignment.individual.dto;

/**
 * DTO to encapsulate properties to get an owner.
 *
 * @param id        id of an owner
 * @param firstName Firstname of an owner
 * @param lastName  Lastname of an owner
 * @param email     Email of an owner
 */
public record OwnerDto(
    long id,
    String firstName,
    String lastName,
    String email
) {
}
