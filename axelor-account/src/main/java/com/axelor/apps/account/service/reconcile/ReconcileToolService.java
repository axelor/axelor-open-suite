package com.axelor.apps.account.service.reconcile;

import com.axelor.apps.account.db.Reconcile;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Partner;
import java.util.List;

public interface ReconcileToolService {

  void updatePartnerAccountingSituation(Reconcile reconcile) throws AxelorException;

  List<Partner> getPartners(Reconcile reconcile);

  void updateInvoiceCompanyInTaxTotalRemaining(Reconcile reconcile) throws AxelorException;

  void updateInvoiceTermsAmountRemaining(Reconcile reconcile) throws AxelorException;
}
