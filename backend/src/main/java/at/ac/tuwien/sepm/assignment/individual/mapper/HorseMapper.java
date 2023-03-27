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
import java.util.HashMap;
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
  public HorseDetailDto entityToDetailDto(Horse horse, Map<Long, OwnerDto> owners, Map<Long, HorseDetailDto> parents) {
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
        getParent(horse.getMotherId(), parents),
        getParent(horse.getFatherId(), parents)
    );
  }

  /**
   * Convert a horse entity object to a {@link HorseTreeDto}.
   * The given map of horses needs to be initialized and if existing the mother/father of
   * {@code horse}. The {@code generation} must be greater than 0.
   *
   * @param horse         the horse to convert
   * @param horses        a map of horse parents by their id, which needs to contain the parents referenced by {@code horse}
   * @param generation    the current generation of the horse in this tree
   * @param maxGeneration the maximal generation which should be loaded, to avoid loading more than necessary (two generation have same ancestor)
   * @return the converted {@link HorseTreeDto}
   */
  public HorseTreeDto entityToTreeDto(Horse horse, Map<Long, Horse> horses, int generation, int maxGeneration) {
    LOG.trace("entityToTreeDto({})", horses);

    if (horse == null || generation > maxGeneration) {
      return null;
    }

    if (horses == null) {
      horses = new HashMap<>();
    }

    return new HorseTreeDto(
        horse.getId(),
        horse.getName(),
        horse.getDateOfBirth(),
        horse.getSex(),
        entityToTreeDto(horses.get(horse.getMotherId()), horses, generation + 1, maxGeneration),
        entityToTreeDto(horses.get(horse.getFatherId()), horses, generation + 1, maxGeneration)
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

  private HorseDetailDto getParent(Long parentId, Map<Long, HorseDetailDto> parents) {
    LOG.trace("getParent({}, {})", parentId, parents);

    HorseDetailDto parent = null;
    if (parentId != null) {
      if (!parents.containsKey(parentId)) {
        throw new FatalException("Given parent map does not contain parent (%d) of this Horse".formatted(parentId));
      }
      parent = parents.get(parentId);
    }
    return parent;
  }
}
