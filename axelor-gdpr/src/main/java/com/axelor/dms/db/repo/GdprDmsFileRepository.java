/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2025 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.axelor.dms.db.repo;

import com.axelor.db.JPA;
import com.axelor.dms.db.DMSFile;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.util.List;

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
