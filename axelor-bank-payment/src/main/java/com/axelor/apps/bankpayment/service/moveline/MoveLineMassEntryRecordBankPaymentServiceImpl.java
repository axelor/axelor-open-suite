package com.axelor.apps.bankpayment.service.moveline;

import com.axelor.apps.account.db.MoveLineMassEntry;
import com.axelor.apps.account.db.PaymentMode;
import com.axelor.apps.account.db.repo.AnalyticMoveLineRepository;
import com.axelor.apps.account.service.move.MoveLoadDefaultConfigService;
import com.axelor.apps.account.service.move.massentry.MassEntryMoveCreateService;
import com.axelor.apps.account.service.moveline.MoveLineRecordService;
import com.axelor.apps.account.service.moveline.MoveLineTaxService;
import com.axelor.apps.account.service.moveline.MoveLineToolService;
import com.axelor.apps.account.service.moveline.massentry.MoveLineMassEntryRecordServiceImpl;
import com.axelor.apps.account.service.moveline.massentry.MoveLineMassEntryService;
import com.axelor.apps.account.util.TaxAccountToolService;
import com.axelor.apps.bankpayment.service.bankdetails.BankDetailsBankPaymentService;
import com.axelor.apps.base.db.BankDetails;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import com.google.inject.Inject;

import java.util.List;

public class MoveLineMassEntryRecordBankPaymentServiceImpl extends MoveLineMassEntryRecordServiceImpl {
    protected final BankDetailsBankPaymentService bankDetailsBankPaymentService;
    @Inject
    public MoveLineMassEntryRecordBankPaymentServiceImpl(MoveLineMassEntryService moveLineMassEntryService, MoveLineRecordService moveLineRecordService, TaxAccountToolService taxAccountToolService, MoveLoadDefaultConfigService moveLoadDefaultConfigService, MassEntryMoveCreateService massEntryMoveCreateService, MoveLineTaxService moveLineTaxService, AnalyticMoveLineRepository analyticMoveLineRepository, MoveLineToolService moveLineToolService, BankDetailsBankPaymentService bankDetailsBankPaymentService) {
        super(moveLineMassEntryService, moveLineRecordService, taxAccountToolService, moveLoadDefaultConfigService, massEntryMoveCreateService, moveLineTaxService, analyticMoveLineRepository, moveLineToolService);
        this.bankDetailsBankPaymentService = bankDetailsBankPaymentService;
    }

    @Override
    public void setMovePartnerBankDetails(MoveLineMassEntry moveLine) {

        PaymentMode paymentMode = moveLine.getMovePaymentMode();
        Partner partner = moveLine.getPartner();
        Company company = moveLine.getMoveMassEntry().getCompany();

        List<BankDetails> bankDetailsList = bankDetailsBankPaymentService
                .getBankDetailsLinkedToActiveUmr(paymentMode, partner, company);

        BankDetails selectedBankDetails = (bankDetailsList != null && !bankDetailsList.isEmpty())
                ? bankDetailsList.get(0)
                : partner.getBankDetailsList().stream()
                .filter(bankDetails -> Boolean.TRUE.equals(bankDetails.getIsDefault()) && Boolean.TRUE.equals(bankDetails.getActive()))
                .findFirst()
                .orElse(null);

        moveLine.setMovePartnerBankDetails(selectedBankDetails);
    }
}
