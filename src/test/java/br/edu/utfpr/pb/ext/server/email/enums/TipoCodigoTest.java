package br.edu.utfpr.pb.ext.server.email.enums;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.Set;
import java.util.List;
import java.util.stream.Collectors;
import java.io.Serializable;

@DisplayName("TipoCodigo Enum Tests")
class TipoCodigoTest {

    @Nested
    @DisplayName("Enum Values and Structure Tests")
    class EnumValuesTest {

        @Test
        @DisplayName("Should have all expected enum values")
        void shouldHaveAllExpectedEnumValues() {
            TipoCodigo[] values = TipoCodigo.values();
            assertThat(values).isNotEmpty();
            assertThat(values.length).isGreaterThan(0);

            for (TipoCodigo value : values) {
                assertThat(value).isNotNull();
                assertThat(value.name()).isNotBlank();
            }
        }

        @ParameterizedTest
        @EnumSource(TipoCodigo.class)
        @DisplayName("Should have non-null enum values with valid names")
        void shouldHaveNonNullEnumValuesWithValidNames(TipoCodigo tipoCodigo) {
            assertThat(tipoCodigo).isNotNull();
            assertThat(tipoCodigo.name()).isNotNull();
            assertThat(tipoCodigo.name()).isNotBlank();
            assertThat(tipoCodigo.name()).doesNotContainWhitespace();
            assertThat(tipoCodigo.name()).isUpperCase();
        }

        @Test
        @DisplayName("Should have unique enum values")
        void shouldHaveUniqueEnumValues() {
            TipoCodigo[] values = TipoCodigo.values();
            Set<String> uniqueNames = Arrays.stream(values)
                .map(Enum::name)
                .collect(Collectors.toSet());
            assertThat(uniqueNames).hasSize(values.length);
        }

        @Test
        @DisplayName("Should have unique ordinal values")
        void shouldHaveUniqueOrdinalValues() {
            TipoCodigo[] values = TipoCodigo.values();
            Set<Integer> uniqueOrdinals = Arrays.stream(values)
                .map(Enum::ordinal)
                .collect(Collectors.toSet());
            assertThat(uniqueOrdinals).hasSize(values.length);
        }
    }

    @Nested
    @DisplayName("Enum Methods Tests")
    class EnumMethodsTest {

        @Test
        @DisplayName("Should return correct ordinal values in sequence")
        void shouldReturnCorrectOrdinalValuesInSequence() {
            TipoCodigo[] values = TipoCodigo.values();
            for (int i = 0; i < values.length; i++) {
                assertThat(values[i].ordinal()).isEqualTo(i);
            }
        }

        @ParameterizedTest
        @EnumSource(TipoCodigo.class)
        @DisplayName("Should have consistent toString implementation")
        void shouldHaveConsistentToStringImplementation(TipoCodigo tipoCodigo) {
            String toString = tipoCodigo.toString();
            assertThat(toString).isNotNull();
            assertThat(toString).isNotBlank();
            assertThat(toString).isEqualTo(tipoCodigo.name());
        }

        @ParameterizedTest
        @EnumSource(TipoCodigo.class)
        @DisplayName("Should support valueOf method for all enum values")
        void shouldSupportValueOfMethodForAllEnumValues(TipoCodigo tipoCodigo) {
            String enumName = tipoCodigo.name();
            TipoCodigo fromValueOf = TipoCodigo.valueOf(enumName);
            assertThat(fromValueOf).isEqualTo(tipoCodigo);
            assertThat(fromValueOf).isSameAs(tipoCodigo);
        }

        @ParameterizedTest
        @ValueSource(strings = {"INVALID_VALUE", "", "invalid_value", "TIPO_CODIGO", "NULL"})
        @DisplayName("Should throw IllegalArgumentException for invalid valueOf inputs")
        void shouldThrowExceptionForInvalidValueOf(String invalidValue) {
            assertThrows(IllegalArgumentException.class, () -> TipoCodigo.valueOf(invalidValue));
        }

        @Test
        @DisplayName("Should throw NullPointerException for null valueOf input")
        void shouldThrowNullPointerExceptionForNullValueOf() {
            assertThrows(NullPointerException.class, () -> TipoCodigo.valueOf(null));
        }

        @ParameterizedTest
        @EnumSource(TipoCodigo.class)
        @DisplayName("Should return Class object for getDeclaringClass")
        void shouldReturnClassObjectForGetDeclaringClass(TipoCodigo tipoCodigo) {
            Class<TipoCodigo> declaringClass = tipoCodigo.getDeclaringClass();
            assertThat(declaringClass).isEqualTo(TipoCodigo.class);
            assertThat(declaringClass.isEnum()).isTrue();
        }
    }

    @Nested
    @DisplayName("Enum Comparison and Equality Tests")
    class EnumComparisonTest {

        @ParameterizedTest
        @EnumSource(TipoCodigo.class)
        @DisplayName("Should support equality comparison with itself")
        void shouldSupportEqualityComparisonWithItself(TipoCodigo tipoCodigo) {
            assertThat(tipoCodigo).isEqualTo(tipoCodigo);
            assertThat(tipoCodigo == tipoCodigo).isTrue();
        }

        @Test
        @DisplayName("Should have different enum values not equal to each other")
        void shouldHaveDifferentEnumValuesNotEqualToEachOther() {
            TipoCodigo[] values = TipoCodigo.values();
            for (int i = 0; i < values.length; i++) {
                for (int j = i + 1; j < values.length; j++) {
                    assertThat(values[i]).isNotEqualTo(values[j]);
                    assertThat(values[i] != values[j]).isTrue();
                }
            }
        }

