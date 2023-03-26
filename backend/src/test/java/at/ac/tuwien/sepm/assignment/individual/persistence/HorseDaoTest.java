package at.ac.tuwien.sepm.assignment.individual.persistence;

import at.ac.tuwien.sepm.assignment.individual.dto.HorseCreateDto;
import at.ac.tuwien.sepm.assignment.individual.dto.HorseDetailDto;
import at.ac.tuwien.sepm.assignment.individual.dto.OwnerDto;
import at.ac.tuwien.sepm.assignment.individual.entity.Horse;
import at.ac.tuwien.sepm.assignment.individual.exception.NotFoundException;
import at.ac.tuwien.sepm.assignment.individual.type.Sex;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

@ActiveProfiles({"test", "datagen"}) // enable "test" spring profile during test execution in order to pick up configuration from application-test.yml
@SpringBootTest
public class HorseDaoTest {

  @Autowired
  HorseDao horseDao;
  @Autowired
  PlatformTransactionManager txm;
  TransactionStatus txstatus;

  @BeforeEach
  public void setupDBTransaction() {
    DefaultTransactionDefinition def = new DefaultTransactionDefinition();
    def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
    txstatus = txm.getTransaction(def);
    assumeTrue(txstatus.isNewTransaction());
    txstatus.setRollbackOnly();
  }

  @AfterEach
  public void tearDownDBData() {
    txm.rollback(txstatus);
  }

  @Test
  @DisplayName("Get all horses via DAO")
  public void getAllReturnsAllStoredHorses() {
    List<Horse> horses = horseDao.getAll();
    assertThat(horses.size()).isEqualTo(10);
    assertThat(horses)
        .extracting(Horse::getId, Horse::getName)
        .contains(tuple(-1L, "Thunder"),
            tuple(-2L, "Daisy"),
            tuple(-3L, "Spirit"),
            tuple(-4L, "Bella"),
            tuple(-5L, "Max"),
            tuple(-6L, "Luna"),
            tuple(-7L, "Romeo"),
            tuple(-8L, "Sophie"),
            tuple(-9L, "Apollo"),
            tuple(-10L, "Misty"));
  }

  @Test
  @DisplayName("Update an existing Horse via DAO")
  public void updateExistingRecord() {
    HorseDetailDto horseToUpdate = new HorseDetailDto(-2L, "Daisy", "Updated Description", LocalDate.of(2017, 3, 10), Sex.FEMALE, null, null, null);

    assertDoesNotThrow(() -> {
      Horse horse = horseDao.update(horseToUpdate);

      assertThat(horse).isNotNull();
      assertThat(horse.getId()).isEqualTo(horseToUpdate.id());
      assertThat(horse.getName()).isEqualTo(horseToUpdate.name());
      assertThat(horse.getDescription()).isEqualTo(horseToUpdate.description());
      assertThat(horse.getDateOfBirth()).isEqualTo(horseToUpdate.dateOfBirth());
      assertThat(horse.getSex()).isEqualTo(horseToUpdate.sex());
      assertThat(horse.getOwnerId()).isEqualTo(horseToUpdate.ownerId());
      assertThat(horse.getMotherId()).isEqualTo(horseToUpdate.motherId());
      assertThat(horse.getFatherId()).isEqualTo(horseToUpdate.fatherId());
    });
  }

  @Test
  @DisplayName("Get an non-existing horse via DAO")
  public void getById() {
    assertThrows(NotFoundException.class, () -> horseDao.getById(-11L));
  }

  @Test
  @DisplayName("Create a new Horse via DAO")
  public void createAValidHorse() {
    HorseCreateDto horseToCreate =
        new HorseCreateDto("Created through testcase", null, LocalDate.of(2020, 1, 21), Sex.MALE, new OwnerDto(-2, null, null, null), null, null);

    assertDoesNotThrow(() -> {
      Horse horse = horseDao.create(horseToCreate);

      assertThat(horse).isNotNull();
      assertThat(horse.getId()).isEqualTo(1L);
      assertThat(horse.getName()).isEqualTo(horseToCreate.name());
      assertThat(horse.getDescription()).isEqualTo(horseToCreate.description());
      assertThat(horse.getDateOfBirth()).isEqualTo(horseToCreate.dateOfBirth());
      assertThat(horse.getSex()).isEqualTo(horseToCreate.sex());
      assertThat(horse.getOwnerId()).isEqualTo(horseToCreate.ownerId());
      assertThat(horse.getMotherId()).isEqualTo(horseToCreate.motherId());
      assertThat(horse.getFatherId()).isEqualTo(horseToCreate.fatherId());

      List<Horse> horses = horseDao.getAll();
      assertThat(horses.size()).isEqualTo(11);
      assertThat(horses)
          .extracting(Horse::getId, Horse::getName)
          .contains(tuple(1L, horseToCreate.name()),
              tuple(-1L, "Thunder"),
              tuple(-2L, "Daisy"),
              tuple(-3L, "Spirit"),
              tuple(-4L, "Bella"),
              tuple(-5L, "Max"),
              tuple(-6L, "Luna"),
              tuple(-7L, "Romeo"),
              tuple(-8L, "Sophie"),
              tuple(-9L, "Apollo"),
              tuple(-10L, "Misty"));
    });
  }

  @Test
  @DisplayName("Delete an existing Horse via DAO")
  public void deleteValidHorse() {
    assertDoesNotThrow(() -> {
      horseDao.delete(-1L);

      List<Horse> horses = horseDao.getAll();
      assertThat(horses.size()).isEqualTo(9);
      assertThat(horses)
          .extracting(Horse::getId)
          .doesNotContain(-1L)
          .contains(-2L, -3L, -4L, -5L, -6L, -7L, -8L, -9L, -10L);
    });
  }
}
