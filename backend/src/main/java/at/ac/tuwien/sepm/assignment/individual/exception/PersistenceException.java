package at.ac.tuwien.sepm.assignment.individual.exception;

/**
 * Exception used to signal errors concerning the database .
 */
public class PersistenceException extends RuntimeException {

  public PersistenceException(String message) {
    super(message);
  }

  public PersistenceException(Throwable cause) {
    super(cause);
  }

  public PersistenceException(String message, Throwable cause) {
    super(message, cause);
  }
}
