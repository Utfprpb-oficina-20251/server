package br.edu.utfpr.pb.ext.server.generics;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityNotFoundException;
import java.util.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class CrudControllerTest {

  private MockMvc mockMvc;
  private ICrudService<TestEntity, Long> mockService;
  private ObjectMapper objectMapper;

  @SuppressWarnings("unchecked")
  @BeforeEach
  void setup() {
    mockService = mock(ICrudService.class);
    ModelMapper mockModelMapper = mock(ModelMapper.class);
    TestController controller = new TestController(mockService, mockModelMapper);
    mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    objectMapper = new ObjectMapper();

    when(mockModelMapper.map(any(TestEntity.class), eq(TestDTO.class)))
        .thenAnswer(
            invocation -> {
              TestEntity entity = invocation.getArgument(0);
              TestDTO dto = new TestDTO();
              dto.setId(entity.getId());
              dto.setName(entity.getName());
              dto.setDescription(entity.getDescription());
              return dto;
            });

    when(mockModelMapper.map(any(TestDTO.class), eq(TestEntity.class)))
        .thenAnswer(
            invocation -> {
              TestDTO dto = invocation.getArgument(0);
              TestEntity entity = new TestEntity();
              entity.setId(dto.getId());
              entity.setName(dto.getName());
              entity.setDescription(dto.getDescription());
              return entity;
            });
  }

  @Test
  void testFindAll_ReturnsAllEntities() throws Exception {
    TestEntity entity1 = new TestEntity();
    entity1.setId(1L);
    entity1.setName("Test 1");

    TestEntity entity2 = new TestEntity();
    entity2.setId(2L);
    entity2.setName("Test 2");

    List<TestEntity> entities = Arrays.asList(entity1, entity2);
    when(mockService.findAll()).thenReturn(entities);

    mockMvc
        .perform(get("/api/test"))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$", hasSize(2)))
        .andExpect(jsonPath("$[0].id", is(1)))
        .andExpect(jsonPath("$[0].name", is("Test 1")))
        .andExpect(jsonPath("$[1].id", is(2)))
        .andExpect(jsonPath("$[1].name", is("Test 2")));

    verify(mockService).findAll();
  }

  @Test
  void testFindAll_EmptyList_ReturnsEmptyArray() throws Exception {
    when(mockService.findAll()).thenReturn(Collections.emptyList());
    mockMvc
        .perform(get("/api/test"))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$", hasSize(0)));
    verify(mockService).findAll();
  }

  @Test
  void testFindAllPaged_DefaultSorting() throws Exception {
    PageRequest expectedPageRequest = PageRequest.of(0, 10);
    TestEntity entity = new TestEntity();
    entity.setId(1L);
    entity.setName("Test Entity");
    List<TestEntity> entityList = new ArrayList<>();
    entityList.add(entity);

    Page<TestEntity> page = new PageImpl<>(entityList, expectedPageRequest, entityList.size());
    when(mockService.findAll(any(PageRequest.class))).thenReturn(page);

    mockMvc
        .perform(get("/api/test/page").param("page", "0").param("size", "10"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content", hasSize(1)))
        .andExpect(jsonPath("$.content[0].id", is(1)))
        .andExpect(jsonPath("$.content[0].name", is("Test Entity")));

    ArgumentCaptor<PageRequest> cap = ArgumentCaptor.forClass(PageRequest.class);
    verify(mockService).findAll(cap.capture());
    PageRequest captured = cap.getValue();
    Assertions.assertEquals(0, captured.getPageNumber());
    Assertions.assertEquals(expectedPageRequest.getPageSize(), captured.getPageSize());
  }

  @Test
  void testFindAllPaged_WithSorting() throws Exception {
    PageRequest expectedPageRequest = PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "name"));
    List<TestEntity> entityList = new ArrayList<>();
    TestEntity entity = buildCustomTestEntity(1L, "Test", null);
    entityList.add(entity);

    long totalElements = entityList.size();
    Page<TestEntity> entityPage = new PageImpl<>(entityList, expectedPageRequest, totalElements);
    when(mockService.findAll(any(PageRequest.class))).thenReturn(entityPage);

    mockMvc
        .perform(
            get("/api/test/page")
                .param("page", "0")
                .param("size", "10")
                .param("order", "name")
                .param("asc", "true"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content", hasSize(1)));
    ArgumentCaptor<PageRequest> cap = ArgumentCaptor.forClass(PageRequest.class);
    verify(mockService).findAll(cap.capture());
    Assertions.assertTrue(
        Objects.requireNonNull(cap.getValue().getSort().getOrderFor("name")).isAscending());
  }

  @Test
  void testFindOne_ExistingEntity_ReturnsEntity() throws Exception {
    TestEntity e = new TestEntity();
    e.setId(1L);
    e.setName("Test Entity");
    when(mockService.findOne(1L)).thenReturn(e);
    mockMvc
        .perform(get("/api/test/1"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id", is(1)))
        .andExpect(jsonPath("$.name", is("Test Entity")));
    verify(mockService).findOne(1L);
  }

  @Test
  void testFindOne_NonExistingEntity_ReturnsNotFound() throws Exception {
    when(mockService.findOne(anyLong())).thenThrow(new EntityNotFoundException());
    mockMvc.perform(get("/api/test/999")).andExpect(status().isNotFound());
    verify(mockService).findOne(999L);
  }

  @Test
  void testCreate_ValidEntity_ReturnsCreated() throws Exception {
    TestDTO dto = new TestDTO(null, "New", "Desc");
    TestEntity saved = buildCustomTestEntity(1L, "New", "Desc");
    when(mockService.save(any(TestEntity.class))).thenReturn(saved);
    mockMvc
        .perform(
            post("/api/test")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id", is(1)))
        .andExpect(jsonPath("$.name", is("New")))
        .andExpect(jsonPath("$.description", is("Desc")));
    verify(mockService).save(any(TestEntity.class));
  }

  @Test
  void testUpdate_ValidEntityAndMatchingId_ReturnsUpdated() throws Exception {
    TestDTO dto = new TestDTO(1L, "Upd", "Desc");
    TestEntity updated = buildCustomTestEntity(1L, "Upd", "Desc");
    when(mockService.save(any(TestEntity.class))).thenReturn(updated);
    mockMvc
        .perform(
            put("/api/test/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id", is(1)))
        .andExpect(jsonPath("$.name", is("Upd")));
    verify(mockService).save(any(TestEntity.class));
  }

  @Test
  void testUpdate_IdMismatch_ReturnsBadRequest() throws Exception {
    TestDTO dto = new TestDTO(2L, "X", null);
    mockMvc
        .perform(
            put("/api/test/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
        .andExpect(status().isBadRequest());
    verify(mockService, never()).save(any(TestEntity.class));
  }

  @Test
  void testExists_ExistingEntity_ReturnsTrue() throws Exception {
    when(mockService.exists(1L)).thenReturn(true);
    mockMvc
        .perform(get("/api/test/exists/1"))
        .andExpect(status().isOk())
        .andExpect(content().string("true"));
    verify(mockService).exists(1L);
  }

  @Test
  void testExists_NonExistingEntity_ReturnsFalse() throws Exception {
    when(mockService.exists(999L)).thenReturn(false);
    mockMvc
        .perform(get("/api/test/exists/999"))
        .andExpect(status().isOk())
        .andExpect(content().string("false"));
    verify(mockService).exists(999L);
  }

  @Test
  void testCount_ReturnsCorrectCount() throws Exception {
    when(mockService.count()).thenReturn(42L);
    mockMvc
        .perform(get("/api/test/count"))
        .andExpect(status().isOk())
        .andExpect(content().string("42"));
    verify(mockService).count();
  }

  @Test
  void testDelete_ReturnsNoContent() throws Exception {
    doNothing().when(mockService).delete(1L);
    mockMvc.perform(delete("/api/test/1")).andExpect(status().isNoContent());
    verify(mockService).delete(1L);
  }

  private TestEntity buildCustomTestEntity(Long id, String name, String description) {
    return TestEntity.builder().id(id).name(name).description(description).build();
  }
}
