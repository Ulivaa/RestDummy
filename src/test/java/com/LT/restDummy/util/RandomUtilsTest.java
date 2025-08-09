package com.LT.restDummy.util;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RandomUtilsTest {

    @Nested
    class RandomChar {
        @Test
        void shouldReturnStringOfGivenLength() {
            String result = RandomUtils.randomChar(10);
            assertEquals(10, result.length());
        }

        @Test
        void shouldContainOnlyLetters() {
            String result = RandomUtils.randomChar(100);
            assertTrue(result.matches("[a-zA-Z]+"));
        }

        @Test
        void shouldThrowExceptionIfLengthIsInvalid() {
            assertThrows(IllegalArgumentException.class, () -> RandomUtils.randomChar(0));
        }
    }

    @Nested
    class RandomNumber {
        @Test
        void shouldReturnDigitsOnly() {
            String result = RandomUtils.randomNumber(20);
            assertEquals(20, result.length());
            assertTrue(result.matches("\\d+"));
        }
    }

    @Nested
    class RandomNumberAndChar {
        @Test
        void shouldReturnAlphanumeric() {
            String result = RandomUtils.randomNumberAndChar(50);
            assertEquals(50, result.length());
            assertTrue(result.matches("[a-zA-Z0-9]+"));
        }
    }

    @Nested
    class RandomRqUID {
        @Test
        void shouldReturnUIDWithAllowedChars() {
            String result = RandomUtils.randomRqUID(16);
            assertEquals(16, result.length());
            assertTrue(result.matches("[a-fA-F0-9]+"));
        }
    }

    @Nested
    class InvalidLengthCases {
        @Test
        void shouldThrowForNegativeLength() {
            assertThrows(IllegalArgumentException.class, () -> RandomUtils.randomNumberAndChar(-1));
        }

        @Test
        void shouldThrowForZeroLength() {
            assertThrows(IllegalArgumentException.class, () -> RandomUtils.randomRqUID(0));
        }
    }
}
