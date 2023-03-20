package com.axelor.apps.contract.batch;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.repo.BatchRepository;
import com.axelor.apps.base.service.administration.AbstractBatch;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.contract.db.Contract;
import com.axelor.apps.contract.db.ContractBatch;
import com.axelor.apps.contract.db.repo.AbstractContractRepository;
import com.axelor.apps.contract.db.repo.ContractBatchRepository;
import com.axelor.apps.contract.db.repo.ContractRepository;
import com.axelor.apps.contract.service.ContractService;
import com.axelor.apps.contract.translation.ITranslation;
import com.axelor.db.JPA;
import com.axelor.db.internal.DBHelper;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.sql.Array;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.naming.NamingException;

public class BatchContractInvoicing extends AbstractBatch {

  protected ContractRepository contractRepository;
  protected ContractService contractService;
  protected ContractBatchRepository contractBatchRepository;

  @Inject
  public BatchContractInvoicing(
      ContractRepository contractRepository,
      ContractService contractService,
      ContractBatchRepository contractBatchRepository) {
    this.contractRepository = contractRepository;
    this.contractService = contractService;
    this.contractBatchRepository = contractBatchRepository;
  }

  public List<List<Contract>> getIdsGroupedBy() {
    ContractBatch contractBatch = batch.getContractBatch();
    contractBatch = contractBatchRepository.find(contractBatch.getId());
    String query =
        "SELECT array_agg(self.id) as list "
            + "FROM contract_contract as self "
            + "WHERE self.is_invoicing_management IS TRUE "
            + "AND self.invoicing_date "
            + "<= '"
            + contractBatch.getInvoicingDate().toString()
            + "'::date "
            + "AND self.status_select != "
            + AbstractContractRepository.CLOSED_CONTRACT
            + " AND (SELECT automatic_invoicing FROM contract_contract_version WHERE contract_contract_version.id = self.current_contract_version) IS TRUE"
            + " AND self.id NOT IN (SELECT DISTINCT contract_set FROM account_invoice_contract_set)"
            + " GROUP BY self.invoiced_partner, self.invoicing_date, self.invoice_period_end_date, self.invoice_period_start_date, self.is_grouped_invoicing;";
    Array array;
    List<List<Contract>> contractListsList = new ArrayList<>();
    try {
      try (Connection connection = DBHelper.getConnection()) {
        ResultSet resultSet = connection.createStatement().executeQuery(query);
        while (resultSet.next()) {
          array = resultSet.getArray("list");
          contractListsList.add(findContracts(array));
        }
      }
    } catch (SQLException | NamingException e) {
      e.printStackTrace();
    }
    return contractListsList;
  }

  protected List<Contract> findContracts(Array array) throws SQLException {
    List<Contract> contractList = new ArrayList<>();
    if (array != null) {
      Long[] idList = (Long[]) array.getArray();
      for (Long id : idList) {
        Contract contract = contractRepository.find(id);
        contractList.add(contract);
      }
    }
    return contractList;
  }

  protected List<Contract> findContractsInList(List<Contract> contractList) {
    List<Contract> refindContractList = new ArrayList<>();
    for (Contract contract : contractList) {
      refindContractList.add(contractRepository.find(contract.getId()));
    }
    return refindContractList;
  }

  @Transactional
  public void invoiceContracts(List<Contract> contractList) throws AxelorException {
    if (contractList.size() == 1) {
      contractService.invoicingContract(contractList.get(0));
    }

    if (contractList.size() > 1) {
      contractService.invoicingContracts(contractList);
    }
  }

  @Override
  protected void process() {
    List<List<Contract>> contractListsList;
    while (!(contractListsList = getIdsGroupedBy()).isEmpty()) {
      for (List<Contract> contractList : contractListsList) {
        try {
          contractList = findContractsInList(contractList);
          invoiceContracts(contractList);
          incrementDone();
          JPA.clear();
        } catch (Exception e) {
          incrementAnomaly();
          TraceBackService.trace(e, "Contract invoicing batch", batch.getId());
        }
      }
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

  @Override
  protected void setBatchTypeSelect() {
    this.batch.setBatchTypeSelect(BatchRepository.BATCH_TYPE_CONTRACT_BATCH);
  }
}
