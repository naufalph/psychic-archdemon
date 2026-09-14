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

  public static final Set<String> SCOPES =
      setOf("NEW_BUILD", "RENOVATION", "INTERIOR_FIT_OUT", "RESTORATION");

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
            "LABORATORY",
            "BANK_BRANCH",
            "GAS_STATION",
            "CAR_WASH",
            "EVENT_HALL",
            "CINEMA",
            "LAUNDRY"));
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
            "GOVERNMENT_FACILITY",
            "HOSPITAL",
            "UNIVERSITY",
            "NURSING_HOME",
            "ORPHANAGE",
            "FIRE_POLICE_STATION",
            "CEMETERY"));
    map.put(
        "INTERIOR_ONLY",
        setOf(
            "RESIDENTIAL_INTERIOR",
            "OFFICE_INTERIOR",
            "RETAIL_FNB_INTERIOR",
            "HOSPITALITY_INTERIOR"));
    map.put("LANDSCAPE", setOf("PRIVATE_GARDEN", "PUBLIC_PARK", "ROOFTOP_GARDEN", "STREETSCAPE"));
    // PARK and OTHER_INFRASTRUCTURE were retired. Posted projects that hold them are left as-is,
    // since sub-categories are only checked when a draft is submitted.
    map.put(
        "INFRASTRUCTURE",
        setOf("DRAINAGE", "ROAD", "BRIDGE", "TELECOM_TOWER", "PARKING_STRUCTURE", "SOLAR_PANEL"));
    map.put("MIXED_USE", setOf("LIVE_WORK", "TOD", "MIXED_USE_TOWER"));
    map.put(
        "RECREATIONAL",
        setOf("SPORTS_FIELD", "SWIMMING_POOL", "SPORTS_HALL", "GOLF_COURSE", "PLAYGROUND"));
    map.put("AGRICULTURAL", setOf("GREENHOUSE", "BARN", "FARM_STORAGE", "AGROTOURISM"));
    return Collections.unmodifiableMap(map);
  }

  private static Set<String> setOf(String... values) {
    return Collections.unmodifiableSet(new LinkedHashSet<>(java.util.Arrays.asList(values)));
  }
}
