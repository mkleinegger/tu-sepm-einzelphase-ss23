package at.ac.tuwien.sepm.assignment.individual.dto;

/**
 * DTO to encapsulate properties to create an owner.
 * {@code firstName} and {@code lastName} are required.
 *
 * @param firstName Firstname of an owner
 * @param lastName  Lastname of an owner
 * @param email     Email of an owner
 */
public record OwnerCreateDto(
    String firstName,
    String lastName,
    String email
) {
}
