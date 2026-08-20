package com.rumantra.shared.constants;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Pins the taxonomy shape so it cannot drift away from frontend2/src/constants/projectTaxonomy.js
 * unnoticed.
 */
class ProjectTaxonomyTest {

  @Test
  @DisplayName("has the eight agreed categories")
  void categoryCount() {
    assertEquals(8, ProjectTaxonomy.categories().size());
    assertTrue(ProjectTaxonomy.isValidCategory("RESIDENTIAL"));
    assertTrue(ProjectTaxonomy.isValidCategory("MIXED_USE"));
    assertFalse(ProjectTaxonomy.isValidCategory("STUDENT_HOUSING"));
    assertFalse(ProjectTaxonomy.isValidCategory("RENOVATION"));
  }

  @Test
  @DisplayName("renovation is a scope, not a category")
  void scopes() {
    assertTrue(ProjectTaxonomy.isValidScope("NEW_BUILD"));
    assertTrue(ProjectTaxonomy.isValidScope("RENOVATION"));
    assertFalse(ProjectTaxonomy.isValidScope("RESIDENTIAL"));
    assertFalse(ProjectTaxonomy.isValidScope(null));
  }

  @Test
  @DisplayName("sub-category lists match the agreed sizes")
  void subCategorySizes() {
    assertEquals(7, ProjectTaxonomy.subCategoriesFor("RESIDENTIAL").size());
    assertEquals(22, ProjectTaxonomy.subCategoriesFor("COMMERCIAL").size());
    assertEquals(9, ProjectTaxonomy.subCategoriesFor("INDUSTRIAL").size());
    assertEquals(8, ProjectTaxonomy.subCategoriesFor("INSTITUTIONAL").size());
    assertEquals(4, ProjectTaxonomy.subCategoriesFor("INFRASTRUCTURE").size());
  }

  @Test
  @DisplayName("only categories with a list demand a sub-category")
  void requiredness() {
    assertTrue(ProjectTaxonomy.requiresSubCategory("RESIDENTIAL"));
    assertFalse(ProjectTaxonomy.requiresSubCategory("INTERIOR_ONLY"));
    assertFalse(ProjectTaxonomy.requiresSubCategory("LANDSCAPE"));
    assertFalse(ProjectTaxonomy.requiresSubCategory("MIXED_USE"));
    assertFalse(ProjectTaxonomy.requiresSubCategory("UNKNOWN"));
  }

  @Test
  @DisplayName("shophouse is valid under both residential and commercial")
  void sharedSubCategory() {
    assertTrue(ProjectTaxonomy.isValidSubCategory("RESIDENTIAL", "SHOPHOUSE"));
    assertTrue(ProjectTaxonomy.isValidSubCategory("COMMERCIAL", "SHOPHOUSE"));
    assertFalse(ProjectTaxonomy.isValidSubCategory("RESIDENTIAL", "CAFE"));
    assertFalse(ProjectTaxonomy.isValidSubCategory("INTERIOR_ONLY", "HOUSE"));
  }
}
