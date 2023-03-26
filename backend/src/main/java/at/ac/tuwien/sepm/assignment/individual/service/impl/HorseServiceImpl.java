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
import java.util.Arrays;
import java.util.Collection;
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
  public Stream<HorseListDto> search(HorseSearchDto searchParameters) {
    LOG.trace("allHorses({})", searchParameters);

    var horses = dao.search(searchParameters);
    var ownerIds = horses
        .stream()
        .map(Horse::getOwnerId)
        .filter(Objects::nonNull)
        .collect(Collectors.toUnmodifiableSet());

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
        horseMapForIds(Arrays.asList(updatedHorse.getMotherId(), updatedHorse.getFatherId())));
  }


  @Override
  public HorseDetailDto getById(long id) throws NotFoundException {
    LOG.trace("details({})", id);

    Horse horse = dao.getById(id);
    return mapper.entityToDetailDto(horse,
        ownerMapForSingleId(horse.getOwnerId()),
        horseMapForIds(Arrays.asList(horse.getMotherId(), horse.getFatherId())));
  }

  @Override
  public HorseDetailDto create(HorseCreateDto newHorse) throws ValidationException, ConflictException {
    LOG.trace("create({})", newHorse);

    validator.validateForCreate(newHorse);
    return mapper.entityToDetailDto(dao.create(newHorse),
        ownerMapForSingleId(newHorse.ownerId()),
        horseMapForIds(Arrays.asList(newHorse.motherId(), newHorse.fatherId())));
  }

  @Override
  public void delete(long id) throws NotFoundException {
    LOG.trace("delete({})", id);

    dao.delete(id);
  }

  @Override
  public HorseTreeDto getGenerationsAsTree(long id, int numberOfGenerations) throws NotFoundException, ValidationException {
    LOG.trace("getGenerationsAsTree({}{})", id, numberOfGenerations);

    if (numberOfGenerations < 1) {
      throw new ValidationException("Cannot load family-tree for %d".formatted(id),
          Collections.singletonList("Number of generation for family tree is not valid"));
    }

    var horse = dao.getById(id);  // to check if horse even existing
    var horses = dao.getGenerationsAsTree(horse.getId(), numberOfGenerations);
    return mapper.entityToTreeDto(horses.get(horse.getId()), horses, 1);
  }

  private Map<Long, OwnerDto> ownerMapForSingleId(Long ownerId) {
    try {
      return ownerId == null ? null : Collections.singletonMap(ownerId, ownerService.getById(ownerId));
    } catch (NotFoundException e) {
      throw new FatalException("Owner %d referenced by horse not found".formatted(ownerId));
    }
  }

  private Map<Long, HorseDetailDto> horseMapForIds(Collection<Long> ids) {
    try {
      HashMap<Long, HorseDetailDto> result = new HashMap<>();
      for (Long id : ids) {
        if (id != null) {
          // set owner, father and mother to null, because it should not be loaded
          result.put(id, mapper.entityToDetailDto(dao.getById(id).setOwnerId(null).setFatherId(null).setMotherId(null), null, null));
        }
      }

      return result;
    } catch (NotFoundException e) {
      throw new FatalException("Horse-Parents %s referenced by horse not found".formatted(Arrays.toString(ids.toArray())));
    }
  }
}
