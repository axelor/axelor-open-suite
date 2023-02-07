package com.axelor.apps.gdpr.service;

import com.axelor.apps.gdpr.db.GDPRRequest;
import com.axelor.auth.db.AuditableModel;
import com.axelor.meta.db.MetaField;
import java.util.List;
import java.util.Optional;

public interface GDPRResponseService {

  public List<MetaField> selectMetaFields(GDPRRequest gdprRequest) throws ClassNotFoundException;

  public Optional<String> getEmailFromPerson(AuditableModel reference);

  public Class<? extends AuditableModel> extractClassFromModel(String model)
      throws ClassNotFoundException;

  public AuditableModel extractReferenceFromModelAndId(String model, Long id)
      throws ClassNotFoundException;
}
