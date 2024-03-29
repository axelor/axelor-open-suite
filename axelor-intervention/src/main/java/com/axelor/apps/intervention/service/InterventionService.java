package com.axelor.apps.intervention.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Duration;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.contract.db.Contract;
import com.axelor.apps.intervention.db.CustomerRequest;
import com.axelor.apps.intervention.db.Intervention;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.auth.db.User;
import com.axelor.rpc.ActionResponse;
import java.time.LocalDateTime;

public interface InterventionService {
  Intervention create(CustomerRequest request) throws AxelorException;

  Intervention create(Contract contract) throws AxelorException;

  void fillFromContract(Intervention intervention, Contract contract);

  void fillFromRequest(Intervention intervention, CustomerRequest request);

  void start(Intervention intervention, LocalDateTime dateTime);

  void reschedule(Intervention intervention) throws AxelorException;

  void cancel(Intervention intervention);

  void suspend(Intervention intervention, LocalDateTime dateTime);

  LocalDateTime computeEstimatedEndDateTime(
      LocalDateTime planificationDateTime, Duration plannedInterventionDuration);

  void plan(
      Intervention intervention,
      ActionResponse response,
      User technicianUser,
      LocalDateTime planificationDateTime,
      LocalDateTime estimatedEndDateTime)
      throws AxelorException;

  void computeTag(Long interventionId);

  void finish(Intervention intervention, LocalDateTime dateTime) throws AxelorException;

  SaleOrder generateSaleOrder(Intervention intervention) throws AxelorException;

  Partner getDefaultInvoicedPartner(Intervention intervention);
}
