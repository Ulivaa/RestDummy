package com.LT.restDummy.helper;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ResponseHeaderBuilderTest {

    @Test
    void shouldSetJsonWhenTypeJson() {
        HttpHeaders h = ResponseHeaderBuilder.build("json");
        assertEquals(MediaType.APPLICATION_JSON, h.getContentType());
    }

    @Test
    void shouldSetXmlWhenTypeNullOrNotJson() {
        assertEquals(MediaType.APPLICATION_XML, ResponseHeaderBuilder.build(null).getContentType());
        assertEquals(MediaType.APPLICATION_XML, ResponseHeaderBuilder.build("xml").getContentType());
        assertEquals(MediaType.APPLICATION_XML, ResponseHeaderBuilder.build("anything").getContentType());
    }

    @Test
    void shouldMergeExtraHeadersAndOverrideContentType() {
        Map<String, String> extra = new HashMap<String, String>();
        extra.put("X-Trace-Id", "abc");
        extra.put("Content-Type", "text/plain");

        HttpHeaders h = ResponseHeaderBuilder.build("json", extra, true);
        assertEquals(MediaType.parseMediaType("text/plain"), h.getContentType());
        assertEquals("abc", h.getFirst("X-Trace-Id"));
    }

    @Test
    void shouldFilterHopByHopHeaders() {
        Map<String, String> extra = new HashMap<String, String>();
        extra.put("Connection", "keep-alive");
        extra.put("Transfer-Encoding", "chunked");
        extra.put("X-Ok", "1");

        HttpHeaders h = ResponseHeaderBuilder.build("json", extra, true);
        assertNull(h.getFirst("Connection"));
        assertNull(h.getFirst("Transfer-Encoding"));
        assertEquals("1", h.getFirst("X-Ok"));
    }

    @Test
    void mergeShouldRespectOverrideFlag() {
        HttpHeaders base = ResponseHeaderBuilder.build("json");
        Map<String, String> extra = new HashMap<String, String>();
        extra.put("Content-Type", "text/plain");

        ResponseHeaderBuilder.merge(base, extra, false);
        assertEquals(MediaType.APPLICATION_JSON, base.getContentType()); // не переопределили

        ResponseHeaderBuilder.merge(base, extra, true);
        assertEquals(MediaType.parseMediaType("text/plain"), base.getContentType()); // переопределили
    }
}