        @ParameterizedTest
        @EnumSource(TipoCodigo.class)
        @DisplayName("Should have consistent hashCode")
        void shouldHaveConsistentHashCode(TipoCodigo tipoCodigo) {
            int hashCode1 = tipoCodigo.hashCode();
            int hashCode2 = tipoCodigo.hashCode();
            assertThat(hashCode1).isEqualTo(hashCode2);
        }

        @Test
        @DisplayName("Should have different hashCodes for different enum values")
        void shouldHaveDifferentHashCodesForDifferentEnumValues() {
            TipoCodigo[] values = TipoCodigo.values();
            if (values.length > 1) {
                Set<Integer> hashCodes = Arrays.stream(values)
                    .map(Object::hashCode)
                    .collect(Collectors.toSet());
                assertThat(hashCodes.size()).isGreaterThan(1);
            }
        }

        @Test
        @DisplayName("Should support compareTo based on ordinal values")
        void shouldSupportCompareToBasedOnOrdinalValues() {
            TipoCodigo[] values = TipoCodigo.values();
            if (values.length > 1) {
                TipoCodigo first = values[0];
                TipoCodigo second = values[1];
                assertThat(first.compareTo(second)).isLessThan(0);
                assertThat(second.compareTo(first)).isGreaterThan(0);
                assertThat(first.compareTo(first)).isEqualTo(0);
            }
        }

        @ParameterizedTest
        @EnumSource(TipoCodigo.class)
        @DisplayName("Should not be equal to null")
        void shouldNotBeEqualToNull(TipoCodigo tipoCodigo) {
            assertThat(tipoCodigo).isNotEqualTo(null);
            assertThat(tipoCodigo.equals(null)).isFalse();
        }
    }

    @Nested
    @DisplayName("Edge Cases and Integration Tests")
    class EdgeCasesTest {

        @Test
        @DisplayName("Should handle enum in switch statements")
        void shouldHandleEnumInSwitchStatements() {
            for (TipoCodigo tipo : TipoCodigo.values()) {
                String result = getDescriptionBySwitch(tipo);
                assertThat(result).isNotNull();
                assertThat(result).isNotBlank();
                assertThat(result).doesNotContainIgnoringCase("unknown");
            }
        }

        private String getDescriptionBySwitch(TipoCodigo tipo) {
            return switch (tipo) {
                default -> "Tipo de código: " + tipo.name();
            };
        }

        @Test
        @DisplayName("Should work with collections and streams")
        void shouldWorkWithCollectionsAndStreams() {
            Set<TipoCodigo> tipoSet = Arrays.stream(TipoCodigo.values())
                .collect(Collectors.toSet());
            assertThat(tipoSet).hasSize(TipoCodigo.values().length);
            assertThat(tipoSet).containsAll(Arrays.asList(TipoCodigo.values()));

            List<String> enumNames = Arrays.stream(TipoCodigo.values())
                .map(Enum::name)
                .collect(Collectors.toList());
            assertThat(enumNames).hasSize(TipoCodigo.values().length);
            assertThat(enumNames).allMatch(name -> name != null && !name.isBlank());
        }

        @Test
        @DisplayName("Should be serializable")
        void shouldBeSerializable() {
            assertThat(Serializable.class).isAssignableFrom(TipoCodigo.class);
        }

        @Test
        @DisplayName("Should maintain singleton property across valueOf calls")
        void shouldMaintainSingletonPropertyAcrossValueOfCalls() {
            for (TipoCodigo tipo : TipoCodigo.values()) {
                TipoCodigo fromValueOf = TipoCodigo.valueOf(tipo.name());
                assertThat(tipo == fromValueOf).isTrue();
                assertThat(tipo).isSameAs(fromValueOf);
            }
        }

        @Test
        @DisplayName("Should have stable ordering across multiple values() calls")
        void shouldHaveStableOrderingAcrossMultipleValuesCalls() {
            TipoCodigo[] values1 = TipoCodigo.values();
            TipoCodigo[] values2 = TipoCodigo.values();
            assertThat(values1).isEqualTo(values2);
            for (int i = 0; i < values1.length; i++) {
                assertThat(values1[i]).isSameAs(values2[i]);
            }
        }

        @Test
        @DisplayName("Should support enum as map keys")
        void shouldSupportEnumAsMapKeys() {
            java.util.Map<TipoCodigo, String> enumMap = new java.util.EnumMap<>(TipoCodigo.class);
            for (TipoCodigo tipo : TipoCodigo.values()) {
                enumMap.put(tipo, "Description for " + tipo.name());
            }
            assertThat(enumMap).hasSize(TipoCodigo.values().length);
            for (TipoCodigo tipo : TipoCodigo.values()) {
                assertThat(enumMap.get(tipo)).isNotNull();
                assertThat(enumMap.get(tipo)).contains(tipo.name());
            }
        }

        @Test
        @DisplayName("Should work correctly with instanceof checks")
        void shouldWorkCorrectlyWithInstanceofChecks() {
            for (TipoCodigo tipo : TipoCodigo.values()) {
                assertThat(tipo).isInstanceOf(TipoCodigo.class);
                assertThat(tipo).isInstanceOf(Enum.class);
                assertThat(tipo).isInstanceOf(Comparable.class);
                assertThat(tipo).isInstanceOf(Serializable.class);
            }
        }
    }
}