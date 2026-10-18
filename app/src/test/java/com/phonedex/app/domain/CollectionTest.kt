package com.phonedex.app.domain

import com.phonedex.app.data.Sighting
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class CollectionTest {
    private fun s(id: Long, brand: String, model: String, ts: Long = id * 1000) =
        Sighting(id = id, brand = brand, model = model, location = "somewhere", timestamp = ts)

    @Test fun caseAndWhitespaceDoNotCreateDuplicates() {
        val e = listOf(
            s(1, "Samsung", "Galaxy S25 Ultra"),
            s(2, "samsung ", "  galaxy   s25 ULTRA "),
            s(3, "Samsung", "Galaxy S25 Ultra")
        ).toModelEntries()
        assertEquals(1, e.size)
        assertEquals(3, e[0].count)
    }

    @Test fun sameModelNameFromDifferentBrandsStaysSeparate() {
        val e = listOf(s(1, "BrandA", "Pro"), s(2, "BrandB", "Pro")).toModelEntries()
        assertEquals(2, e.size)
    }

    @Test fun similarButDifferentModelsAreNotMerged() {
        val e = listOf(
            s(1, "Samsung", "Galaxy S25"), s(2, "Samsung", "Galaxy S25+"),
            s(3, "Samsung", "Galaxy S25 Ultra"), s(4, "Samsung", "Galaxy S25 FE")
        ).toModelEntries()
        assertEquals(4, e.size)
    }

    @Test fun leadingBrandNameIsIgnoredForIdentity() {
        assertEquals(modelKey("OnePlus", "OnePlus 13"), modelKey("OnePlus", "13"))
        assertNotEquals(modelKey("OnePlus", "13"), modelKey("OnePlus", "13R"))
    }

    @Test fun fullNameDoesNotRepeatBrand() {
        assertEquals("Apple iPhone Air", fullName("Apple", "iPhone Air"))
        assertEquals("Apple iPhone Air", fullName("Apple", "Apple iPhone Air"))
    }

    @Test fun counterIncrementsByExactlyOne() {
        val before = listOf(s(1, "Samsung", "Galaxy S25 Ultra"), s(2, "Samsung", "Galaxy S25 Ultra"), s(3, "Samsung", "Galaxy S25 Ultra"))
        assertEquals(3, before.toModelEntries().single().count)
        val after = before + s(4, "Samsung", "Galaxy S25 Ultra")
        assertEquals(4, after.toModelEntries().single().count)
    }

    @Test fun deletingAndEditingUpdateCounters() {
        val all = listOf(s(1, "Apple", "iPhone Air"), s(2, "Apple", "iPhone Air"), s(3, "Apple", "iPhone 17 Pro"))
        val afterDelete = all.filter { it.id != 2L }
        assertEquals(1, afterDelete.toModelEntries().first { it.name == "Apple iPhone Air" }.count)
        val afterEdit = all.map { if (it.id == 3L) it.copy(model = "iPhone Air") else it }
        val entries = afterEdit.toModelEntries()
        assertEquals(1, entries.size)
        assertEquals(3, entries[0].count)
    }

    @Test fun firstLastAndHistoryOrder() {
        val e = listOf(s(1, "Apple", "iPhone Air", 500), s(2, "Apple", "iPhone Air", 900), s(3, "Apple", "iPhone Air", 100))
            .toModelEntries().single()
        assertEquals(100L, e.firstSpotted)
        assertEquals(900L, e.lastSpotted)
        assertEquals(listOf(2L, 1L, 3L), e.sightings.map { it.id })
    }

    @Test fun brandSummariesCountModelsAndSightings() {
        val b = listOf(
            s(1, "Apple", "iPhone Air"), s(2, "Apple", "iPhone Air"), s(3, "Apple", "iPhone 17 Pro"),
            s(4, "Samsung", "Galaxy S25 Ultra")
        ).toModelEntries().toBrandEntries()
        assertEquals(2, b.size)
        val apple = b.first { it.brand == "Apple" }
        assertEquals(2, apple.models)
        assertEquals(3, apple.sightings)
    }

    @Test fun sortModes() {
        val e = listOf(
            s(1, "Samsung", "Galaxy S25 Ultra", 100), s(2, "Samsung", "Galaxy S25 Ultra", 200), s(3, "Samsung", "Galaxy S25 Ultra", 300),
            s(4, "Samsung", "Galaxy A56", 900), s(5, "Samsung", "Galaxy Z Flip 7", 400), s(6, "Samsung", "Galaxy Z Flip 7", 450)
        ).toModelEntries()
        assertEquals("Samsung Galaxy S25 Ultra", e.sortModels(ModelSort.COUNT).first().name)
        assertEquals("Samsung Galaxy A56", e.sortModels(ModelSort.NAME).first().name)
        assertEquals("Samsung Galaxy A56", e.sortModels(ModelSort.RECENT).first().name)
    }
}

class BrandLogoNameTest {
    @Test fun lowercaseAndCaseInsensitive() {
        assertEquals("samsung", logoResourceName("Samsung"))
        assertEquals("redmi", logoResourceName("  REDMI "))
        assertEquals("oneplus", logoResourceName("OnePlus"))
        assertEquals("iqoo", logoResourceName("iQOO"))
    }

    @Test fun multiWordBrandsUseUnderscores() {
        assertEquals("black_shark", logoResourceName("Black Shark"))
        assertEquals("general_mobile", logoResourceName("General   Mobile"))
    }
}
