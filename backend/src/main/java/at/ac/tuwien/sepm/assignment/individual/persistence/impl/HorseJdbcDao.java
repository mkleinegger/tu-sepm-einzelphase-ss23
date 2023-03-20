package at.ac.tuwien.sepm.assignment.individual.persistence.impl;

import at.ac.tuwien.sepm.assignment.individual.dto.HorseCreateDto;
import at.ac.tuwien.sepm.assignment.individual.dto.HorseDetailDto;
import at.ac.tuwien.sepm.assignment.individual.dto.HorseSearchDto;
import at.ac.tuwien.sepm.assignment.individual.entity.Horse;
import at.ac.tuwien.sepm.assignment.individual.exception.FatalException;
import at.ac.tuwien.sepm.assignment.individual.exception.NotFoundException;
import at.ac.tuwien.sepm.assignment.individual.persistence.HorseDao;
import at.ac.tuwien.sepm.assignment.individual.type.Sex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;

import java.lang.invoke.MethodHandles;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Repository
public class HorseJdbcDao implements HorseDao {
  private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private static final String TABLE_NAME = "horse";
  private static final String SQL_SELECT_ALL = "SELECT * FROM " + TABLE_NAME;
  private static final String SQL_SELECT_BY_ID = "SELECT * FROM " + TABLE_NAME + " WHERE id = ?";
  private static final String SQL_CREATE =
      "INSERT INTO " + TABLE_NAME + " (name, description, date_of_birth, sex, owner_id, mother_id, father_id) VALUES (?, ?, ?, ?, ?, ?, ?)";
  private static final String SQL_UPDATE = "UPDATE " + TABLE_NAME
      + " SET name = ?"
      + "  , description = ?"
      + "  , date_of_birth = ?"
      + "  , sex = ?"
      + "  , owner_id = ?"
      + "  , mother_id = ?"
      + "  , father_id = ?"
      + " WHERE id = ?";

  private static final String SQL_DELETE = "DELETE FROM " + TABLE_NAME + " WHERE id = ?";
  private static final String SQL_SELECT_WHERE = " WHERE UPPER(name) like UPPER('%'||COALESCE(?, '')||'%')";
  private static final String SQL_SELECT_SEARCH_DESCRIPTION_CLAUSE = " AND UPPER(description) like UPPER('%'||COALESCE(?, '')||'%')";
  private static final String SQL_SELECT_SEARCH_DATE_CLAUSE = " AND date_of_birth > ?";
  private static final String SQL_SELECT_SEARCH_SEX_CLAUSE = " AND UPPER(sex) like UPPER(COALESCE(?, '%')) ";
  private static final String SQL_SELECT_SEARCH_OWNER_CLAUSE = " AND date > ?";

  private static final String SQL_SELECT_SEARCH_LIMIT_CLAUSE = " LIMIT ?";


  private final JdbcTemplate jdbcTemplate;

  public HorseJdbcDao(JdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = jdbcTemplate;
  }

  @Override
  public List<Horse> getAll() {
    LOG.trace("getAll()");
    return jdbcTemplate.query(SQL_SELECT_ALL, this::mapRow);
  }

  @Override
  public Horse getById(long id) throws NotFoundException {
    LOG.trace("getById({})", id);
    List<Horse> horses;
    horses = jdbcTemplate.query(SQL_SELECT_BY_ID, this::mapRow, id);

    if (horses.isEmpty()) {
      throw new NotFoundException("No horse with ID %d found".formatted(id));
    }
    if (horses.size() > 1) {
      // This should never happen!!
      throw new FatalException("Too many horses with ID %d found".formatted(id));
    }

    return horses.get(0);
  }

  @Override
  public Horse update(HorseDetailDto horse) throws NotFoundException {
    LOG.trace("update({})", horse);
    int updated = jdbcTemplate.update(SQL_UPDATE,
        horse.name(),
        horse.description(),
        horse.dateOfBirth(),
        horse.sex().toString(),
        horse.ownerId(),
        horse.motherId(),
        horse.fatherId(),
        horse.id());

    if (updated == 0) {
      throw new NotFoundException("Could not update horse with ID " + horse.id() + ", because it does not exist");
    }

    return new Horse()
        .setId(horse.id())
        .setName(horse.name())
        .setDescription(horse.description())
        .setDateOfBirth(horse.dateOfBirth())
        .setSex(horse.sex())
        .setOwnerId(horse.ownerId())
        .setMotherId(horse.motherId())
        .setFatherId(horse.fatherId());
  }

