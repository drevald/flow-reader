package com.veve.flowreader

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.veve.flowreader.dao.PageGlyphRecord
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers
import org.junit.Test
import java.io.ByteArrayOutputStream

class JsonConversionTest {

    @Test
    fun testJsonGlyph() {
        val glyphRecord = PageGlyphRecord(1, 1, 1, 1, 1, 1, 1, 1, true, false, false)
        val mapper = ObjectMapper()
        val baos = ByteArrayOutputStream()
        mapper.writeValue(baos, glyphRecord)
        val glyphRecordRestored = mapper.readValue(baos.toByteArray(), PageGlyphRecord::class.java)
        assertThat(glyphRecord, Matchers.samePropertyValuesAs(glyphRecordRestored))
    }

    @Test
    fun testJsonGlyphs() {
        val glyphRecords = listOf(
            PageGlyphRecord(1, 1, 1, 1, 1, 1, 1, 1, true, false, false),
            PageGlyphRecord(1, 1, 1, 1, 1, 1, 1, 1, true, false, false),
            PageGlyphRecord(1, 1, 1, 1, 1, 1, 1, 1, true, false, false)
        )
        val mapper = ObjectMapper()
        val baos = ByteArrayOutputStream()
        mapper.writeValue(baos, glyphRecords)
        val glyphsRecordRestored: List<PageGlyphRecord> = mapper.readValue(
            baos.toByteArray(), object : TypeReference<List<PageGlyphRecord>>() {}
        )
        for (i in glyphsRecordRestored.indices) {
            assertThat(glyphsRecordRestored[i], Matchers.samePropertyValuesAs(glyphRecords[i]))
        }
    }
}
