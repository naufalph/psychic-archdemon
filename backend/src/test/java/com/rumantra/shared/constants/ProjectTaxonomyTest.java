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
  @DisplayName("has the ten agreed categories")
  void categoryCount() {
    assertEquals(10, ProjectTaxonomy.categories().size());
    assertTrue(ProjectTaxonomy.isValidCategory("RESIDENTIAL"));
    assertTrue(ProjectTaxonomy.isValidCategory("MIXED_USE"));
    assertTrue(ProjectTaxonomy.isValidCategory("RECREATIONAL"));
    assertTrue(ProjectTaxonomy.isValidCategory("AGRICULTURAL"));
    assertFalse(ProjectTaxonomy.isValidCategory("STUDENT_HOUSING"));
    assertFalse(ProjectTaxonomy.isValidCategory("RENOVATION"));
  }

  @Test
  @DisplayName("renovation is a scope, not a category")
  void scopes() {
    assertEquals(4, ProjectTaxonomy.SCOPES.size());
    assertTrue(ProjectTaxonomy.isValidScope("NEW_BUILD"));
    assertTrue(ProjectTaxonomy.isValidScope("RENOVATION"));
    assertTrue(ProjectTaxonomy.isValidScope("INTERIOR_FIT_OUT"));
    assertTrue(ProjectTaxonomy.isValidScope("RESTORATION"));
    assertFalse(ProjectTaxonomy.isValidScope("RESIDENTIAL"));
    assertFalse(ProjectTaxonomy.isValidScope(null));
  }

  @Test
  @DisplayName("sub-category lists match the agreed sizes")
  void subCategorySizes() {
    assertEquals(7, ProjectTaxonomy.subCategoriesFor("RESIDENTIAL").size());
    assertEquals(28, ProjectTaxonomy.subCategoriesFor("COMMERCIAL").size());
    assertEquals(9, ProjectTaxonomy.subCategoriesFor("INDUSTRIAL").size());
    assertEquals(14, ProjectTaxonomy.subCategoriesFor("INSTITUTIONAL").size());
    assertEquals(4, ProjectTaxonomy.subCategoriesFor("INTERIOR_ONLY").size());
    assertEquals(4, ProjectTaxonomy.subCategoriesFor("LANDSCAPE").size());
    assertEquals(6, ProjectTaxonomy.subCategoriesFor("INFRASTRUCTURE").size());
    assertEquals(3, ProjectTaxonomy.subCategoriesFor("MIXED_USE").size());
    assertEquals(5, ProjectTaxonomy.subCategoriesFor("RECREATIONAL").size());
    assertEquals(4, ProjectTaxonomy.subCategoriesFor("AGRICULTURAL").size());
  }

  @Test
  @DisplayName("every category demands a sub-category")
  void requiredness() {
    ProjectTaxonomy.categories()
        .forEach(category -> assertTrue(ProjectTaxonomy.requiresSubCategory(category), category));
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

  @Test
  @DisplayName("park moved from infrastructure to landscape")
  void retiredInfrastructureValues() {
    assertFalse(ProjectTaxonomy.isValidSubCategory("INFRASTRUCTURE", "PARK"));
    assertFalse(ProjectTaxonomy.isValidSubCategory("INFRASTRUCTURE", "OTHER_INFRASTRUCTURE"));
    assertTrue(ProjectTaxonomy.isValidSubCategory("LANDSCAPE", "PUBLIC_PARK"));
  }
}
