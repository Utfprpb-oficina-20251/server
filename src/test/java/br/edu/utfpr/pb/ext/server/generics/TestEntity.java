package br.edu.utfpr.pb.ext.server.generics;

import lombok.*;
import lombok.experimental.SuperBuilder;

@NoArgsConstructor
@AllArgsConstructor()
@SuperBuilder
@Getter
@Setter
public class TestEntity extends BaseEntity {
  private String name;
  private String description;
}
