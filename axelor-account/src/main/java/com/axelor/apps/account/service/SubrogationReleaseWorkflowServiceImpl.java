package com.axelor.apps.account.service;

import com.axelor.apps.account.db.SubrogationRelease;
import com.axelor.apps.account.db.repo.SubrogationReleaseRepository;
import com.axelor.apps.account.exception.IExceptionMessage;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;
import com.axelor.i18n.I18n;
import com.google.inject.persist.Transactional;
import java.util.ArrayList;
import java.util.List;

public class SubrogationReleaseWorkflowServiceImpl implements SubrogationReleaseWorkflowService {
  @Transactional(rollbackOn = {AxelorException.class})
  @Override
  public void goBackToAccounted(SubrogationRelease subrogationRelease) throws AxelorException {
    List<Integer> authorizedStatus = new ArrayList<>();
    authorizedStatus.add(SubrogationReleaseRepository.STATUS_CLEARED);
    authorizedStatus.add(SubrogationReleaseRepository.STATUS_CANCELED);
    if (subrogationRelease.getStatusSelect() == null
        || !authorizedStatus.contains(subrogationRelease.getStatusSelect())) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(IExceptionMessage.SUBROGATION_RELEASE_BACK_TO_ACCOUNTED_WRONG_STATUS));
    }
    subrogationRelease.setStatusSelect(SubrogationReleaseRepository.STATUS_ACCOUNTED);
  }
}
