package com.rumantra.shared.constants;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * Server-side mirror of the IAI (Ikatan Arsitek Indonesia) building classification defined in
 * frontend2/src/constants/iaiTaxonomy.js, used by architect expertise and portfolio project type.
 * Both copies must be updated together; IaiTaxonomyTest pins the shape so drift fails the build.
 *
 * <p>The selectable unit is the (Kategori, Tipe) pair — "Hunian" exists under four different
 * categories and means something different in each — so a type code carries its category prefix and
 * is unique on its own. Labels and the example lists live only on the frontend, as with {@link
 * ProjectTaxonomy}.
 */
public final class IaiTaxonomy {

  private static final Map<String, Set<String>> TYPES_BY_CATEGORY = buildTypes();

  private static final Set<String> ALL_TYPES = buildAllTypes();

  private static final Map<String, String> PROJECT_CATEGORY_TO_IAI = buildProjectCategoryBridge();

  private IaiTaxonomy() {}

  public static Set<String> categories() {
    return TYPES_BY_CATEGORY.keySet();
  }

  public static boolean isValidCategory(String category) {
    return category != null && TYPES_BY_CATEGORY.containsKey(category);
  }

  public static Set<String> typesFor(String category) {
    return TYPES_BY_CATEGORY.getOrDefault(category, Collections.emptySet());
  }

  public static Set<String> types() {
    return ALL_TYPES;
  }

  public static boolean isValidType(String type) {
    return type != null && ALL_TYPES.contains(type);
  }

  /**
   * Nearest IAI equivalent of a {@link ProjectTaxonomy} category, for the one place the two
   * vocabularies meet: archiving a finished Rumantra project into a portfolio. Returns null where
   * the IAI table has no counterpart, so the caller leaves the field empty rather than storing a
   * foreign code.
   */
  public static String fromProjectCategory(String projectCategory) {
    return projectCategory == null ? null : PROJECT_CATEGORY_TO_IAI.get(projectCategory);
  }

  private static Map<String, Set<String>> buildTypes() {
    Map<String, Set<String>> map = new LinkedHashMap<>();
    map.put(
        "SOSIAL",
        setOf(
            "SOSIAL_PELAYANAN_MASYARAKAT", "SOSIAL_PERIBADATAN", "SOSIAL_SOSIAL", "SOSIAL_HUNIAN"));
    map.put("K1", setOf("K1_HUNIAN", "K1_INDUSTRI", "K1_KOMERSIAL"));
    map.put(
        "K2",
        setOf(
            "K2_HUNIAN",
            "K2_INDUSTRI",
            "K2_KOMERSIAL",
            "K2_KOMUNITAS",
            "K2_MEDIS",
            "K2_PENDIDIKAN",
            "K2_REKREASI"));
    map.put(
        "K3",
        setOf(
            "K3_HUNIAN",
            "K3_KOMERSIAL",
            "K3_KOMUNITAS",
            "K3_MEDIS",
            "K3_PENDIDIKAN",
            "K3_PERIBADATAN",
            "K3_LAINNYA"));
    map.put("KHUSUS", setOf("KHUSUS_PEMERINTAH"));
    return Collections.unmodifiableMap(map);
  }

  private static Set<String> buildAllTypes() {
    Set<String> all = new LinkedHashSet<>();
    TYPES_BY_CATEGORY.values().forEach(all::addAll);
    return Collections.unmodifiableSet(all);
  }

  private static Map<String, String> buildProjectCategoryBridge() {
    Map<String, String> map = new LinkedHashMap<>();
    map.put("RESIDENTIAL", "K3_HUNIAN");
    map.put("COMMERCIAL", "K2_KOMERSIAL");
    map.put("INDUSTRIAL", "K2_INDUSTRI");
    map.put("INSTITUTIONAL", "K2_KOMUNITAS");
    map.put("MIXED_USE", "K2_KOMERSIAL");
    return Collections.unmodifiableMap(map);
  }

  private static Set<String> setOf(String... values) {
    return Collections.unmodifiableSet(new LinkedHashSet<>(Arrays.asList(values)));
  }
}
