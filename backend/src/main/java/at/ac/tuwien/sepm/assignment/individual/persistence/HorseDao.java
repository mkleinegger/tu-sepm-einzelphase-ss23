package at.ac.tuwien.sepm.assignment.individual.persistence;

import at.ac.tuwien.sepm.assignment.individual.dto.HorseCreateDto;
import at.ac.tuwien.sepm.assignment.individual.dto.HorseDetailDto;
import at.ac.tuwien.sepm.assignment.individual.dto.HorseSearchDto;
import at.ac.tuwien.sepm.assignment.individual.entity.Horse;
import at.ac.tuwien.sepm.assignment.individual.exception.NotFoundException;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Data Access Object for horses.
 * Implements access functionality to the application's persistent data store regarding horses.
 */
public interface HorseDao {
  /**
   * Get all horses stored in the persistent data store.
   *
   * @return a list of all stored horses
   */
  List<Horse> getAll();

  /**
   * Search for horses matching the criteria in {@code searchParameters}.
   * <p>
   * A horse is considered matched, if attributes from {@code searchParameters} match attributes from
   * the entity {@link Horse}. The query is additive, meaning that all attributes are linked with an AND operator.
   * The returned stream of horses never contains more than {@code searchParameters.limit} elements,
   * even if there would be more matches in the persistent data store.
   * </p>
   *
   * @param searchParameters object containing the search parameters to match
   * @return a stream containing horses matching the criteria in {@code searchParameters}
   */
  Collection<Horse> search(HorseSearchDto searchParameters);

  /**
   * Get a horse by its ID from the persistent data store.
   *
   * @param id the ID of the horse to get
   * @return the horse
   * @throws NotFoundException if the Horse with the given ID does not exist in the persistent data store
   */
  Horse getById(long id) throws NotFoundException;

  /**
   * Create a new horse in the persistent data store.
   *
   * @param newHorse the data to create the new horse from
   * @return the newly created horse
   */
  Horse create(HorseCreateDto newHorse);

  /**
   * Update the horse with the ID given in {@code horse}
   * with the data given in {@code horse}
   * in the persistent data store.
   *
   * @param horse the horse to update
   * @return the updated horse
   * @throws NotFoundException if the Horse with the given ID does not exist in the persistent data store
   */
  Horse update(HorseDetailDto horse) throws NotFoundException;

  /**
   * Deletes the horse, with the given ID, from the persistent data store.
   *
   * @param id the ID of the horse to get
   */
  void delete(long id) throws NotFoundException;

  /**
   * Gets the family tree for the specified ID from the persistent data store with a depth
   * of {@code limit}.
   * {@code limit} must be greater than 0
   *
   * @param id    the ID of the horse to get the family-tree
   * @param limit The numbers of generations the dao should load
   * @return a map of horse by their id, which represent the family-tree
   * @throws NotFoundException if the Horse with the given ID does not exist in the persistent data store
   */
  Map<Long, Horse> getGenerationsAsTree(long id, long limit) throws NotFoundException;

  /**
   * Gets all the children of the horse, with the specified id
   *
   * @param id the ID of the horse to get the family-tree
   * @return a stream containing children from the horse with the specified {@code id}
   */
  Collection<Horse> getChildren(long id);
}
