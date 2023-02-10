package com.axelor.dms.db.repo;

import com.axelor.db.JPA;
import com.axelor.dms.db.DMSFile;
import java.util.List;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

public class GdprDmsFileRepository extends DMSFileRepository {

  public List<DMSFile> findByModel(Long id, String model) {
    CriteriaBuilder criteriaBuilder = JPA.em().getCriteriaBuilder();
    CriteriaQuery<DMSFile> criteriaQuery = criteriaBuilder.createQuery(DMSFile.class);
    Root<DMSFile> root = criteriaQuery.from(DMSFile.class);

    Predicate relatedModel = criteriaBuilder.equal(root.get("relatedModel"), model);
    Predicate relatedId = criteriaBuilder.equal(root.get("relatedId"), id);
    Predicate isNotDirectory = criteriaBuilder.equal(root.get("isDirectory"), false);

    CriteriaQuery<DMSFile> select =
        criteriaQuery.select(root).where(relatedModel, relatedId, isNotDirectory);

    TypedQuery<DMSFile> typedQuery = JPA.em().createQuery(select);
    return typedQuery.getResultList();
  }
}
