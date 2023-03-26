package at.ac.tuwien.sepm.assignment.individual.service;

import at.ac.tuwien.sepm.assignment.individual.dto.OwnerCreateDto;
import at.ac.tuwien.sepm.assignment.individual.dto.OwnerDto;
import at.ac.tuwien.sepm.assignment.individual.dto.OwnerSearchDto;
import at.ac.tuwien.sepm.assignment.individual.exception.ValidationException;
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
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

@ActiveProfiles({"test", "datagen"}) // enable "test" spring profile during test execution in order to pick up configuration from application-test.yml
@SpringBootTest
public class OwnerServiceTest {

  @Autowired
  OwnerService ownerService;
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
  @DisplayName("Create a new Owner via Service")
  public void createANewOwner() {
    OwnerCreateDto ownerToCreate =
        new OwnerCreateDto("created firstname", "created lastname", null);

    assertDoesNotThrow(() -> {
      OwnerDto owner = ownerService.create(ownerToCreate);

      assertThat(owner).isNotNull();
      assertThat(owner.id()).isEqualTo(1L);
      assertThat(owner.firstName()).isEqualTo(ownerToCreate.firstName());
      assertThat(owner.lastName()).isEqualTo(ownerToCreate.lastName());
      assertThat(owner.email()).isEqualTo(ownerToCreate.email());

      List<OwnerDto> owners = ownerService.search(new OwnerSearchDto(null, null))
          .toList();
      assertThat(owners).isNotNull();
      assertThat(owners.size()).isGreaterThanOrEqualTo(11);
      assertThat(owners)
          .extracting(OwnerDto::id, OwnerDto::firstName, OwnerDto::lastName)
          .contains(tuple(-1L, "Bob", "Jones"))
          .contains(tuple(-2L, "Alice", "Lee"))
          .contains(tuple(-3L, "Tom", "Smith"))
          .contains(tuple(-4L, "Maggie", "Nguyen"))
          .contains(tuple(-5L, "Jim", "Brown"))
          .contains(tuple(-6L, "Emily", "Kim"))
          .contains(tuple(-7L, "Joshua", "Wong"))
          .contains(tuple(-8L, "Sarah", "Chen"))
          .contains(tuple(-9L, "Jack", "Lee"))
          .contains(tuple(-10L, "Karen", "Kim"))
          .contains(tuple(1L, ownerToCreate.firstName(), ownerToCreate.lastName()));
    });
  }

  @Test
  @DisplayName("Test validation of owner-create")
  public void createInvalidOwner() {
    assertThat(assertThrows(ValidationException.class, () -> ownerService.create(new OwnerCreateDto(null, null, "abc@abc")))
        .errors()).contains("Owner needs a firstname", "Owner needs a lastname", "Owner's email has an invalid format");
  }
}
