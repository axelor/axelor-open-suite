package com.axelor.apps.contract.batch;

import com.axelor.apps.account.service.batch.BatchStrategy;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Duration;
import com.axelor.apps.base.db.repo.DurationRepository;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.contract.db.Contract;
import com.axelor.apps.contract.db.repo.ContractRepository;
import com.axelor.apps.contract.db.repo.ContractVersionRepository;
import com.axelor.apps.contract.exception.ContractExceptionMessage;
import com.axelor.apps.contract.service.ContractRevaluationService;
import com.axelor.apps.contract.service.ContractService;
import com.axelor.apps.contract.service.ContractVersionService;
import com.axelor.apps.contract.translation.ITranslation;
import com.axelor.db.JPA;
import com.axelor.db.Query;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.lang.invoke.MethodHandles;
import java.time.LocalDate;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.script.ScriptException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BatchContractRevaluate extends BatchStrategy {

  protected ContractService contractService;
  protected ContractRepository contractRepository;
  protected ContractVersionService contractVersionService;
  protected ContractRevaluationService contractRevaluationService;
  private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  @Inject
  public BatchContractRevaluate(
      ContractService contractService,
      ContractRepository contractRepository,
      ContractVersionService contractVersionService,
      ContractRevaluationService contractRevaluationService) {
    this.contractService = contractService;
    this.contractRepository = contractRepository;
    this.contractVersionService = contractVersionService;
    this.contractRevaluationService = contractRevaluationService;
  }

  protected void process() {
    revaluateContracts();
  }

  protected void revaluateContracts() {
    List<Long> idsOk = new ArrayList<>();
    List<Long> idsFail = new ArrayList<>();
    List<Long> idsReevaluated = new ArrayList<>();
    idsOk.add(0L);
    idsFail.add(0L);
    Query<Contract> query =
        contractRepository
            .all()
            .filter(
                "self.isToRevaluate = true AND self.currentContractVersion.statusSelect = :onGoingStatus AND self.id NOT IN (:idsOk) AND self.id NOT IN (:idsFail)");
    List<Contract> contractList;
    while (!(contractList =
            query
                .bind("onGoingStatus", ContractVersionRepository.ONGOING_VERSION)
                .bind("idsOk", idsOk)
                .bind("idsFail", idsFail)
                .fetch(FETCH_LIMIT))
        .isEmpty()) {
      Map<String, List<Contract>> ids = revaluateContracts(contractList);
      idsOk.addAll(ids.get("OK").stream().map(Contract::getId).collect(Collectors.toList()));
      idsFail.addAll(ids.get("FAIL").stream().map(Contract::getId).collect(Collectors.toList()));
      idsReevaluated.addAll(
          ids.get("REEVALUATED").stream().map(Contract::getId).collect(Collectors.toList()));
      JPA.clear();
    }
    LOG.debug("{} Reevaluated contracts : {}", idsReevaluated.size(), idsReevaluated);
  }

  protected Map<String, List<Contract>> revaluateContracts(List<Contract> contractList) {
    List<Contract> idsFail = new ArrayList<>();
    List<Contract> idsOk = new ArrayList<>();
    List<Contract> idsReevaluated = new ArrayList<>();
    for (Contract contract : contractList) {
      processContract(idsFail, idsOk, idsReevaluated, contract);
    }
    return Stream.of(
            new AbstractMap.SimpleEntry<>("OK", idsOk),
            new AbstractMap.SimpleEntry<>("FAIL", idsFail),
            new AbstractMap.SimpleEntry<>("REEVALUATED", idsReevaluated))
        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
  }

  @Transactional(rollbackOn = Exception.class)
  protected void processContract(
      List<Contract> idsFail,
      List<Contract> idsOk,
      List<Contract> idsReevaluated,
      Contract contract) {
    try {
      contract = contractRepository.find(contract.getId());
      Contract newContract = contractService.getNextContract(contract);
      processContract(newContract, idsOk, idsReevaluated);
    } catch (Exception e) {
      TraceBackService.trace(e, null, batch.getId());
      idsFail.add(contract);
      incrementAnomaly();
    }
  }

  void processContract(Contract contract, List<Contract> idsOk, List<Contract> idsReevaluated)
      throws AxelorException, ScriptException {
    LocalDate todayDate = appBaseService.getTodayDate(contract.getCompany());
    LocalDate nextRevaluationDate = contract.getNextRevaluationDate();
    LocalDate lastRevaluationDate = contract.getLastRevaluationDate();

    if (launchBatch(todayDate, contract)) {
      contract = contractRevaluationService.applyFormula(contract);
      contract.setLastRevaluationDate(todayDate);
      contract.setNextRevaluationDate(computeNextRevaluationDate(contract, todayDate));
      idsReevaluated.add(contract);
      incrementDone();
    } else if (nextRevaluationDate == null && lastRevaluationDate != null) {
      contract.setNextRevaluationDate(
          computeNextRevaluationDate(contract, lastRevaluationDate).minusDays(5));
    } else if (nextRevaluationDate == null) {
      contract.setNextRevaluationDate(computeNextRevaluationDate(contract, null).minusDays(5));
    }
    idsOk.add(contract);
    contractRepository.save(contract);
  }

  protected boolean launchBatch(LocalDate todayDate, Contract contract) {
    LocalDate nextRevaluationDate = contract.getNextRevaluationDate();
    LocalDate lastRevaluationDate = contract.getLastRevaluationDate();
    LocalDate startDate = contract.getStartDate();
    Duration revaluationPeriod = contract.getRevaluationPeriod();
    LocalDate computedLastRevaluationDate =
        computeNewDateWithPeriod(revaluationPeriod, lastRevaluationDate);
    LocalDate computedStartDate = computeNewDateWithPeriod(revaluationPeriod, startDate);

    return nextRevaluationDate != null
        && (todayDate.isEqual(nextRevaluationDate)
            || (nextRevaluationDate.isBefore(todayDate)
                && ((computedLastRevaluationDate != null
                        && computedLastRevaluationDate.isBefore(nextRevaluationDate))
                    || (computedStartDate != null && computedStartDate.isBefore(todayDate)))));
  }

  protected LocalDate computeNewDateWithPeriod(Duration revaluationPeriod, LocalDate date) {
    if (date == null || revaluationPeriod == null) {
      return null;
    }

    int revaluationPeriodType = revaluationPeriod.getTypeSelect();
    Integer revaluationPeriodValue = revaluationPeriod.getValue();
    LocalDate computedDate = null;
    if (revaluationPeriodType == DurationRepository.TYPE_MONTH) {
      computedDate = date.plusMonths(revaluationPeriodValue);
    } else if (revaluationPeriodType == DurationRepository.TYPE_DAY) {
      computedDate = date.plusDays(revaluationPeriodValue);
    }
    return computedDate;
  }

  /**
   * Compute the next revaluation date for the given contract
   *
   * @param contract used to get the revaluation period.
   * @param date to add the period to. If null it will be the contract's start date or if null the
   *     contract's createdOn date.
   * @return the next revaluation date.
   * @throws AxelorException if there is no revaluation period or if the revaluation period is
   *     unknown.
   */
  protected LocalDate computeNextRevaluationDate(Contract contract, LocalDate date)
      throws AxelorException {
    if (date == null) {
      date = contract.getStartDate();
    }
    if (date == null) {
      date = contract.getCreatedOn().toLocalDate();
    }
    if (contract.getRevaluationPeriod() == null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_NO_VALUE,
          I18n.get(ContractExceptionMessage.CONTRACT_MISSING_REVALUATION_PERIOD));
    }
    switch (contract.getRevaluationPeriod().getTypeSelect()) {
      case (DurationRepository.TYPE_DAY):
        return date.plusDays(contract.getRevaluationPeriod().getValue());
      case (DurationRepository.TYPE_MONTH):
        return date.plusMonths(contract.getRevaluationPeriod().getValue());
      default:
        throw new AxelorException(
            TraceBackRepository.CATEGORY_NO_VALUE,
            I18n.get(ContractExceptionMessage.CONTRACT_MISSING_REVALUATION_PERIOD));
    }
  }

  @Override
  protected void stop() {
    super.stop();
    addComment(
        String.format(
            I18n.get(ITranslation.CONTRACT_BATCH_EXECUTION_RESULT),
            batch.getDone(),
            batch.getAnomaly()));
  }
}
