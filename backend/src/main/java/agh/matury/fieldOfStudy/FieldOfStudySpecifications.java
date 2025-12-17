package agh.matury.fieldOfStudy;

import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

public final class FieldOfStudySpecifications {

  private FieldOfStudySpecifications() {}

  public static Specification<FieldOfStudy> byFilter(FieldOfStudyFilter f) {
    return (root, query, cb) -> {
      List<Predicate> preds = new ArrayList<>();

      boolean wantsDept = hasText(f.getDepartment());
      boolean wantsUni  = hasText(f.getUniversity());
      boolean wantsCity = hasText(f.getCity());

      var department = wantsDept ? root.join("department", JoinType.LEFT) : null;
      var university = (wantsUni || wantsCity) ? root.join("university", JoinType.LEFT) : null;

      if (f.getSemestersFrom() != null) {
        preds.add(cb.greaterThanOrEqualTo(root.get("duration"), f.getSemestersFrom()));
      }
      if (f.getSemestersTo() != null) {
        preds.add(cb.lessThanOrEqualTo(root.get("duration"), f.getSemestersTo()));
      }

      if (hasText(f.getName())) {
        preds.add(ilike(cb, root.get("name"), f.getName()));
      }

      if (wantsDept) {
        preds.add(ilike(cb, department.get("name"), f.getDepartment()));
      }
      if (wantsUni) {
        preds.add(ilike(cb, university.get("name"), f.getUniversity()));
      }
      if (wantsCity) {
        preds.add(ilike(cb, university.get("city"), f.getCity()));
      }

      if (f.getDegrees() != null && !f.getDegrees().isEmpty()) {
        preds.add(root.get("level").in(f.getDegrees()));
      }

      if (f.getIds() != null && !f.getIds().isEmpty()) {
        preds.add(root.get("id").in(f.getIds()));
      }

      if ((wantsDept || wantsUni || wantsCity)) {
        query.distinct(true);
      }

      if (preds.isEmpty()) {
        return cb.conjunction();
      } else {
        return cb.and(preds.toArray(new Predicate[0]));
      }
    };
  }

  private static boolean hasText(String s) {
    return StringUtils.hasText(s);
  }

  private static Predicate ilike(
      jakarta.persistence.criteria.CriteriaBuilder cb,
      jakarta.persistence.criteria.Expression<String> path,
      String value
  ) {
    return cb.like(cb.lower(path), "%" + value.trim().toLowerCase() + "%");
  }
}
