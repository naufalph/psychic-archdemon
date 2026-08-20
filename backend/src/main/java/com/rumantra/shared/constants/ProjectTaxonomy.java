package com.rumantra.shared.constants;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * Server-side mirror of the three-level project taxonomy defined in
 * frontend2/src/constants/projectTaxonomy.js. Both copies must be updated together;
 * ProjectTaxonomyTest pins the shape so drift fails the build rather than letting an unrecognised
 * category reach the database.
 */
public final class ProjectTaxonomy {

  public static final Set<String> SCOPES = setOf("NEW_BUILD", "RENOVATION");

  private static final Map<String, Set<String>> SUB_CATEGORIES = buildSubCategories();

  private ProjectTaxonomy() {}

  public static Set<String> categories() {
    return SUB_CATEGORIES.keySet();
  }

  public static boolean isValidScope(String scope) {
    return scope != null && SCOPES.contains(scope);
  }

  public static boolean isValidCategory(String category) {
    return category != null && SUB_CATEGORIES.containsKey(category);
  }

  public static Set<String> subCategoriesFor(String category) {
    return SUB_CATEGORIES.getOrDefault(category, Collections.emptySet());
  }

  /** Categories without a third level submit no sub-category at all, so none is demanded. */
  public static boolean requiresSubCategory(String category) {
    return !subCategoriesFor(category).isEmpty();
  }

  public static boolean isValidSubCategory(String category, String subCategory) {
    return subCategory != null && subCategoriesFor(category).contains(subCategory);
  }

  private static Map<String, Set<String>> buildSubCategories() {
    Map<String, Set<String>> map = new LinkedHashMap<>();
    map.put(
        "RESIDENTIAL",
        setOf(
            "HOUSE",
            "VILLA",
            "APARTMENT_UNIT",
            "BOARDING_HOUSE",
            "TOWNHOUSE",
            "DORMITORY",
            "SHOPHOUSE"));
    map.put(
        "COMMERCIAL",
        setOf(
            "RETAIL_STORE",
            "OFFICE",
            "SHOPHOUSE",
            "SHOWROOM",
            "SALON",
            "CLINIC",
            "WAREHOUSE",
            "MINIMARKET",
            "MALL_BOOTH",
            "CAFE",
            "RESTAURANT",
            "FOOD_KIOSK",
            "BAR_LOUNGE",
            "BAKERY",
            "HOTEL",
            "GUESTHOUSE",
            "AIRBNB_UNIT",
            "RESORT",
            "GYM",
            "SPA",
            "PHARMACY",
            "LABORATORY"));
    map.put(
        "INDUSTRIAL",
        setOf(
            "FACTORY",
            "FOOD_PROCESSING",
            "WORKSHOP",
            "PACKAGING_PLANT",
            "COLD_STORAGE",
            "UTILITY_BUILDING",
            "WASTE_MANAGEMENT",
            "INDUSTRIAL_WAREHOUSE",
            "LOGISTIC_HUB"));
    map.put(
        "INSTITUTIONAL",
        setOf(
            "KINDERGARTEN",
            "SCHOOL",
            "LEARNING_CENTER",
            "LIBRARY",
            "RELIGIOUS_FACILITY",
            "COMMUNITY_CENTER",
            "GALLERY_MUSEUM",
            "GOVERNMENT_FACILITY"));
    map.put("INTERIOR_ONLY", Collections.emptySet());
    map.put("LANDSCAPE", Collections.emptySet());
    map.put("INFRASTRUCTURE", setOf("PARK", "DRAINAGE", "ROAD", "OTHER_INFRASTRUCTURE"));
    map.put("MIXED_USE", Collections.emptySet());
    return Collections.unmodifiableMap(map);
  }

  private static Set<String> setOf(String... values) {
    return Collections.unmodifiableSet(new LinkedHashSet<>(java.util.Arrays.asList(values)));
  }
}
