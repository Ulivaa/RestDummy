package com.LT.restDummy.helper;

import com.LT.restDummy.util.JsonXmlParamExtractor;
import com.LT.restDummy.util.RandomUtils;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class ResponseCorrelatorServiceTest {

    private final ResponseCorrelatorService service = new ResponseCorrelatorService();

    @Nested
    class MagicMarkers {

        @Test
        void shouldReplaceRqtmMarkerWithDate() {
            String response = "__rqtm__";
            try (MockedStatic<com.LT.restDummy.date.DateModule> mocked = mockStatic(com.LT.restDummy.date.DateModule.class)) {
                mocked.when(com.LT.restDummy.date.DateModule::get_date_now).thenReturn("2025-03-24");
                String result = service.correlate("", response, "json");
                assertEquals("2025-03-24", result);
            }
        }

        @Test
        void shouldReplaceGetNewRqUID() {
            try (MockedStatic<RandomUtils> mocked = mockStatic(RandomUtils.class)) {
                mocked.when(() -> RandomUtils.randomRqUID(10)).thenReturn("RANDOM_UID");
                String result = service.correlate("", "__getNewRqUID<10>__", "json");
                assertEquals("RANDOM_UID", result);
            }
        }

        @Test
        void shouldReplaceRndNumChar() {
            try (MockedStatic<RandomUtils> mocked = mockStatic(RandomUtils.class)) {
                mocked.when(() -> RandomUtils.randomNumberAndChar(5)).thenReturn("X1A2B");
                String result = service.correlate("", "__rndNumChar<5>__", "json");
                assertEquals("X1A2B", result);
            }
        }

        @Test
        void shouldReplaceRndNum() {
            try (MockedStatic<RandomUtils> mocked = mockStatic(RandomUtils.class)) {
                mocked.when(() -> RandomUtils.randomNumber(3)).thenReturn("123");
                String result = service.correlate("", "__rndNum<3>__", "json");
                assertEquals("123", result);
            }
        }

        @Test
        void shouldReplaceRndChar() {
            try (MockedStatic<RandomUtils> mocked = mockStatic(RandomUtils.class)) {
                mocked.when(() -> RandomUtils.randomChar(4)).thenReturn("ABCD");
                String result = service.correlate("", "__rndChar<4>__", "json");
                assertEquals("ABCD", result);
            }
        }
    }

    @Nested
    class RequestParamMarkers {

        @Test
        void shouldExtractParamFromRequestAndReplace() {
            String request = "{\"value\":\"42\"}";
            String response = "__value__";

            try (MockedStatic<JsonXmlParamExtractor> mocked = mockStatic(JsonXmlParamExtractor.class)) {
                mocked.when(() -> JsonXmlParamExtractor.extract(anyString(), eq("value"), eq("json"))).thenReturn("42");
                String result = service.correlate(request, response, "json");
                assertEquals("42", result);
            }
        }

        @Test
        void shouldReplaceCustomMarkersTripleUnderscore() {
            String response = "{\"parameter\":\"abc\",\"value\":\"___abc___\"}";
            String request = "{\"parameter\":\"abc\",\"value\":\"replaced\"}";

            String result = service.correlate(request, response, "json");
            assertEquals("{\"parameter\":\"abc\",\"value\":\"replaced\"}", result);
        }

        @Test
        void shouldReplaceCustomMarkersDashUnderscore() {
            String response = "\"abc\": {\"value\": \"_-_abc_-_\"}";
            String request = "{\"abc\":{\"value\":\"YES\"}}";

            String result = service.correlate(request, response, "json");
            assertEquals("\"abc\": {\"value\": \"YES\"}", result);
        }
    }

    @Nested
    class EdgeCases {

        @Test
        void shouldReturnUnchangedIfNoMarkers() {
            String original = "{\"result\":\"ok\"}";
            String result = service.correlate("{}", original, "json");
            assertEquals(original, result);
        }

        @Test
        void shouldHandleEmptyInputGracefully() {
            String result = service.correlate("", "", "json");
            assertEquals("", result);
        }

        @Test
        void shouldIgnoreMalformedMagicMarker() {
            String result = service.correlate("", "__rndNum<>__", "json");
            assertEquals("__rndNum<>__", result);
        }

        @Test
        void shouldIgnoreNonMatchingParamInRequest() {
            try (MockedStatic<JsonXmlParamExtractor> mocked = mockStatic(JsonXmlParamExtractor.class)) {
                mocked.when(() -> JsonXmlParamExtractor.extract(anyString(), eq("nonexistent"), anyString())).thenReturn("");
                String result = service.correlate("{}", "__nonexistent__", "json");
                assertEquals("", result);
            }
        }
    }
}
