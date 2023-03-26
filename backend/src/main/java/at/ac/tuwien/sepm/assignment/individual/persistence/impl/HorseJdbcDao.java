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
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Repository
public class HorseJdbcDao implements HorseDao {
  private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private static final String TABLE_NAME = "horse";
  private static final String TABLE_NAME_OWNER = "owner";
  private static final String SQL_SELECT_ALL = "SELECT * FROM " + TABLE_NAME;
  private static final String SQL_SELECT_ALL_CHILDREN = "SELECT * FROM " + TABLE_NAME + " WHERE mother_id = ? OR father_id = ?";
  private static final String SQL_SELECT_SEARCH = "SELECT h.* FROM " + TABLE_NAME + " h"
      + " LEFT JOIN " + TABLE_NAME_OWNER + " o ON o.id = h.owner_id"
      + " WHERE (? IS NULL OR UPPER(h.name) like UPPER('%'||COALESCE(?, '')||'%'))"
      + " AND (? IS NULL OR UPPER(h.description) like UPPER('%'||COALESCE(?, '')||'%'))"
      + " AND (? IS NULL OR h.date_of_birth > ?)"
      + " AND (? IS NULL OR h.sex = ?)"
      + " AND (? IS NULL OR UPPER(o.first_name ||' '|| o.last_name) like UPPER('%'||COALESCE(?, '')||'%'))";
  private static final String SQL_SELECT_SEARCH_LIMIT_CLAUSE = " LIMIT ?";
  private static final String SQL_SELECT_GENERATION =
      "WITH RECURSIVE ancestor(id, name, description, date_of_birth, sex, owner_id, mother_id, father_id, generation) AS ("
          + " SELECT *, 1 as generation FROM horse where Id = ? UNION ALL SELECT horse.*, (ancestor.generation + 1) FROM horse, ancestor"
          + " WHERE (ancestor.mother_id = horse.id OR ancestor.father_id = horse.id) AND ancestor.generation < ?) SELECT * FROM ancestor";

  private static final String SQL_SELECT_BY_ID = "SELECT * FROM " + TABLE_NAME + " WHERE id = ?";
  private static final String SQL_CREATE =
      "INSERT INTO " + TABLE_NAME + " (name, description, date_of_birth, sex, owner_id, mother_id, father_id) VALUES (?, ?, ?, ?, ?, ?, ?)";
  private static final String SQL_UPDATE =
      "UPDATE " + TABLE_NAME + " SET name = ? , description = ?, date_of_birth = ?, sex = ?, owner_id = ?, mother_id = ?"
          + "  , father_id = ? WHERE id = ?";
  private static final String SQL_DELETE = "DELETE FROM " + TABLE_NAME + " WHERE id = ?";
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

    List<Horse> horses = jdbcTemplate.query(SQL_SELECT_BY_ID, this::mapRow, id);

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
      throw new NotFoundException("Could not update horse with ID %d, because it does not exist".formatted(horse.id()));
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
      stmt.setDate(3, Date.valueOf(newHorse.dateOfBirth()));
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
        .setFatherId(newHorse.fatherId());
  }

  @Override
  public void delete(long id) throws NotFoundException {
    LOG.trace("delete({})", id);

    int deleted = jdbcTemplate.update(SQL_DELETE, id);

    if (deleted == 0) {
      throw new NotFoundException("No horse with ID %d deleted".formatted(id));
    }
  }

  @Override
  public Collection<Horse> search(HorseSearchDto searchParameters) {
    LOG.trace("search({})", searchParameters);

    var query = SQL_SELECT_SEARCH;
    var params = new ArrayList<>();
    params.add(searchParameters.name());
    params.add(searchParameters.name());
    params.add(searchParameters.description());
    params.add(searchParameters.description());
    params.add(searchParameters.bornBefore());
    params.add(searchParameters.bornBefore());
    params.add(searchParameters.sex());
    params.add(searchParameters.sex() == null ? null : searchParameters.sex().toString());
    params.add(searchParameters.ownerName());
    params.add(searchParameters.ownerName());

    // limit
    var maxAmount = searchParameters.limit();
    if (maxAmount != null) {
      query += SQL_SELECT_SEARCH_LIMIT_CLAUSE;
      params.add(maxAmount);
    }

    return jdbcTemplate.query(query, this::mapRow, params.toArray());
  }

  @Override
  public Map<Long, Horse> getGenerationsAsTree(long id, long limit) throws NotFoundException {
    LOG.trace("getGenerationsAsTree({}, {})", id, limit);

    List<Horse> horses = jdbcTemplate.query(SQL_SELECT_GENERATION, this::mapRow, id, limit);
    if (horses.isEmpty()) {
      throw new NotFoundException("Could not return family-tree for horse with ID %d, because it does not exist".formatted(id));
    }

    return horses.stream().collect(Collectors.toMap(Horse::getId, Function.identity()));
  }

  @Override
  public Collection<Horse> getChildren(long id) {
    LOG.trace("getChildren({})", id);

    return jdbcTemplate.query(SQL_SELECT_ALL_CHILDREN, this::mapRow, id, id);
  }

  private Horse mapRow(ResultSet result, int rownum) throws SQLException {
    LOG.trace("mapRow({}, {})", result, rownum);

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
