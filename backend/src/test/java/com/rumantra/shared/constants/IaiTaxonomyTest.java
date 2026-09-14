package com.rumantra.shared.constants;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Pins the IAI taxonomy shape so it cannot drift away from frontend2/src/constants/iaiTaxonomy.js
 * unnoticed.
 */
class IaiTaxonomyTest {

  @Test
  @DisplayName("has the five IAI categories")
  void categoryCount() {
    assertEquals(5, IaiTaxonomy.categories().size());
    assertTrue(IaiTaxonomy.isValidCategory("SOSIAL"));
    assertTrue(IaiTaxonomy.isValidCategory("KHUSUS"));
    assertFalse(IaiTaxonomy.isValidCategory("K4"));
    assertFalse(IaiTaxonomy.isValidCategory(null));
  }

  @Test
  @DisplayName("type lists match the agreed sizes")
  void typeSizes() {
    assertEquals(4, IaiTaxonomy.typesFor("SOSIAL").size());
    assertEquals(3, IaiTaxonomy.typesFor("K1").size());
    assertEquals(7, IaiTaxonomy.typesFor("K2").size());
    assertEquals(7, IaiTaxonomy.typesFor("K3").size());
    assertEquals(1, IaiTaxonomy.typesFor("KHUSUS").size());
    assertEquals(22, IaiTaxonomy.types().size());
    assertTrue(IaiTaxonomy.typesFor("UNKNOWN").isEmpty());
  }

  @Test
  @DisplayName("the same Tipe under a different Kategori is a distinct code")
  void sharedTypeNamesAreDistinctCodes() {
    assertTrue(IaiTaxonomy.isValidType("SOSIAL_HUNIAN"));
    assertTrue(IaiTaxonomy.isValidType("K1_HUNIAN"));
    assertTrue(IaiTaxonomy.isValidType("K2_HUNIAN"));
    assertTrue(IaiTaxonomy.isValidType("K3_HUNIAN"));
    assertFalse(IaiTaxonomy.isValidType("HUNIAN"));
  }

  @Test
  @DisplayName("rejects the legacy free-text values it replaces")
  void rejectsLegacyValues() {
    assertFalse(IaiTaxonomy.isValidType("Residential"));
    assertFalse(IaiTaxonomy.isValidType("Residential - Single Family"));
    assertFalse(IaiTaxonomy.isValidType("Sustainable Design"));
    assertFalse(IaiTaxonomy.isValidType(null));
  }

  @Test
  @DisplayName("bridges project categories that have an IAI counterpart, and only those")
  void projectCategoryBridge() {
    assertEquals("K3_HUNIAN", IaiTaxonomy.fromProjectCategory("RESIDENTIAL"));
    assertEquals("K2_KOMERSIAL", IaiTaxonomy.fromProjectCategory("COMMERCIAL"));
    assertEquals("K2_KOMERSIAL", IaiTaxonomy.fromProjectCategory("MIXED_USE"));
    assertNull(IaiTaxonomy.fromProjectCategory("INTERIOR_ONLY"));
    assertNull(IaiTaxonomy.fromProjectCategory("LANDSCAPE"));
    assertNull(IaiTaxonomy.fromProjectCategory("INFRASTRUCTURE"));
    assertNull(IaiTaxonomy.fromProjectCategory(null));
  }

  @Test
  @DisplayName("every bridged value resolves to a real IAI type")
  void bridgeTargetsAreValid() {
    for (String category : ProjectTaxonomy.categories()) {
      String mapped = IaiTaxonomy.fromProjectCategory(category);
      if (mapped != null) {
        assertTrue(IaiTaxonomy.isValidType(mapped), category + " maps to unknown type " + mapped);
      }
    }
  }
}