  @Override
  public Horse create(HorseCreateDto newHorse) {
    LOG.trace("create({})", newHorse);

    GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
    jdbcTemplate.update(con -> {
      PreparedStatement stmt = con.prepareStatement(SQL_CREATE, Statement.RETURN_GENERATED_KEYS);
      stmt.setString(1, newHorse.name());
      stmt.setString(2, newHorse.description());
      stmt.setString(3, String.valueOf(newHorse.dateOfBirth()));
      stmt.setString(4, newHorse.sex().toString());
      if (newHorse.ownerId() != null) {
        stmt.setLong(5, newHorse.ownerId());
      } else {
        stmt.setNull(5, Types.BIGINT);
      }
      if (newHorse.motherId() != null) {
        stmt.setLong(6, newHorse.motherId());
      } else {
        stmt.setNull(6, Types.BIGINT);
      }
      if (newHorse.fatherId() != null) {
        stmt.setLong(7, newHorse.fatherId());
      } else {
        stmt.setNull(7, Types.BIGINT);
      }

      return stmt;
    }, keyHolder);

    Number key = keyHolder.getKey();
    if (key == null) {
      // This should never happen. If it does, something is wrong with the DB or the way the prepared statement is set up.
      throw new FatalException("Could not extract key for newly created horse. There is probably a programming error…");
    }

    return new Horse()
        .setId(key.longValue())
        .setName(newHorse.name())
        .setDescription(newHorse.description())
        .setDateOfBirth(newHorse.dateOfBirth())
        .setSex(newHorse.sex())
        .setOwnerId(newHorse.ownerId())
        .setMotherId(newHorse.motherId())
        .setFatherId(newHorse.fatherId())
        ;
  }

  @Override
  public Horse delete(long id) throws NotFoundException {
    Horse deletedHorse = getById(id);
    int affectedRows = jdbcTemplate.update(connection -> {
      PreparedStatement stmt = connection.prepareStatement(SQL_DELETE, Statement.RETURN_GENERATED_KEYS);
      stmt.setLong(1, id);
      return stmt;
    });

    if (affectedRows == 0) {
      throw new NotFoundException("No horse with ID %d deleted".formatted(id));
    }
    return deletedHorse;
  }

  @Override
  public Collection<Horse> search(HorseSearchDto searchParameters) {
    LOG.trace("search({})", searchParameters);
    var query = SQL_SELECT_ALL;
    var params = new ArrayList<>();

    if (searchParameters != null) {
      query += SQL_SELECT_WHERE;
      params.add(searchParameters.name());

      //name
      var description = searchParameters.description();
      if (description != null) {
        query += SQL_SELECT_SEARCH_DESCRIPTION_CLAUSE;
        params.add(description);
      }

      var bornBefore = searchParameters.bornBefore();
      if (bornBefore != null) {
        query += SQL_SELECT_SEARCH_DATE_CLAUSE;
        params.add(bornBefore);
      }

      var sex = searchParameters.sex();
      if (sex != null) {
        query += SQL_SELECT_SEARCH_SEX_CLAUSE;
        params.add(sex.toString());
      }

      // limit
      var maxAmount = searchParameters.limit();
      if (maxAmount != null) {
        query += SQL_SELECT_SEARCH_LIMIT_CLAUSE;
        params.add(maxAmount);
      }
    }
    return jdbcTemplate.query(query, this::mapRow, params.toArray());
  }

  private Horse mapRow(ResultSet result, int rownum) throws SQLException {
    return new Horse()
        .setId(result.getLong("id"))
        .setName(result.getString("name"))
        .setDescription(result.getString("description"))
        .setDateOfBirth(result.getDate("date_of_birth").toLocalDate())
        .setSex(Sex.valueOf(result.getString("sex")))
        .setOwnerId(result.getObject("owner_id", Long.class))
        .setMotherId(result.getObject("mother_id", Long.class))
        .setFatherId(result.getObject("father_id", Long.class));

  }
}
