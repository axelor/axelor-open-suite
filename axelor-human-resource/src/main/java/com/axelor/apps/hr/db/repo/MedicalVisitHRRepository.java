package com.axelor.apps.hr.db.repo;

import com.axelor.apps.hr.db.MedicalVisit;

public class MedicalVisitHRRepository extends MedicalVisitRepository {

  @Override
  public MedicalVisit copy(MedicalVisit entity, boolean deep) {
    MedicalVisit copy = super.copy(entity, deep);
    copy.setStatusSelect(MedicalVisitRepository.STATUS_DRAFT);

    return copy;
  }
}
