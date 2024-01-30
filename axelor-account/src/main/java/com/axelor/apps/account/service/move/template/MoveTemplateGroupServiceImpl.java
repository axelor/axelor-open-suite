package com.axelor.apps.account.service.move.template;

import com.axelor.apps.account.db.Journal;
import com.axelor.apps.account.db.MoveTemplate;
import com.axelor.apps.account.db.MoveTemplateType;
import com.axelor.apps.account.db.repo.JournalRepository;
import com.axelor.apps.account.db.repo.MoveTemplateRepository;
import com.axelor.apps.account.db.repo.MoveTemplateTypeRepository;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.repo.CompanyRepository;
import com.axelor.apps.base.service.user.UserService;
import com.google.inject.Inject;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;

public class MoveTemplateGroupServiceImpl implements MoveTemplateGroupService {

  protected CompanyRepository companyRepository;
  protected UserService userService;
  protected MoveTemplateTypeRepository moveTemplateTypeRepository;
  protected MoveTemplateRepository moveTemplateRepository;

  @Inject
  public MoveTemplateGroupServiceImpl(
      CompanyRepository companyRepository,
      UserService userService,
      MoveTemplateTypeRepository moveTemplateTypeRepository,
      MoveTemplateRepository moveTemplateRepository) {
    this.companyRepository = companyRepository;
    this.userService = userService;
    this.moveTemplateTypeRepository = moveTemplateTypeRepository;
    this.moveTemplateRepository = moveTemplateRepository;
  }

  @Override
  public Map<String, Object> getOnNewValuesMap(
      Long companyId, Long moveTemplateTypeId, Long moveTemplateOriginId) throws AxelorException {
    Map<String, Object> valuesMap = new HashMap<>();

    MoveTemplateType moveTemplateType = moveTemplateTypeRepository.find(moveTemplateTypeId);
    Company company = getDefaultCompany(companyId);
    MoveTemplate moveTemplate = getDefaultMoveTemplate(moveTemplateOriginId, company);

    valuesMap.put("company", company);

    if (moveTemplate != null
        && moveTemplateType != null
        && moveTemplateType.getTypeSelect() != null) {
      valuesMap.put("moveTemplateType", moveTemplate.getMoveTemplateType());
      if (moveTemplateType.getTypeSelect() == MoveTemplateTypeRepository.TYPE_PERCENTAGE) {
        valuesMap.put("moveTemplate", moveTemplate);
      } else {
        valuesMap.put("moveDate", LocalDate.now());
        valuesMap.put("moveTemplateSet", new HashSet<>(Arrays.asList(moveTemplate)));
      }
      valuesMap.put("popup", true);
    }

    return valuesMap;
  }

  protected Company getDefaultCompany(Long companyId) {
    Company company = companyRepository.find(companyId);
    if (company != null) {
      return company;
    }

    company = userService.getUserActiveCompany();
    if (company != null) {
      return company;
    } else if (companyRepository.all().count() == 1) {
      return companyRepository.all().fetchOne();
    }

    return null;
  }

  protected MoveTemplate getDefaultMoveTemplate(Long moveTemplateOriginId, Company company) {
    if (moveTemplateOriginId == null || company == null) {
      return null;
    }

    MoveTemplate moveTemplate = moveTemplateRepository.find(moveTemplateOriginId);
    if (moveTemplate != null
        && moveTemplate.getIsValid()
        && (moveTemplate.getEndOfValidityDate() == null
            || moveTemplate.getEndOfValidityDate().isAfter(LocalDate.now()))) {
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
