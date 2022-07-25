package com.axelor.apps.account.service.moveline;

import java.util.List;
import java.util.Objects;

import com.axelor.apps.account.db.Account;
import com.axelor.apps.account.db.AnalyticMoveLine;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.base.db.Partner;

public class MoveLineCompletionServiceImpl implements MoveLineCompletionService{

	@Override
	public void completeAnalyticMoveLines(List<MoveLine> moveLines) {
		Objects.requireNonNull(moveLines);
        for (MoveLine moveLine : moveLines) {
            List<AnalyticMoveLine> analyticMoveLineList = moveLine.getAnalyticMoveLineList();
            if (analyticMoveLineList != null) {
              for (AnalyticMoveLine analyticMoveLine : analyticMoveLineList) {
                analyticMoveLine.setAccount(moveLine.getAccount());
                analyticMoveLine.setAccountType(moveLine.getAccount().getAccountType());
              }
            }
          }
	}
	

	@Override
	public void freezeAccountAndPartnerFields(MoveLine moveLine) {
		Account account = moveLine.getAccount();

		moveLine.setAccountId(account.getId());
		moveLine.setAccountCode(account.getCode());
		moveLine.setAccountName(account.getName());
		moveLine.setServiceType(account.getServiceType());
		moveLine.setServiceTypeCode(
		    account.getServiceType() != null ? account.getServiceType().getCode() : null);

		Partner partner = moveLine.getPartner();

		if (partner != null) {
		  moveLine.setPartnerId(partner.getId());
		  moveLine.setPartnerFullName(partner.getFullName());
		  moveLine.setPartnerSeq(partner.getPartnerSeq());
		  moveLine.setDas2Activity(partner.getDas2Activity());
		  moveLine.setDas2ActivityName(
		      partner.getDas2Activity() != null ? partner.getDas2Activity().getName() : null);
		}
		if (moveLine.getTaxLine() != null) {
		  moveLine.setTaxRate(moveLine.getTaxLine().getValue());
		  moveLine.setTaxCode(moveLine.getTaxLine().getTax().getCode());
		}
	}


}
