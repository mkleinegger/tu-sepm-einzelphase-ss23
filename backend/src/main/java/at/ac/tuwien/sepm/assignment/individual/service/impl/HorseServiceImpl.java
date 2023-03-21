package at.ac.tuwien.sepm.assignment.individual.service.impl;

import at.ac.tuwien.sepm.assignment.individual.dto.HorseCreateDto;
import at.ac.tuwien.sepm.assignment.individual.dto.HorseDetailDto;
import at.ac.tuwien.sepm.assignment.individual.dto.HorseListDto;
import at.ac.tuwien.sepm.assignment.individual.dto.HorseSearchDto;
import at.ac.tuwien.sepm.assignment.individual.dto.HorseTreeDto;
import at.ac.tuwien.sepm.assignment.individual.dto.OwnerDto;
import at.ac.tuwien.sepm.assignment.individual.entity.Horse;
import at.ac.tuwien.sepm.assignment.individual.exception.ConflictException;
import at.ac.tuwien.sepm.assignment.individual.exception.FatalException;
import at.ac.tuwien.sepm.assignment.individual.exception.NotFoundException;
import at.ac.tuwien.sepm.assignment.individual.exception.ValidationException;
import at.ac.tuwien.sepm.assignment.individual.mapper.HorseMapper;
import at.ac.tuwien.sepm.assignment.individual.persistence.HorseDao;
import at.ac.tuwien.sepm.assignment.individual.service.HorseService;
import at.ac.tuwien.sepm.assignment.individual.service.OwnerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.lang.invoke.MethodHandles;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class HorseServiceImpl implements HorseService {
  private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
  private final HorseDao dao;
  private final HorseMapper mapper;
  private final HorseValidator validator;
  private final OwnerService ownerService;

  public HorseServiceImpl(HorseDao dao, HorseMapper mapper, HorseValidator validator, OwnerService ownerService) {
    this.dao = dao;
    this.mapper = mapper;
    this.validator = validator;
    this.ownerService = ownerService;
  }

  @Override
  public Stream<HorseListDto> allHorses(HorseSearchDto searchParameters) {
    LOG.trace("allHorses()");
    var horses = dao.search(searchParameters);
    var ownerIds = horses.stream().map(Horse::getOwnerId).filter(Objects::nonNull).collect(Collectors.toUnmodifiableSet());
    Map<Long, OwnerDto> ownerMap;

    try {
      ownerMap = ownerService.getAllById(ownerIds);
    } catch (NotFoundException e) {
      throw new FatalException("Horse, that is already persisted, refers to non-existing owner", e);
    }

    return horses.stream().map(horse -> mapper.entityToListDto(horse, ownerMap));
  }


  @Override
  public HorseDetailDto update(HorseDetailDto horse) throws NotFoundException, ValidationException, ConflictException {
    LOG.trace("update({})", horse);
    validator.validateForUpdate(horse);
    var updatedHorse = dao.update(horse);

    return mapper.entityToDetailDto(updatedHorse,
        ownerMapForSingleId(updatedHorse.getOwnerId()),
        horseMapForIds(updatedHorse.getMotherId(), updatedHorse.getFatherId()));
  }


  @Override
  public HorseDetailDto getById(long id) throws NotFoundException {
    LOG.trace("details({})", id);
    Horse horse = dao.getById(id);
    return mapper.entityToDetailDto(horse,
        ownerMapForSingleId(horse.getOwnerId()),
        horseMapForIds(horse.getMotherId(), horse.getFatherId()));
  }

  @Override
  public HorseDetailDto create(HorseCreateDto newHorse) throws ValidationException, ConflictException {
    LOG.trace("create({})", newHorse);

    validator.validateForCreate(newHorse);
    return mapper.entityToDetailDto(dao.create(newHorse),
        ownerMapForSingleId(newHorse.ownerId()),
        horseMapForIds(newHorse.motherId(), newHorse.fatherId()));
  }

  @Override
  public HorseDetailDto delete(long id) throws NotFoundException {
    LOG.trace("delete({})", id);
    Horse horse = dao.delete(id);
    return mapper.entityToDetailDto(horse,
        ownerMapForSingleId(horse.getOwnerId()),
        horseMapForIds(horse.getMotherId(), horse.getFatherId()));
  }

  @Override
  public HorseTreeDto getGenerationsAsTree(long id, int limit) throws NotFoundException {
    LOG.trace("delete({}{})", id, limit);
    var horses = dao.getGenerationsAsTree(id, limit);
    return mapper.entityToTreeDto(horses.stream().filter(horse -> horse.getId() == id).findFirst().get(), horses, 1);
    // return horses.stream().map(horse -> mapper.entityToTreeDto(horse));
  }

  private Map<Long, OwnerDto> ownerMapForSingleId(Long ownerId) {
    try {
      return ownerId == null ? null : Collections.singletonMap(ownerId, ownerService.getById(ownerId));
    } catch (NotFoundException e) {
      throw new FatalException("Owner %d referenced by horse not found".formatted(ownerId));
    }
  }

  private Map<Long, HorseDetailDto> horseMapForIds(Long... ids) {
    try {
      if (ids.length == 0) {
        return null;
      }

      HashMap<Long, HorseDetailDto> result = new HashMap<>();
      for (Long id : ids) {
        if (id != null) {
          Horse h = dao.getById(id);
          result.put(id, mapper.entityToDetailDto(h,
              ownerMapForSingleId(h.getOwnerId()), null));
        }
      }

      return result;
    } catch (NotFoundException e) {
      throw new FatalException("Owner %d referenced by horse not found".formatted(ids));
    }
  }

}
