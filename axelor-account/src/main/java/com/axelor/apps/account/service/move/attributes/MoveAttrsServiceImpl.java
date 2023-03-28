package com.axelor.apps.account.service.move.attributes;

import com.axelor.apps.account.db.Journal;
import com.axelor.apps.account.db.JournalType;
import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.repo.JournalTypeRepository;
import com.axelor.apps.account.db.repo.MoveRepository;
import com.axelor.apps.base.service.app.AppBaseService;
import com.google.inject.Inject;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class MoveAttrsServiceImpl implements MoveAttrsService {

  protected AppBaseService appBaseService;

  @Inject
  public MoveAttrsServiceImpl(AppBaseService appBaseService) {
    this.appBaseService = appBaseService;
  }

  @Override
  public Map<String, Map<String, Object>> getHiddenAttributeValues(Move move) {
    Objects.requireNonNull(move);
    Map<String, Map<String, Object>> mapResult = new HashMap<>();

    mapResult.put("moveLineList.counter", new HashMap<>());
    mapResult.put("moveLineList.amountRemaining", new HashMap<>());
    mapResult.put("moveLineList.reconcileGroup", new HashMap<>());
    mapResult.put("moveLineList.partner", new HashMap<>());

    mapResult.get("moveLineList.partner").put("hidden", move.getPartner() != null);
    mapResult
        .get("moveLineList.counter")
        .put(
            "hidden",
            move.getStatusSelect() == null || move.getStatusSelect() == MoveRepository.STATUS_NEW);
    mapResult
        .get("moveLineList.amountRemaining")
        .put(
            "hidden",
            move.getStatusSelect() == null
                || move.getStatusSelect() == MoveRepository.STATUS_NEW
                || move.getStatusSelect() == MoveRepository.STATUS_CANCELED
                || move.getStatusSelect() == MoveRepository.STATUS_SIMULATED);
    mapResult
        .get("moveLineList.reconcileGroup")
        .put(
            "hidden",
            move.getStatusSelect() == MoveRepository.STATUS_NEW
                || move.getStatusSelect() == MoveRepository.STATUS_CANCELED);
    return mapResult;
  }

  @Override
  public Map<String, Map<String, Object>> getFunctionalOriginSelectDomain(Move move) {
    Objects.requireNonNull(move);
    Map<String, Map<String, Object>> mapResult = new HashMap<>();
    mapResult.put("functionalOriginSelect", new HashMap<>());

    String selectionValue = null;

    if (move.getJournal() != null) {
      selectionValue =
          Optional.ofNullable(move.getJournal().getAuthorizedFunctionalOriginSelect()).orElse("0");
    }
    mapResult.get("functionalOriginSelect").put("selection-in", selectionValue);

    return mapResult;
  }

  @Override
  public Map<String, Map<String, Object>> getBankDetailsAttributes(Move move) {
    Objects.requireNonNull(move);

    Map<String, Map<String, Object>> mapResult = new HashMap<>();

    Integer technicalTypeSelect =
        Optional.ofNullable(move.getJournal())
            .map(Journal::getJournalType)
            .map(JournalType::getTechnicalTypeSelect)
            .orElse(null);
    boolean technicalTypeSelectRequired = false;
    if (technicalTypeSelect == JournalTypeRepository.TECHNICAL_TYPE_SELECT_EXPENSE
        || technicalTypeSelect == JournalTypeRepository.TECHNICAL_TYPE_SELECT_SALE) {
      technicalTypeSelectRequired = true;
    }

    mapResult.put("companyBankDetails", new HashMap<>());
    mapResult
        .get("companyBankDetails")
        .put("hidden", !appBaseService.getAppBase().getManageMultiBanks());
    mapResult
        .get("companyBankDetails")
        .put(
            "required",
            appBaseService.getAppBase().getManageMultiBanks() && technicalTypeSelectRequired);
    return null;
  }
}
