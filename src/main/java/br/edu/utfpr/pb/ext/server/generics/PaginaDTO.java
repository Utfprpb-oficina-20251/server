package br.edu.utfpr.pb.ext.server.generics; // Garanta que o pacote está correto

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.Collections;
import java.util.List;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

public class PaginaDTO<T> extends PageImpl<T> {

  @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
  public PaginaDTO(
      @JsonProperty("content") List<T> content,
      @JsonProperty("number") int number,
      @JsonProperty("size") int size,
      @JsonProperty("totalElements") Long totalElements,
      @JsonProperty("pageable") JsonNode pageable,
      @JsonProperty("last") boolean last,
      @JsonProperty("totalPages") int totalPages,
      @JsonProperty("sort") JsonNode sort,
      @JsonProperty("first") boolean first,
      @JsonProperty("numberOfElements") int numberOfElements) {

    super(
        (content == null) ? Collections.emptyList() : content,
        PageRequest.of((number < 0) ? 0 : number, (size < 1) ? 10 : size),
        (totalElements == null) ? 0L : totalElements);
  }
}
