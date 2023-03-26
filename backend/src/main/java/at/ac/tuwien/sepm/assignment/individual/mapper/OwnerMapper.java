package at.ac.tuwien.sepm.assignment.individual.mapper;

import at.ac.tuwien.sepm.assignment.individual.dto.HorseListDto;
import at.ac.tuwien.sepm.assignment.individual.dto.OwnerDto;
import at.ac.tuwien.sepm.assignment.individual.entity.Owner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.lang.invoke.MethodHandles;

@Component
public class OwnerMapper {
  private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  /**
   * Convert an owner entity object to a {@link OwnerDto}.
   *
   * @param owner the owner to convert
   * @return the converted {@link HorseListDto}
   */
  public OwnerDto entityToDto(Owner owner) {
    LOG.trace("entityToDto({})", owner);

    if (owner == null) {
      return null;
    }

    return new OwnerDto(
        owner.getId(),
        owner.getFirstName(),
        owner.getLastName(),
        owner.getEmail());
  }
}
