package com.axelor.apps.account.service;

import com.axelor.apps.account.db.Reimbursement;

public interface ReimbursementDomainService {
    String createDomainForBankDetails(Reimbursement reimbursement);
}
