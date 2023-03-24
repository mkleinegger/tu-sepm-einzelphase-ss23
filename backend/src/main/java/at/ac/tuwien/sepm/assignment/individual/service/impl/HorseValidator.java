package at.ac.tuwien.sepm.assignment.individual.service.impl;

import at.ac.tuwien.sepm.assignment.individual.dto.HorseCreateDto;
import at.ac.tuwien.sepm.assignment.individual.dto.HorseDetailDto;
import at.ac.tuwien.sepm.assignment.individual.entity.Horse;
import at.ac.tuwien.sepm.assignment.individual.exception.ConflictException;
import at.ac.tuwien.sepm.assignment.individual.exception.NotFoundException;
import at.ac.tuwien.sepm.assignment.individual.exception.ValidationException;
import at.ac.tuwien.sepm.assignment.individual.persistence.HorseDao;
import at.ac.tuwien.sepm.assignment.individual.persistence.OwnerDao;
import at.ac.tuwien.sepm.assignment.individual.type.Sex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.lang.invoke.MethodHandles;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Component
public class HorseValidator {
  private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private final HorseDao horseDao;
  private final OwnerDao ownerDao;

  public HorseValidator(HorseDao horseDao, OwnerDao ownerDao) {
    this.horseDao = horseDao;
    this.ownerDao = ownerDao;
  }

  public void validateForUpdate(HorseDetailDto horse) throws ValidationException, ConflictException, NotFoundException {
    LOG.trace("validateForUpdate({})", horse);
    List<String> validationErrors = new ArrayList<>();

    // validation errors
    if (horse.id() == null) {
      validationErrors.add("No ID given");
    }
    validateName(validationErrors, horse.name());
    validateDescription(validationErrors, horse.description());
    validateDateOfBirth(validationErrors, horse.dateOfBirth());
    validateSex(validationErrors, horse.sex());
    // TODO: wann die exceptions werfen
    if (!validationErrors.isEmpty()) {
      throw new ValidationException("Validation of horse for update failed", validationErrors);
    }

    // conflict errors
    List<String> conflictErrors = new ArrayList<>();

    validateOwner(conflictErrors, horse.ownerId());
    if (horse.id() != null && (horse.id() == horse.motherId() || horse.id() == horse.fatherId())) {
      conflictErrors.add("The horse cannot be its own parent");
    }
    validateDifferentParents(conflictErrors, horse.motherId(), horse.fatherId());
    validateParent(conflictErrors, horse.motherId(), Sex.FEMALE, horse.dateOfBirth());
    validateParent(conflictErrors, horse.fatherId(), Sex.MALE, horse.dateOfBirth());

    Horse oldHorseData = horseDao.getById(horse.id());
    Collection<Horse> children = horseDao.getChildren(horse.id());

    // sex cannot change with children
    if (oldHorseData.getSex() != horse.sex() && !children.isEmpty()) {
      conflictErrors.add("Cannot change sex of horse with children");
    }

    // date of birth can change, but you cannot be younger than any of your children
    if (oldHorseData.getDateOfBirth() != horse.dateOfBirth() && !children.isEmpty()) {
      for (Horse child : children) {
        if (horse.dateOfBirth().isAfter(child.getDateOfBirth())) {
          conflictErrors.add("horse cannot be younger than any of his children (e.g.: %s born at %s)".formatted(child.getName(), child.getDateOfBirth()));
          break;
        }
      }
    }

    if (!conflictErrors.isEmpty()) {
      throw new ConflictException("Validation of horse for update failed", conflictErrors);
    }
  }

  public void validateForCreate(HorseCreateDto newHorse) throws ValidationException, ConflictException {
    LOG.trace("validateForCreate({})", newHorse);

    // validation errors
    List<String> validationErrors = new ArrayList<>();

    validateName(validationErrors, newHorse.name());
    validateDescription(validationErrors, newHorse.description());
    validateDateOfBirth(validationErrors, newHorse.dateOfBirth());
    validateSex(validationErrors, newHorse.sex());
    if (!validationErrors.isEmpty()) {
      throw new ValidationException("Validation of horse for create failed", validationErrors);
    }

    // conflict errors
    List<String> conflictErrors = new ArrayList<>();

    validateOwner(conflictErrors, newHorse.ownerId());
    validateDifferentParents(conflictErrors, newHorse.motherId(), newHorse.fatherId());
    validateParent(conflictErrors, newHorse.motherId(), Sex.FEMALE, newHorse.dateOfBirth());
    validateParent(conflictErrors, newHorse.fatherId(), Sex.MALE, newHorse.dateOfBirth());

    if (!conflictErrors.isEmpty()) {
      throw new ConflictException("Validation of horse for create failed", conflictErrors);
    }
  }

  private void validateName(List<String> errors, String name) {
    LOG.trace("validateName({}, {})", errors, name);

    if (name == null || name.isBlank() || name.length() > 255) {
      errors.add("Horse needs a name");
    }
  }

  private void validateDescription(List<String> errors, String description) {
    LOG.trace("validateDescription({}, {})", errors, description);

    if (description != null) {
      if (description.isBlank()) {
        errors.add("Horse description is given but blank");
      }
      if (description.length() > 4095) {
        errors.add("Horse description too long: longer than 4095 characters");
      }
    }
  }

  private void validateDateOfBirth(List<String> errors, LocalDate dateOfBirth) {
    LOG.trace("validateDateOfBirth({}, {})", errors, dateOfBirth);

    if (dateOfBirth == null) {
      errors.add("Horse needs a date of birth");
    }
  }

  private void validateSex(List<String> errors, Sex sex) {
    LOG.trace("validateSex({}, {})", errors, sex);

    if (sex == null) {
      errors.add("Horse needs a sex");
    }
  }

  private void validateOwner(List<String> errors, Long ownerId) {
    LOG.trace("validateOwner({}, {})", errors, ownerId);

    if (ownerId != null) {
      try {
        ownerDao.getById(ownerId);
      } catch (NotFoundException e) {
        errors.add("The owner of a horse must exist");
      }
    }
  }

  private void validateDifferentParents(List<String> errors, Long motherId, Long fatherId) {
    LOG.trace("validateDifferentParents({}, {}, {})", errors, motherId, fatherId);

    if (motherId != null && fatherId != null && motherId == fatherId) {
      errors.add("Mother and father cannot be the same horse");
    }
  }

  private void validateParent(List<String> errors, Long parentId, Sex sexToCheck, LocalDate birthdayToCheck) {
    LOG.trace("validateParent({}, {}, {}, {})", errors, parentId, sexToCheck, birthdayToCheck);

    String parentIdentifier = (sexToCheck == Sex.MALE) ? "father" : "mother";

    try {
      if (parentId != null) {
        Horse parent = horseDao.getById(parentId);

        if (parent.getSex() != sexToCheck) {
          errors.add("The " + parentIdentifier + " horse must be " + sexToCheck.toString().toLowerCase());
        }
        if (parent.getDateOfBirth().isAfter(birthdayToCheck)) {
          errors.add("The horse cannot be older than it's " + parentIdentifier);
        }
      }
    } catch (NotFoundException e) {
      errors.add("The " + parentIdentifier + " of a horse must exist");
    }
  }
}
