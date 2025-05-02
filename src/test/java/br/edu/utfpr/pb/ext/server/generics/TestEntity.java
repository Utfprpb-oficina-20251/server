package br.edu.utfpr.pb.ext.server.generics;

import jakarta.persistence.Entity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data()
@NoArgsConstructor
@AllArgsConstructor()
@SuperBuilder
@Entity
public class TestEntity extends BaseEntity {
  private String name;
  private String description;
}
