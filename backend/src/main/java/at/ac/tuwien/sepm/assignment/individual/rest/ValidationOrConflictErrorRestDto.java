package at.ac.tuwien.sepm.assignment.individual.rest;

import java.util.List;

/**
 * DTO to encapsulate validation or conflict errors
 *
 * @param message Error message to display
 * @param errors  List of errors
 */
public record ValidationOrConflictErrorRestDto(
    String message,
    List<String> errors
) {
}
