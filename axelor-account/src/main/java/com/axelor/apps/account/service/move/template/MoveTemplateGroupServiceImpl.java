package com.axelor.apps.account.service.move.template;

import com.axelor.apps.account.db.Journal;
import com.axelor.apps.account.db.MoveTemplate;
import com.axelor.apps.account.db.MoveTemplateType;
import com.axelor.apps.account.db.repo.JournalRepository;
import com.axelor.apps.account.db.repo.MoveTemplateRepository;
import com.axelor.apps.account.db.repo.MoveTemplateTypeRepository;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.service.CompanyService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.google.inject.Inject;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;

public class MoveTemplateGroupServiceImpl implements MoveTemplateGroupService {

  protected CompanyService companyService;
  protected MoveTemplateTypeRepository moveTemplateTypeRepository;
  protected MoveTemplateRepository moveTemplateRepository;
  protected AppBaseService appBaseService;

  @Inject
  public MoveTemplateGroupServiceImpl(
      CompanyService companyService,
      MoveTemplateTypeRepository moveTemplateTypeRepository,
      MoveTemplateRepository moveTemplateRepository,
      AppBaseService appBaseService) {
    this.companyService = companyService;
    this.moveTemplateTypeRepository = moveTemplateTypeRepository;
    this.moveTemplateRepository = moveTemplateRepository;
    this.appBaseService = appBaseService;
  }

  @Override
  public Map<String, Object> getOnNewValuesMap(
      Long companyId, Long moveTemplateTypeId, Long moveTemplateOriginId) throws AxelorException {
    Map<String, Object> valuesMap = new HashMap<>();

    MoveTemplateType moveTemplateType = moveTemplateTypeRepository.find(moveTemplateTypeId);
    Company company = companyService.getDefaultCompany(companyId);
    MoveTemplate moveTemplate = getDefaultMoveTemplate(moveTemplateOriginId, company);

    valuesMap.put("company", company);

    if (moveTemplate != null
        && moveTemplateType != null
        && moveTemplateType.getTypeSelect() != null) {
      valuesMap.put("moveTemplateType", moveTemplate.getMoveTemplateType());
      if (moveTemplateType.getTypeSelect() == MoveTemplateTypeRepository.TYPE_PERCENTAGE) {
        valuesMap.put("moveTemplate", moveTemplate);
      } else {
        valuesMap.put("moveDate", appBaseService.getTodayDate(company));
        valuesMap.put("moveTemplateSet", new HashSet<>(Arrays.asList(moveTemplate)));
      }
    }

    return valuesMap;
  }

  protected MoveTemplate getDefaultMoveTemplate(Long moveTemplateOriginId, Company company) {
    if (moveTemplateOriginId == null || company == null) {
      return null;
    }

    MoveTemplate moveTemplate = moveTemplateRepository.find(moveTemplateOriginId);
    if (moveTemplate != null
        && moveTemplate.getIsValid()
        && (moveTemplate.getEndOfValidityDate() == null
            || moveTemplate.getEndOfValidityDate().isAfter(appBaseService.getTodayDate(company)))) {
      Journal journal = moveTemplate.getJournal();
      if (journal != null
          && Objects.equals(company, journal.getCompany())
          && journal.getStatusSelect() == JournalRepository.STATUS_ACTIVE) {
        return moveTemplate;
      }
    }

    return null;
  }
}
