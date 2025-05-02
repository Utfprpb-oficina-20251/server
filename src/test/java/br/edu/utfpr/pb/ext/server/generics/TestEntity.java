package br.edu.utfpr.pb.ext.server.generics;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * A simple entity class for testing CrudServiceImpl.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TestEntity extends BaseEntity {
    private String name;
    private String description;
}
