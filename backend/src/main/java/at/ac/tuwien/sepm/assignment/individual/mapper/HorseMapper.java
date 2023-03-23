package at.ac.tuwien.sepm.assignment.individual.mapper;

import at.ac.tuwien.sepm.assignment.individual.dto.HorseDetailDto;
import at.ac.tuwien.sepm.assignment.individual.dto.HorseListDto;
import at.ac.tuwien.sepm.assignment.individual.dto.HorseTreeDto;
import at.ac.tuwien.sepm.assignment.individual.dto.OwnerDto;
import at.ac.tuwien.sepm.assignment.individual.entity.Horse;
import at.ac.tuwien.sepm.assignment.individual.exception.FatalException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.lang.invoke.MethodHandles;
import java.util.Collection;
import java.util.Map;

@Component
public class HorseMapper {
  private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  public HorseMapper() {
  }

  /**
   * Convert a horse entity object to a {@link HorseListDto}.
   * The given map of owners needs to contain the owner of {@code horse}.
   *
   * @param horse  the horse to convert
   * @param owners a map of horse owners by their id, which needs to contain the owner referenced by {@code horse}
   * @return the converted {@link HorseListDto}
   */
  public HorseListDto entityToListDto(Horse horse, Map<Long, OwnerDto> owners) {
    LOG.trace("entityToDto({})", horse);
    if (horse == null) {
      return null;
    }

    return new HorseListDto(
        horse.getId(),
        horse.getName(),
        horse.getDescription(),
        horse.getDateOfBirth(),
        horse.getSex(),
        getOwner(horse, owners)
    );
  }

  /**
   * Convert a horse entity object to a {@link HorseListDto}.
   * The given map of owners needs to contain the owner of {@code horse}.
   *
   * @param horse  the horse to convert
   * @param owners a map of horse owners by their id, which needs to contain the owner referenced by {@code horse}
   * @return the converted {@link HorseListDto}
   */
  public HorseDetailDto entityToDetailDto(
      Horse horse,
      Map<Long, OwnerDto> owners,
      Map<Long, HorseDetailDto> parents) {
    LOG.trace("entityToDto({})", horse);
    if (horse == null) {
      return null;
    }

    return new HorseDetailDto(
        horse.getId(),
        horse.getName(),
        horse.getDescription(),
        horse.getDateOfBirth(),
        horse.getSex(),
        getOwner(horse, owners),
        getMother(horse, parents),
        getFather(horse, parents)
    );
  }

  private OwnerDto getOwner(Horse horse, Map<Long, OwnerDto> owners) {
    OwnerDto owner = null;
    var ownerId = horse.getOwnerId();
    if (ownerId != null) {
      if (!owners.containsKey(ownerId)) {
        throw new FatalException("Given owner map does not contain owner of this Horse (%d)".formatted(horse.getId()));
      }
      owner = owners.get(ownerId);
    }
    return owner;
  }

  private HorseDetailDto getFather(Horse horse, Map<Long, HorseDetailDto> parents) {
    HorseDetailDto father = null;
    var fatherId = horse.getFatherId();
    if (fatherId != null && parents != null) {
      if (!parents.containsKey(fatherId)) {
        throw new FatalException("Given owner map does not contain owner of this Horse (%d)".formatted(horse.getId()));
      }
      father = parents.get(fatherId);
    }
    return father;
  }

  private HorseDetailDto getMother(Horse horse, Map<Long, HorseDetailDto> parents) {
    HorseDetailDto mother = null;
    var motherId = horse.getMotherId();
    if (motherId != null && parents != null) {
      if (!parents.containsKey(motherId)) {
        throw new FatalException("Given owner map does not contain owner of this Horse (%d)".formatted(horse.getId()));
      }
      mother = parents.get(motherId);
    }
    return mother;
  }

  public HorseTreeDto entityToTreeDto(Horse h, Collection<Horse> horses, int generation) {
    LOG.trace("entityToTreeDto({})", horses);

    if (horses == null || h == null) {
      return null;
    }

    var mother = (h.getMotherId() == null) ? null : horses.stream().filter(horse -> horse.getId() == h.getMotherId()).findFirst().get();
    var father = (h.getFatherId() == null) ? null : horses.stream().filter(horse -> horse.getId() == h.getFatherId()).findFirst().get();

    return new HorseTreeDto(
        h.getId(),
        h.getName(),
        h.getDateOfBirth(),
        h.getSex(),
        generation,
        entityToTreeDto(mother, horses, generation + 1),
        entityToTreeDto(father, horses, generation + 1)
    );
  }
}
