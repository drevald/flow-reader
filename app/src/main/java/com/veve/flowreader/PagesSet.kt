package com.veve.flowreader

class PagesSet {
    val start: Int
    val end: Int
    constructor(str: String) {
        if (str.indexOf('-') == -1) {
            val n = Integer.parseInt(str.trim())
            start = n
            end = n
        } else {
            start = -1 + Integer.parseInt(str.substring(0, str.lastIndexOf('-')).trim())
            end = -1 + Integer.parseInt(str.substring(1 + str.lastIndexOf('-')).trim())
        }
    }

    constructor(start: Int, end: Int) {
        this.start = start
        this.end = end
    }

    companion object {
        @JvmStatic
        fun getPagesSet(str: String): List<PagesSet> {
            if (str.isEmpty()) return emptyList()
            return str.split(Regex("[\\s,;:]+"))
                .filter { it.trim().isNotEmpty() }
                .map { PagesSet(it.trim()) }
        }
    }
}
