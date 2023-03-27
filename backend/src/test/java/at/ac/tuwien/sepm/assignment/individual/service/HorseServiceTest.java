package at.ac.tuwien.sepm.assignment.individual.service;

import at.ac.tuwien.sepm.assignment.individual.dto.HorseCreateDto;
import at.ac.tuwien.sepm.assignment.individual.dto.HorseListDto;
import at.ac.tuwien.sepm.assignment.individual.dto.HorseSearchDto;
import at.ac.tuwien.sepm.assignment.individual.exception.ValidationException;
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

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

@ActiveProfiles({"test", "datagen"}) // enable "test" spring profile during test execution in order to pick up configuration from application-test.yml
@SpringBootTest
public class HorseServiceTest {

  @Autowired
  HorseService horseService;
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
  @DisplayName("Get all horses via Service")
  public void getAllReturnsAllStoredHorses() {
    List<HorseListDto> horses = horseService.search(new HorseSearchDto(null, null, null, null, null, null))
        .toList();

    assertThat(horses.size()).isGreaterThanOrEqualTo(10);
    assertThat(horses)
        .map(HorseListDto::id, HorseListDto::sex)
        .contains(tuple(-1L, Sex.MALE))
        .contains(tuple(-2L, Sex.MALE))
        .contains(tuple(-3L, Sex.FEMALE))
        .contains(tuple(-4L, Sex.FEMALE))
        .contains(tuple(-5L, Sex.MALE))
        .contains(tuple(-6L, Sex.FEMALE))
        .contains(tuple(-7L, Sex.MALE))
        .contains(tuple(-8L, Sex.FEMALE))
        .contains(tuple(-9L, Sex.MALE))
        .contains(tuple(-10L, Sex.FEMALE));
  }

  @Test
  @DisplayName("Get all female horses via Service")
  public void testSearchHorses() {
    List<HorseListDto> horses = horseService.search(new HorseSearchDto(null, null, null, Sex.FEMALE, null, null))
        .toList();

    assertThat(horses.size()).isEqualTo(5);
    assertThat(horses)
        .map(HorseListDto::id, HorseListDto::sex)
        .contains(tuple(-3L, Sex.FEMALE))
        .contains(tuple(-4L, Sex.FEMALE))
        .contains(tuple(-6L, Sex.FEMALE))
        .contains(tuple(-8L, Sex.FEMALE))
        .contains(tuple(-10L, Sex.FEMALE));
  }

  @Test
  @DisplayName("Test validation of horse-create via Service")
  public void createInvalidHorse() {
    assertThat(assertThrows(ValidationException.class, () -> horseService.create(new HorseCreateDto(null, "", null, null, null, null, null)))
        .errors()).contains("Horse needs a name", "Horse needs a date of birth", "Horse needs a sex", "Horse description is given but blank");
  }
}
