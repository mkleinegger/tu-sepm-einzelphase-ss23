package at.ac.tuwien.sepm.assignment.individual.rest;

import at.ac.tuwien.sepm.assignment.individual.dto.HorseDetailDto;
import at.ac.tuwien.sepm.assignment.individual.dto.HorseListDto;
import at.ac.tuwien.sepm.assignment.individual.dto.HorseTreeDto;
import at.ac.tuwien.sepm.assignment.individual.dto.OwnerDto;
import at.ac.tuwien.sepm.assignment.individual.type.Sex;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.junit.jupiter.api.Assumptions.assumeTrue;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles({"test", "datagen"}) // enable "test" spring profile during test execution in order to pick up configuration from application-test.yml
@SpringBootTest
@EnableWebMvc
@WebAppConfiguration
public class HorseEndpointTest {

  @Autowired
  private WebApplicationContext webAppContext;
  private MockMvc mockMvc;
  @Autowired
  ObjectMapper objectMapper;
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

  @BeforeEach
  public void setup() {
    this.mockMvc = MockMvcBuilders.webAppContextSetup(webAppContext).build();
  }

  @Test
  @DisplayName("Get all horses via Endpoint")
  public void gettingAllHorses() throws Exception {
    byte[] body = mockMvc
        .perform(MockMvcRequestBuilders
            .get("/horses")
            .accept(MediaType.APPLICATION_JSON)
        ).andExpect(status().isOk())
        .andReturn().getResponse().getContentAsByteArray();

    List<HorseListDto> horseResult = objectMapper.readerFor(HorseListDto.class).<HorseListDto>readValues(body).readAll();

    assertThat(horseResult).isNotNull();
    assertThat(horseResult.size()).isEqualTo(10);
    assertThat(horseResult)
        .extracting(HorseListDto::id, HorseListDto::name)
        .contains(tuple(-1L, "Thunder"),
            tuple(-2L, "Romeo"),
            tuple(-3L, "Sophie"),
            tuple(-4L, "Daisy"),
            tuple(-5L, "Spirit"),
            tuple(-6L, "Bella"),
            tuple(-7L, "Max"),
            tuple(-8L, "Luna"),
            tuple(-9L, "Apollo"),
            tuple(-10L, "Misty"));
  }

  @Test
  @DisplayName("Trying to get a non existing url")
  public void gettingNonexistentUrlReturns404() throws Exception {
    mockMvc.perform(MockMvcRequestBuilders
        .get("/asdf123")
    ).andExpect(status().isNotFound());
  }

  @Test
  @DisplayName("Get family tree with only 2 generations")
  public void gettingFamilyTreeWithLimit() throws Exception {
    byte[] body = mockMvc
        .perform(MockMvcRequestBuilders
            .get("/horses/-6/familytree?numberOfGenerations=2")
            .accept(MediaType.APPLICATION_JSON)
        ).andExpect(status().isOk())
        .andReturn().getResponse().getContentAsByteArray();

    HorseTreeDto familyTree = objectMapper.readerFor(HorseTreeDto.class).readValue(body);
    assertThat(familyTree).isNotNull();
    assertThat(familyTree.id()).isEqualTo(-6L);
    assertThat(familyTree.mother().id()).isEqualTo(-4L);
    assertThat(familyTree.father().id()).isEqualTo(-1L);
    assertThat(familyTree.mother().mother()).isNull();
    assertThat(familyTree.mother().father()).isNull();
    assertThat(familyTree.father().mother()).isNull();
    assertThat(familyTree.father().father()).isNull();
  }

  @Test
  @DisplayName("Trying update an horse where conflicts would be")
  public void updateWithConflicts() throws Exception {
    HorseDetailDto horse = new HorseDetailDto(-1L, "update via Endpoint", null, LocalDate.of(2023, 1, 21), Sex.FEMALE, null,
        null, new HorseDetailDto(-4L, null, null, null, null, null, null, null));

    byte[] body = mockMvc
        .perform(MockMvcRequestBuilders
            .put("/horses/-1")
            .accept(MediaType.APPLICATION_JSON)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsBytes(horse))
            .characterEncoding("utf-8")
        ).andExpect(status().isConflict())
        .andReturn().getResponse().getContentAsByteArray();

    ValidationOrConflictErrorRestDto horseResult =
        objectMapper.readerFor(ValidationOrConflictErrorRestDto.class).readValue(body);
    assertThat(horseResult).isNotNull();
    assertThat(horseResult.message()).isEqualTo("Conflicts occurred when trying to update the horse ");
    assertThat(horseResult.errors())
        .contains("The father horse must be male",
            "horse cannot be younger than any of his children (e.g.: Bella born at 2019-02-01)",
            "Cannot change sex of horse with children");
  }

  @Test
  @DisplayName("Deleting a horse")
  public void deleteHorse() throws Exception {
    HorseDetailDto horse = new HorseDetailDto(-1L, "update via Endpoint", null, LocalDate.of(2023, 1, 21), Sex.FEMALE, null,
        null, new HorseDetailDto(-2L, null, null, null, null, null, null, null));

    byte[] body = mockMvc
        .perform(MockMvcRequestBuilders
            .delete("/horses/-1")
            .accept(MediaType.APPLICATION_JSON)
        ).andExpect(status().isNoContent())
        .andReturn().getResponse().getContentAsByteArray();

    assertThat(body).isEmpty();
  }

  @Test
  @DisplayName("Update horse")
  public void updateHorse() throws Exception {
    HorseDetailDto horse = new HorseDetailDto(-8L,
        "update via Endpoint",
        null,
        LocalDate.of(2016, 6, 2),
        Sex.FEMALE,
        new OwnerDto(-1L, null, null, null),
        null,
        new HorseDetailDto(-5L, null, null, null, null, null, null, null));

    byte[] body = mockMvc
        .perform(MockMvcRequestBuilders
            .put("/horses/-8")
            .accept(MediaType.APPLICATION_JSON)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsBytes(horse))
            .characterEncoding("utf-8")
        ).andExpect(status().isOk())
        .andReturn().getResponse().getContentAsByteArray();

    HorseDetailDto horseResult = objectMapper.readerFor(HorseDetailDto.class).readValue(body);
    assertThat(horseResult).isNotNull();
    assertThat(horseResult.id()).isEqualTo(horse.id());
    assertThat(horseResult.name()).isEqualTo(horse.name());
    assertThat(horseResult.description()).isEqualTo(horse.description());
    assertThat(horseResult.dateOfBirth()).isEqualTo(horse.dateOfBirth());
    assertThat(horseResult.sex()).isEqualTo(horse.sex());
    assertThat(horseResult.ownerId()).isEqualTo(horse.ownerId());
    assertThat(horseResult.motherId()).isEqualTo(horse.motherId());
    assertThat(horseResult.fatherId()).isEqualTo(horse.fatherId());
  }
}
