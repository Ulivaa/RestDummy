package com.LT.restDummy.util;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JsonXmlParamExtractorTest {

    @Nested
    class JsonTests {

        @Test
        void shouldExtractFlatJsonParam() {
            String json = "{\"id\":123,\"name\":\"John\"}";
            String value = JsonXmlParamExtractor.extract(json, "name", "json");
            assertEquals("John", value);
        }

        @Test
        void shouldExtractNestedJsonParam() {
            String json = "{\"person\":{\"name\":\"Alice\"}}";
            String value = JsonXmlParamExtractor.extract(json, "name", "json");
            assertEquals("Alice", value);
        }

        @Test
        void shouldExtractFirstValueIfMultiple() {
            String json = "{\"users\":[{\"name\":\"Mike\"}, {\"name\":\"Anna\"}]}";
            String value = JsonXmlParamExtractor.extract(json, "name", "json");
            assertEquals("Mike", value); // возвращает первый
        }

        @Test
        void shouldExtractParameterFromParameterValueStructure() {
            String json = "{\"parameter\":\"myParam\",\"value\":\"ABC123\"}";
            String value = JsonXmlParamExtractor.extract(json, "value", "json");
            assertEquals("ABC123", value);
        }
        @Test
        void shouldExtractValueFieldFromJsonObject() {
            String json = "{\"abc\": {\"value\": \"YES\"}}";
            String value = JsonXmlParamExtractor.extract(json, "abc", "json");
            assertEquals("YES", value);
        }


        @Test
        void shouldReturnNullIfJsonParamNotFound() {
            String json = "{\"foo\":\"bar\"}";
            String value = JsonXmlParamExtractor.extract(json, "missing", "json");
            assertNull(value);
        }

        @Test
        void shouldReturnNullForBlankParamOrRequest() {
            assertNull(JsonXmlParamExtractor.extract("", "id", "json"));
            assertNull(JsonXmlParamExtractor.extract("{\"id\":1}", "", "json"));
        }

        @Test
        void shouldHandleInvalidJsonGracefully() {
            String json = "{id:}";
            String value = JsonXmlParamExtractor.extract(json, "id", "json");
            assertNull(value);
        }
    }

    @Nested
    class XmlTests {

        @Test
        void shouldExtractSimpleXmlParam() {
            String xml = "<person><name>John</name></person>";
            String value = JsonXmlParamExtractor.extract(xml, "name", "xml");
            assertEquals("John", value);
        }

        @Test
        void shouldExtractNestedXmlParam() {
            String xml = "<root><person><name>Maria</name></person></root>";
            String value = JsonXmlParamExtractor.extract(xml, "name", "xml");
            assertEquals("Maria", value);
        }

        @Test
        void shouldReturnEmptyStringIfXmlParamNotFound() {
            String xml = "<root><other>data</other></root>";
            String value = JsonXmlParamExtractor.extract(xml, "name", "xml");
            assertEquals("", value); // XPath возвращает "", а не null
        }

        @Test
        void shouldHandleInvalidXmlGracefully() {
            String xml = "<root><broken>";
            String value = JsonXmlParamExtractor.extract(xml, "something", "xml");
            assertNull(value);
        }
    }

    @Nested
    class EdgeCases {

        @Test
        void shouldReturnErrorMessageForUnsupportedType() {
            String result = JsonXmlParamExtractor.extract("{\"id\":1}", "id", "csv");
            assertEquals("У вас не указан type для сервиса или type не поддерживается", result);
        }

        @Test
        void shouldIgnoreWhitespaceInJson() {
            String json = "   {   \"key\"   :   \"value\"   }   ";
            String result = JsonXmlParamExtractor.extract(json, "key", "json");
            assertEquals("value", result);
        }
    }
}
