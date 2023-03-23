package at.ac.tuwien.sepm.assignment.individual.rest;

import at.ac.tuwien.sepm.assignment.individual.dto.HorseCreateDto;
import at.ac.tuwien.sepm.assignment.individual.dto.HorseDetailDto;
import at.ac.tuwien.sepm.assignment.individual.dto.HorseListDto;
import at.ac.tuwien.sepm.assignment.individual.dto.HorseSearchDto;
import at.ac.tuwien.sepm.assignment.individual.dto.HorseTreeDto;
import at.ac.tuwien.sepm.assignment.individual.exception.ConflictException;
import at.ac.tuwien.sepm.assignment.individual.exception.NotFoundException;
import at.ac.tuwien.sepm.assignment.individual.exception.ValidationException;
import at.ac.tuwien.sepm.assignment.individual.service.HorseService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.lang.invoke.MethodHandles;
import java.util.stream.Stream;

@RestController
@RequestMapping(path = HorseEndpoint.BASE_PATH)
public class HorseEndpoint {
  private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
  static final String BASE_PATH = "/horses";
  private final HorseService service;

  public HorseEndpoint(HorseService service) {
    this.service = service;
  }

  @GetMapping
  public Stream<HorseListDto> searchHorses(HorseSearchDto searchParameters) {
    LOG.info("GET " + BASE_PATH);
    LOG.debug("request parameters: {}", searchParameters);

    return service.allHorses(searchParameters); //check persistence exception?
  }

  @GetMapping("{id}")
  public HorseDetailDto getById(@PathVariable long id) {
    LOG.info("GET " + BASE_PATH + "/{}", id);

    try {
      return service.getById(id);
    } catch (NotFoundException e) {
      HttpStatus status = HttpStatus.NOT_FOUND;
      logClientError(status, "Horse to get details of not found", e);
      throw new ResponseStatusException(status, e.getMessage(), e);
    }
  }

  @GetMapping("{id}/generations")
  public HorseTreeDto getGenerationsFor(@PathVariable long id) {
    LOG.info("GET " + BASE_PATH + "/{}/generations", id);
    try {
      return service.getGenerationsAsTree(id, 3);
    } catch (NotFoundException e) {
      HttpStatus status = HttpStatus.NOT_FOUND;
      logClientError(status, "Horse to get details of not found", e);
      throw new ResponseStatusException(status, e.getMessage(), e);
    }
  }

  @PutMapping("{id}")
  public HorseDetailDto update(@PathVariable long id, @RequestBody HorseDetailDto toUpdate) throws ValidationException, ConflictException {
    LOG.info("PUT " + BASE_PATH + "/{}", id);
    LOG.debug("Body of request:\n{}", toUpdate);

    try {
      return service.update(toUpdate.withId(id));
    } catch (NotFoundException e) {
      HttpStatus status = HttpStatus.NOT_FOUND;
      logClientError(status, "Horse to update not found", e);
      throw new ResponseStatusException(status, e.getMessage(), e);
    } catch (ValidationException e) {
      HttpStatus status = HttpStatus.UNPROCESSABLE_ENTITY;
      logClientError(HttpStatus.UNPROCESSABLE_ENTITY, "Horse to update is not valid found", e);
      throw new ResponseStatusException(status, e.getMessage(), e);
    } catch (ConflictException e) {
      HttpStatus status = HttpStatus.CONFLICT;
      logClientError(status, "Horse to update contains conflicts with other data", e);
      throw new ResponseStatusException(status, e.getMessage(), e);
    }
  }

  @PostMapping
  @ResponseStatus(code = HttpStatus.CREATED, reason = "CREATED")
  public HorseDetailDto create(@RequestBody HorseCreateDto toCreate) throws ValidationException, ConflictException {
    LOG.info("POST " + BASE_PATH);
    LOG.debug("Body of request:\n{}", toCreate);

    try {
      return service.create(toCreate);
    } catch (ValidationException e) {
      HttpStatus status = HttpStatus.UNPROCESSABLE_ENTITY;
      logClientError(HttpStatus.UNPROCESSABLE_ENTITY, "Horse to create is not valid found", e);
      throw new ResponseStatusException(status, e.getMessage(), e);
    } catch (ConflictException e) {
      HttpStatus status = HttpStatus.CONFLICT;
      logClientError(status, "Horse to update contains conflicts with other data", e);
      throw new ResponseStatusException(status, e.getMessage(), e);
    }
  }

  @DeleteMapping("{id}")
  @ResponseStatus(code = HttpStatus.NO_CONTENT, reason = "NO_CONTENT")
  public void delete(@PathVariable long id) throws NotFoundException {
    LOG.info("DELETE " + BASE_PATH + "/{}", id);

    try {
      service.delete(id);
    } catch (NotFoundException e) {
      HttpStatus status = HttpStatus.NOT_FOUND;
      logClientError(status, "Horse to update not found", e);
      throw new ResponseStatusException(status, e.getMessage(), e);
    }
  }

  private void logClientError(HttpStatus status, String message, Exception e) {
    LOG.warn("{} {}: {}: {}", status.value(), message, e.getClass().getSimpleName(), e.getMessage());
  }
}
