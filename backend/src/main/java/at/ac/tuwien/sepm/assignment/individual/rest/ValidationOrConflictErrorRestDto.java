package at.ac.tuwien.sepm.assignment.individual.rest;

import java.util.List;

public record ValidationOrConflictErrorRestDto(
    String message,
    List<String> errors
) {
}
