package com.axelor.apps.bankpayment.service;

import com.axelor.apps.account.db.PaymentMode;
import com.axelor.apps.account.db.Reimbursement;
import com.axelor.apps.account.service.ReimbursementDomainServiceImpl;
import com.axelor.apps.bankpayment.service.bankdetails.BankDetailsBankPaymentService;
import com.axelor.apps.base.db.BankDetails;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import com.axelor.utils.helpers.StringHelper;
import com.google.inject.Inject;

import java.util.List;

public class ReimbursementDomainBankPaymentServiceImpl extends ReimbursementDomainServiceImpl {

    protected final BankDetailsBankPaymentService bankDetailsBankPaymentService;

    @Inject
    public ReimbursementDomainBankPaymentServiceImpl(BankDetailsBankPaymentService bankDetailsBankPaymentService) {
        this.bankDetailsBankPaymentService = bankDetailsBankPaymentService;
    }

    @Override
    public String createDomainForBankDetails(Reimbursement reimbursement) {
        Partner partner = reimbursement.getPartner();
        String domain = super.createDomainForBankDetails(reimbursement);

        PaymentMode paymentMode = reimbursement.getPartner().getInPaymentMode();
        Company company = reimbursement.getCompany();

        if (partner != null && !partner.getBankDetailsList().isEmpty()) {
            List<BankDetails> bankDetailsList =
                    bankDetailsBankPaymentService.getBankDetailsLinkedToActiveUmr(
                            paymentMode, partner, company);
            if (!bankDetailsList.isEmpty()) {
                domain = "self.id IN (" + StringHelper.getIdListString(bankDetailsList) + ")";
            }
        }
        return domain;
    }
}
