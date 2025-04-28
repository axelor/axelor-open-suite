/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2025 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.axelor.apps.contract.batch;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.contract.db.Contract;
import com.axelor.apps.contract.db.ContractBatch;
import com.axelor.apps.contract.db.repo.AbstractContractRepository;
import com.axelor.apps.contract.db.repo.ContractBatchRepository;
import com.axelor.apps.contract.db.repo.ContractRepository;
import com.axelor.apps.contract.service.ContractInvoicingService;
import com.axelor.apps.contract.translation.ITranslation;
import com.axelor.db.JPA;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import javax.persistence.Query;
import org.apache.commons.collections.CollectionUtils;

public class BatchContractInvoicing extends BatchStrategy {

  protected ContractRepository contractRepository;
  protected ContractInvoicingService contractInvoicingService;
  protected ContractBatchRepository contractBatchRepository;

  @Inject
  public BatchContractInvoicing(
      ContractRepository contractRepository,
      ContractInvoicingService contractInvoicingService,
      ContractBatchRepository contractBatchRepository) {
    this.contractRepository = contractRepository;
    this.contractInvoicingService = contractInvoicingService;
    this.contractBatchRepository = contractBatchRepository;
  }

  public List<List<Long>> getIdsGroupedBy() {
    ContractBatch contractBatch = batch.getContractBatch();
    contractBatch = contractBatchRepository.find(contractBatch.getId());
    String filter =
        "SELECT array_to_string(array_agg(self.id), ',') "
            + "FROM contract_contract as self "
            + "WHERE self.is_invoicing_management IS TRUE "
            + "AND self.invoicing_date <= :invoicingDate "
            + "AND self.status_select != :closedContract "
            + "AND (SELECT automatic_invoicing FROM contract_contract_version WHERE contract_contract_version.id = self.current_contract_version) IS TRUE "
            + "GROUP BY self.invoiced_partner, self.invoicing_date, self.invoice_period_end_date, self.invoice_period_start_date, self.is_grouped_invoicing";

    Query query =
        JPA.em()
            .createNativeQuery(filter)
            .setParameter("invoicingDate", contractBatch.getInvoicingDate())
            .setParameter("closedContract", AbstractContractRepository.CLOSED_CONTRACT);

    List<String> stringList = query.getResultList();
    return convertStringToLongList(stringList);
  }

  protected List<List<Long>> convertStringToLongList(List<String> stringList) {
    List<List<Long>> longList = new ArrayList<>();
    if (CollectionUtils.isNotEmpty(stringList)) {
      for (String list : stringList) {
        longList.add(
            Arrays.stream(list.split(","))
                .map(s -> Long.parseLong(s.trim()))
                .collect(Collectors.toList()));
      }
    }
    return longList;
  }

  protected List<Contract> findContractsInList(List<Long> contractList) {
    List<Contract> refindContractList = new ArrayList<>();
    for (Long id : contractList) {
      refindContractList.add(contractRepository.find(id));
    }
    return refindContractList;
  }

  @Transactional
  public void invoiceContracts(List<Contract> contractList) throws AxelorException {
    if (contractList.size() == 1) {
      contractInvoicingService.invoicingContract(contractList.get(0));
    }

    if (contractList.size() > 1) {
      contractInvoicingService.invoicingContracts(contractList);
    }
  }

  @Override
  protected void process() {
    int offset = 0;
    for (List<Long> idList : getIdsGroupedBy()) {
      ++offset;
      try {
        invoiceContracts(findContractsInList(idList));
        incrementDone();
      } catch (Exception e) {
        incrementAnomaly();
        TraceBackService.trace(e, "Contract invoicing batch", batch.getId());
      }
      if (offset % getFetchLimit() == 0) {
        JPA.clear();
        findBatch();
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
}
