package com.axelor.apps.gdpr.service.response;

import com.axelor.apps.gdpr.db.GDPRRequest;
import com.axelor.auth.db.AuditableModel;
import com.axelor.exception.AxelorException;
import com.axelor.meta.db.MetaField;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import javax.mail.MessagingException;
import wslite.json.JSONException;

public interface GdprResponseService {

  public List<MetaField> selectMetaFields(GDPRRequest gdprRequest) throws ClassNotFoundException;

  public Optional<String> getEmailFromPerson(AuditableModel reference);

  public Class<? extends AuditableModel> extractClassFromModel(String model)
      throws ClassNotFoundException;

  public AuditableModel extractReferenceFromModelAndId(String model, Long id)
      throws ClassNotFoundException;

  void generateResponse(GDPRRequest gdprRequest)
      throws AxelorException, IOException, ClassNotFoundException, JSONException;

  void sendResponse(GDPRRequest gdprRequest)
      throws AxelorException, JSONException, IOException, ClassNotFoundException,
          InstantiationException, IllegalAccessException, MessagingException;
}
