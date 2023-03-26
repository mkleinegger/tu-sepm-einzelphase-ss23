package at.ac.tuwien.sepm.assignment.individual.service;

import at.ac.tuwien.sepm.assignment.individual.dto.HorseCreateDto;
import at.ac.tuwien.sepm.assignment.individual.dto.HorseDetailDto;
import at.ac.tuwien.sepm.assignment.individual.dto.HorseListDto;
import at.ac.tuwien.sepm.assignment.individual.dto.HorseSearchDto;
import at.ac.tuwien.sepm.assignment.individual.dto.HorseTreeDto;
import at.ac.tuwien.sepm.assignment.individual.exception.ConflictException;
import at.ac.tuwien.sepm.assignment.individual.exception.NotFoundException;
import at.ac.tuwien.sepm.assignment.individual.exception.ValidationException;

import java.util.stream.Stream;

/**
 * Service for working with horses.
 */
public interface HorseService {

  /**
   * Search for horses matching the criteria in {@code searchParameters}.
   * <p>
   * A horses is considered matched, if any properties match properties from {@code searchParameters}
   * The returned stream of horses never contains more than {@code searchParameters.limit} elements,
   * even if there would be more matches in the persistent data store.
   * </p>
   *
   * @param searchParameters object containing the search parameters to match
   * @return a stream containing horses matching the criteria in {@code searchParameters}
   */
  Stream<HorseListDto> search(HorseSearchDto searchParameters);

  /**
   * Get the horse with given ID, with more detail information.
   * This includes the owner of the horse, and its parents.
   * The parents of the parents are not included.
   *
   * @param id the ID of the horse to get
   * @return the horse with ID {@code id}
   * @throws NotFoundException if the horse with the given ID does not exist in the persistent data store
   */
  HorseDetailDto getById(long id) throws NotFoundException;

  /**
   * Updates the horse with the ID given in {@code horse}
   * with the data given in {@code horse} in the persistent data store.
   *
   * @param horse the horse to update
   * @return the updated horse
   * @throws NotFoundException   if the horse with given ID does not exist in the persistent data store
   * @throws ValidationException if the update data given for the horse is in itself incorrect (description too long, no name, …)
   * @throws ConflictException   if the update data given for the horse is in conflict the data currently in the system (owner does not exist, …)
   */
  HorseDetailDto update(HorseDetailDto horse) throws NotFoundException, ValidationException, ConflictException;

  /**
   * Create a new horse in the persistent data store.
   *
   * @param newHorse the data for the new horse
   * @return the horse, that was just newly created in the persistent data store
   * @throws ValidationException if the create data given for the new horse is in itself incorrect (description too long, no name, …)
   * @throws ConflictException   if the create data given for the new horse is in conflict the data currently in the system (owner does not exist, …)
   */
  HorseDetailDto create(HorseCreateDto newHorse) throws ValidationException, ConflictException;

  /**
   * Deletes the horse with given {@code id}.
   *
   * @param id the ID of the horse to get
   * @throws NotFoundException if the horse with the given ID does not exist in the persistent data store
   */
  void delete(long id) throws NotFoundException;

  /**
   * Gets a family tree for the given {@code id} with depth of the specified
   * {@code numberOfGenerations}
   *
   * @param id                  the ID of the horse to get
   * @param numberOfGenerations the number of generations which should be returned
   * @return a family-tree for the specified id with and number of generations
   * @throws NotFoundException   if the horse with the given ID does not exist in the persistent data store
   * @throws ValidationException if the numberOfGenerations is invalid (therefore below 1)
   */
  HorseTreeDto getGenerationsAsTree(long id, int numberOfGenerations) throws NotFoundException, ValidationException;
}
