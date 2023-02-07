package com.axelor.apps.gdpr.service;

import com.axelor.apps.gdpr.db.GDPRRequest;
import com.axelor.apps.message.db.EmailAddress;
import com.axelor.auth.db.AuditableModel;
import com.axelor.db.JPA;
import com.axelor.db.mapper.Mapper;
import com.axelor.meta.db.MetaField;
import java.util.List;
import java.util.Optional;
import javax.persistence.Query;

public class GDPRResponseServiceImpl implements GDPRResponseService {
  @Override
  public List<MetaField> selectMetaFields(GDPRRequest gdprRequest) throws ClassNotFoundException {

    Class<? extends AuditableModel> modelSelectKlass =
        extractClassFromModel(gdprRequest.getModelSelect());
    String modelSelect = modelSelectKlass.getSimpleName();

    Query selectedObj =
        JPA.em()
            .createQuery(
                "SELECT self FROM MetaField self WHERE self.typeName= :modelSelect AND self.relationship NOT LIKE 'ManyToMany'");
    selectedObj.setParameter("modelSelect", modelSelect);

    return selectedObj.getResultList();
  }

  @Override
  public Optional<String> getEmailFromPerson(AuditableModel reference) {
    Mapper mapper = Mapper.of(reference.getClass());
    return Optional.ofNullable(mapper.get(reference, "emailAddress"))
        .map(EmailAddress.class::cast)
        .map(EmailAddress::getAddress);
  }

  @Override
  public Class<? extends AuditableModel> extractClassFromModel(String model)
      throws ClassNotFoundException {
    return (Class<? extends AuditableModel>) Class.forName(model);
  }

  @Override
  public AuditableModel extractReferenceFromModelAndId(String model, Long id)
      throws ClassNotFoundException {
    return com.axelor.db.Query.of(extractClassFromModel(model)).filter("id = ?", id).fetchOne();
  }
}
