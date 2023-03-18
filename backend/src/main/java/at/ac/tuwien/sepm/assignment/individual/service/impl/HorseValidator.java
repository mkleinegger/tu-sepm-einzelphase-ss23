package at.ac.tuwien.sepm.assignment.individual.service.impl;

import at.ac.tuwien.sepm.assignment.individual.dto.HorseDetailDto;
import at.ac.tuwien.sepm.assignment.individual.exception.ConflictException;
import at.ac.tuwien.sepm.assignment.individual.exception.ValidationException;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class HorseValidator {
  private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());


  public void validateForUpdate(HorseDetailDto horse) throws ValidationException, ConflictException {
    LOG.trace("validateForUpdate({})", horse);
    List<String> validationErrors = new ArrayList<>();

    if (horse.id() == null) {
      validationErrors.add("No ID given");
    }

    if (horse.description() != null) {
      if (horse.description().isBlank()) {
        validationErrors.add("Horse description is given but blank");
      }
      if (horse.description().length() > 4095) {
        validationErrors.add("Horse description too long: longer than 4095 characters");
      }
    }

    // TODO this is not complete…

    if (!validationErrors.isEmpty()) {
      throw new ValidationException("Validation of horse for update failed", validationErrors);
    }
  }

  public void validateForCreate(HorseCreateDto newHorse) throws ValidationException, ConflictException {
    LOG.trace("validateForCreate({})", newHorse);
    List<String> validationErrors = new ArrayList<>();

    if (newHorse.name() == null) {
      validationErrors.add("Horse name is required");
    } else {
      if (newHorse.name().isBlank()) {
        validationErrors.add("Horse name is required");
      }
    }

    if (newHorse.dateOfBirth() == null) {
      validationErrors.add("Horse date-of-birth is required");
    } else {
      //TODO: check for format
    }

    if (newHorse.sex() == null) {
      validationErrors.add("Horse sex is required");
    }

    if (newHorse.description() != null) {
      if (newHorse.description().isBlank()) {
        validationErrors.add("Horse description is given but blank");
      }

      if (newHorse.description().length() > 4095) {
        validationErrors.add("Horse description too long: longer than 4095 characters");
      }
    }

    if (!validationErrors.isEmpty()) {
      throw new ValidationException("Validation of horse for update failed", validationErrors);
    }
  }

}
