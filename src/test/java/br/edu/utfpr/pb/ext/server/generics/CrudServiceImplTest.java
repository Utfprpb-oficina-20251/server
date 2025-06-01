package br.edu.utfpr.pb.ext.server.generics;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import jakarta.persistence.EntityNotFoundException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

/** Unit tests for CrudServiceImpl abstract class. */
@ExtendWith(MockitoExtension.class)
class CrudServiceImplTest {

  @Mock private JpaRepository<TestEntity, Long> repository;

  private CrudServiceImpl<TestEntity, Long> service;
  private TestEntity entity1;
  private TestEntity entity2;
  private List<TestEntity> entities;

  @BeforeEach
  void setUp() {
    service = new TestCrudServiceImpl(repository);

    entity1 = new TestEntity();
    entity1.setId(1L);
    entity1.setName("Test Entity 1");
    entity1.setDescription("Description 1");

    entity2 = new TestEntity();
    entity2.setId(2L);
    entity2.setName("Test Entity 2");
    entity2.setDescription("Description 2");

    entities = Arrays.asList(entity1, entity2);
  }

  @Test
  @DisplayName("findAll() should return all entities")
  void testFindAll() {
    when(repository.findAll()).thenReturn(entities);

    List<TestEntity> result = service.findAll();

    assertEquals(entities, result);
    verify(repository).findAll();
  }

  @Test
  @DisplayName("findAll(Sort) should return all entities sorted")
  void testFindAllSorted() {
    Sort sort = Sort.by("name").ascending();
    when(repository.findAll(sort)).thenReturn(entities);

    List<TestEntity> result = service.findAll(sort);

    assertEquals(entities, result);
    verify(repository).findAll(sort);
  }

  @Test
  @DisplayName("findAll(Pageable) should return a page of entities")
  void testFindAllPaginated() {
    Pageable pageable = PageRequest.of(0, 10);
    Page<TestEntity> page = new PageImpl<>(entities, pageable, entities.size());
    when(repository.findAll(pageable)).thenReturn(page);

    Page<TestEntity> result = service.findAll(pageable);

    assertEquals(page, result);
    verify(repository).findAll(pageable);
  }

  @Test
  @DisplayName("save(T) should save the entity")
  void testSave() {
    when(repository.save(entity1)).thenReturn(entity1);

    TestEntity result = service.save(entity1);

    assertEquals(entity1, result);
    verify(repository).save(entity1);
  }

  @Test
  @DisplayName("saveAndFlush(T) should save and flush the entity")
  void testSaveAndFlush() {
    when(repository.saveAndFlush(entity1)).thenReturn(entity1);

    TestEntity result = service.saveAndFlush(entity1);

    assertEquals(entity1, result);
    verify(repository).saveAndFlush(entity1);
  }

  @Test
  @DisplayName("save(Iterable<T>) should save all entities")
  void testSaveAll() {
    when(repository.saveAll(entities)).thenReturn(entities);

    Iterable<TestEntity> result = service.save(entities);

    assertEquals(entities, result);
    verify(repository).saveAll(entities);
  }

  @Test
  @DisplayName("flush() should flush changes")
  void testFlush() {
    doNothing().when(repository).flush();

    service.flush();

    verify(repository).flush();
  }

  @Test
  @DisplayName("findOne(ID) should return the entity with the given ID")
  void testFindOne() {
    when(repository.findById(1L)).thenReturn(Optional.of(entity1));

    TestEntity result = service.findOne(1L);

    assertEquals(entity1, result);
    verify(repository).findById(1L);
  }

  @Test
  @DisplayName("findOne(ID) deve retornar erro de entidade não encontrada caso não exista")
  void testFindOne_NotFoundShouldReturnEntityNotFound() {
    when(repository.findById(3L)).thenReturn(Optional.empty());
    assertThrows(EntityNotFoundException.class, () -> service.findOne(3L));
  }

  @Test
  @DisplayName("exists(ID) should return true if entity exists")
  void testExists() {
    when(repository.existsById(1L)).thenReturn(true);

    boolean result = service.exists(1L);

    assertTrue(result);
    verify(repository).existsById(1L);
  }

  @Test
  @DisplayName("exists(ID) should return false if entity does not exist")
  void testExistsNotFound() {
    when(repository.existsById(3L)).thenReturn(false);

    boolean result = service.exists(3L);

    assertFalse(result);
    verify(repository).existsById(3L);
  }

  @Test
  @DisplayName("count() should return the number of entities")
  void testCount() {
    when(repository.count()).thenReturn(2L);

    long result = service.count();

    assertEquals(2L, result);
    verify(repository).count();
  }

  @Test
  @DisplayName("delete(ID) should delete the entity with the given ID")
  void testDeleteById() {
    doNothing().when(repository).deleteById(1L);

    service.delete(1L);

    verify(repository).deleteById(1L);
  }

  @Test
  @DisplayName("delete(Iterable) should delete all given entities")
  void testDeleteIterable() {
    doNothing().when(repository).deleteAll(entities);

    service.delete(entities);

    verify(repository).deleteAll(entities);
  }

  @Test
  @DisplayName("deleteAll() should delete all entities")
  void testDeleteAll() {
    doNothing().when(repository).deleteAll();

    service.deleteAll();

    verify(repository).deleteAll();
  }
}
