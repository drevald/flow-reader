package com.veve.flowreader

import org.junit.Assert
import org.junit.Test

class PagesSetsTest {

    @Test
    fun testParsing() {
        val sets = PagesSet.getPagesSet("1-2;3-6:7-8")
        Assert.assertNotNull(sets)
    }
}
