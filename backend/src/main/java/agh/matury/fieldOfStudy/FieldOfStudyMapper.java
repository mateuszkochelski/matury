package agh.matury.fieldOfStudy;

import java.util.HashMap;
import java.util.Map;

public final class FieldOfStudyMapper {
  private static final Map<String, String> EN_TO_PL = new HashMap<>();

  static {
    EN_TO_PL.put("bachelor", "licencjackie");
    EN_TO_PL.put("master", "magisterskie");
    EN_TO_PL.put("engineer", "inżynierskie");
    EN_TO_PL.put("long_master", "jednolite_magisterskie");
  }

  private FieldOfStudyMapper() {
  }

  public static String toPolish(String level) {
    if (level == null)
      return null;
    return EN_TO_PL.getOrDefault(level.toLowerCase(), level);
  }
}
