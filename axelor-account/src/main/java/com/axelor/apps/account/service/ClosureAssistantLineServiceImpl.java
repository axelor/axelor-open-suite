package com.axelor.apps.account.service;

import com.axelor.apps.account.db.ClosureAssistant;
import com.axelor.apps.account.db.ClosureAssistantLine;
import com.axelor.apps.account.db.repo.ClosureAssistantLineRepository;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.auth.AuthUtils;
import com.axelor.common.ObjectUtils;
import com.axelor.db.JPA;
import com.axelor.exception.AxelorException;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.TypedQuery;

public class ClosureAssistantLineServiceImpl implements ClosureAssistantLineService {

  protected ClosureAssistantService closureAssistantService;
  protected ClosureAssistantLineRepository closureAssistantLineRepository;
  protected AppBaseService appBaseService;

  @Inject
  public ClosureAssistantLineServiceImpl(
      ClosureAssistantService closureAssistantService,
      ClosureAssistantLineRepository closureAssistantLineRepository,
      AppBaseService appBaseService) {
    this.closureAssistantLineRepository = closureAssistantLineRepository;
    this.closureAssistantService = closureAssistantService;
    this.appBaseService = appBaseService;
  }

  @Override
  public List<ClosureAssistantLine> initClosureAssistantLines(ClosureAssistant closureAssistant)
      throws AxelorException {
    List<ClosureAssistantLine> closureAssistantLineList = new ArrayList<ClosureAssistantLine>();
    for (int i = 2; i < 8; i++) {
      ClosureAssistantLine closureAssistantLine = new ClosureAssistantLine(i - 1, null, i, false);

      if (i != 2) {
        closureAssistantLine.setIsPreviousLineValidated(false);
      } else {
        closureAssistantLine.setIsPreviousLineValidated(true);
      }
      closureAssistantLine.setIsNextLineValidated(false);
      closureAssistantLineList.add(closureAssistantLine);
    }
    return closureAssistantLineList;
  }

  @Override
  public void cancelClosureAssistantLine(ClosureAssistantLine closureAssistantLine)
      throws AxelorException {
    this.validateOrCancelClosureAssistantLine(closureAssistantLine, false);
  }

  @Override
  public void validateClosureAssistantLine(ClosureAssistantLine closureAssistantLine)
      throws AxelorException {
    this.validateOrCancelClosureAssistantLine(closureAssistantLine, true);
  }

  @Transactional
  protected ClosureAssistantLine validateOrCancelClosureAssistantLine(
      ClosureAssistantLine closureAssistantLine, boolean isValidated) throws AxelorException {
    closureAssistantLine.setIsValidated(isValidated);
    if (isValidated) {
      closureAssistantLine.setValidatedByUser(AuthUtils.getUser());
      closureAssistantLine.setValidatedOnDate(
          appBaseService.getTodayDate(closureAssistantLine.getClosureAssistant().getCompany()));
    } else {
      closureAssistantLine.setValidatedByUser(null);
      closureAssistantLine.setValidatedOnDate(null);
    }
    setIsPreviousLineValidatedForPreviousAndNextLine(closureAssistantLine, isValidated);
    closureAssistantLineRepository.save(closureAssistantLine);
    closureAssistantService.updateClosureAssistantProgress(
        closureAssistantLine.getClosureAssistant());
    return closureAssistantLine;
  }

  @Transactional
  protected void setIsPreviousLineValidatedForPreviousAndNextLine(
      ClosureAssistantLine closureAssistantLine, boolean isValidated) {
    ClosureAssistantLine previousClosureAssistantLine = null;
    ClosureAssistantLine nextClosureAssistantLine = null;

    TypedQuery<ClosureAssistantLine> closureAssistantLineQuery =
        JPA.em()
            .createQuery(
                "SELECT self FROM ClosureAssistantLine self  "
                    + "WHERE self.closureAssistant = :closureAssistant AND self.sequence = :sequence ",
                ClosureAssistantLine.class);

    closureAssistantLineQuery.setParameter(
        "closureAssistant", closureAssistantLine.getClosureAssistant());
    closureAssistantLineQuery.setParameter("sequence", closureAssistantLine.getSequence() - 1);

    List<ClosureAssistantLine> previousClosureAssistantLineList =
        closureAssistantLineQuery.getResultList();

    if (!ObjectUtils.isEmpty(previousClosureAssistantLineList)) {
      previousClosureAssistantLine = previousClosureAssistantLineList.get(0);
    }

    closureAssistantLineQuery.setParameter("sequence", closureAssistantLine.getSequence() + 1);

    List<ClosureAssistantLine> nextClosureAssistantLineList =
        closureAssistantLineQuery.getResultList();
    if (!ObjectUtils.isEmpty(nextClosureAssistantLineList)) {
      nextClosureAssistantLine = nextClosureAssistantLineList.get(0);
    }

    if (previousClosureAssistantLine != null) {
      previousClosureAssistantLine.setIsPreviousLineValidated(!isValidated);
      previousClosureAssistantLine.setIsNextLineValidated(isValidated);
      closureAssistantLineRepository.save(previousClosureAssistantLine);
    }
    if (nextClosureAssistantLine != null) {
      nextClosureAssistantLine.setIsPreviousLineValidated(isValidated);
      closureAssistantLineRepository.save(nextClosureAssistantLine);
    }
  }
}
