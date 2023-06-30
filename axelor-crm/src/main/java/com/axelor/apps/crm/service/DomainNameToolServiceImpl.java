package com.axelor.apps.crm.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.crm.service.app.AppCrmService;
import com.axelor.auth.db.AuditableModel;
import com.axelor.common.ObjectUtils;
import com.axelor.db.Query;
import com.google.inject.Inject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DomainNameToolServiceImpl<T> implements DomainNameToolService<T> {

  protected AppCrmService appCrmService;
  protected T t;

  @Inject
  public DomainNameToolServiceImpl(AppCrmService appCrmService) {
    this.appCrmService = appCrmService;
  }

  public void set(T t) {
    this.t = t;
  }

  public T get() {
    return t;
  }

  @Override
  public List<T> getEntitiesVithSameEmailAddress(
      String emailAddressName, List<Long> idListToExlude, String supplementaryFilter)
      throws AxelorException, ClassNotFoundException {
    List<T> entityWithSameDomainNameList = new ArrayList<>();
    if (ObjectUtils.notEmpty(emailAddressName)) {
      String emailDomainToIgnore = appCrmService.getAppCrm().getEmailDomainToIgnore();
      String entityDomainNameStr = emailAddressName.split("@")[1].replace("]", "");
      String finalFilter = this.computeFilter(supplementaryFilter);
      Class<? extends AuditableModel> modelClass =
          (Class<? extends AuditableModel>) Class.forName(t.getClass().getCanonicalName());
      if (ObjectUtils.isEmpty(emailDomainToIgnore)
          || !Arrays.asList(emailDomainToIgnore.split(",")).stream()
              .anyMatch(emailDomain -> emailDomain.matches(entityDomainNameStr))) {
        List<T> entityWithSameDomainNameListTemp =
            (List<T>)
                Query.of(modelClass)
                    .filter(finalFilter)
                    .bind("domainName", "%" + entityDomainNameStr + "%")
                    .bind("idList", idListToExlude)
                    .fetch();
        if (ObjectUtils.notEmpty(entityWithSameDomainNameListTemp)) {
          entityWithSameDomainNameList.addAll(entityWithSameDomainNameListTemp);
        }
      }
    }
    return entityWithSameDomainNameList;
  }

  protected String computeFilter(String supplementaryFilter) {
    StringBuilder stringBuilder =
        new StringBuilder(
            "self.emailAddress is not null and self.emailAddress.name like :domainName and self.id not in ( :idList )");

    if (ObjectUtils.notEmpty(supplementaryFilter)) {
      stringBuilder.append(" AND ");
      stringBuilder.append(supplementaryFilter);
    }
    return stringBuilder.toString();
  }
}
