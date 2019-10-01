package com.veve.flowreader;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.veve.flowreader.dao.PageGlyphRecord;
import com.veve.flowreader.model.impl.PageGlyphImpl;

import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class JsonConversionTest {

    @Test
    public void testJsonGlyph() throws Exception {
        PageGlyphRecord glyphRecord = new PageGlyphRecord(1, 1, 1, 1, 1, 1, 1, 1, true);
        ObjectMapper mapper = new ObjectMapper(); // create once, reuse
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        mapper.writeValue(baos, glyphRecord);
        PageGlyphRecord glyphRecordRestored = mapper.readValue(baos.toByteArray(), PageGlyphRecord.class);
        assertEquals(glyphRecord, glyphRecordRestored);
    }

    @Test
    public void testJsonGlyphs() throws Exception {
        List<PageGlyphRecord> glyphRecords = Arrays.asList(
                new PageGlyphRecord(1, 1, 1, 1, 1, 1, 1, 1, true),
                new PageGlyphRecord(1, 1, 1, 1, 1, 1, 1, 1, true),
                new PageGlyphRecord(1, 1, 1, 1, 1, 1, 1, 1, true)
        );
        ObjectMapper mapper = new ObjectMapper(); // create once, reuse
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        mapper.writeValue(baos, glyphRecords);
        List<PageGlyphRecord> glyphsRecordRestored = mapper.readValue(baos.toByteArray(), new TypeReference<List<PageGlyphRecord>>(){});
        assertArrayEquals(glyphRecords.toArray(), glyphsRecordRestored.toArray());
    }

}
