package at.ac.tuwien.sepm.assignment.individual.service.impl;

import at.ac.tuwien.sepm.assignment.individual.dto.OwnerCreateDto;
import at.ac.tuwien.sepm.assignment.individual.exception.ValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

@Component
public class OwnerValidator {
  private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  public OwnerValidator() {
  }

  public void validateForCreate(OwnerCreateDto newOwner) throws ValidationException {
    LOG.trace("validateForCreate({})", newOwner);

    // validation errors
    List<String> validationErrors = new ArrayList<>();

    if (newOwner.firstName() != null) {
      if (newOwner.firstName().isBlank()) {
        validationErrors.add("Owner's firstname is given but blank");
      }
      if (newOwner.firstName().length() > 255) {
        validationErrors.add("Owner's firstname too long: longer than 4095 characters");
      }
    } else {
      validationErrors.add("Owner needs a firstname");
    }

    if (newOwner.lastName() != null) {
      if (newOwner.lastName().isBlank()) {
        validationErrors.add("Owner's lastName is given but blank");
      }
      if (newOwner.lastName().length() > 255) {
        validationErrors.add("Owner's lastName too long: longer than 4095 characters");
      }
    } else {
      validationErrors.add("Owner needs a lastName");
    }

    if (newOwner.email() != null && !Pattern.matches("^[\\w-.]+@[\\w-]+(\\.[\\w-]+)*$", newOwner.email())) {
      validationErrors.add("Owner's email is not valid");
    }

    if (!validationErrors.isEmpty()) {
      throw new ValidationException("Validation of owner for create failed", validationErrors);
    }
  }
}
