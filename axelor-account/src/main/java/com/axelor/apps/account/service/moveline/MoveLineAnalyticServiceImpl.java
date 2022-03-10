package com.axelor.apps.account.service.moveline;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

import com.axelor.apps.account.db.AnalyticAccount;
import com.axelor.apps.account.db.AnalyticAxisByCompany;
import com.axelor.apps.account.db.AnalyticMoveLine;
import com.axelor.apps.account.db.InvoiceLine;
import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.service.analytic.AnalyticToolService;
import com.axelor.apps.account.service.app.AppAccountService;
import com.axelor.apps.account.service.config.AccountConfigService;
import com.axelor.apps.base.db.Company;
import com.axelor.exception.AxelorException;
import com.axelor.inject.Beans;
import com.google.inject.Inject;

public class MoveLineAnalyticServiceImpl implements MoveLineAnalyticService{

	protected AnalyticToolService analyticToolService;
	protected AccountConfigService accountConfigService;
	protected AppAccountService appAccountService;
	private final int RETURN_SCALE = 2;
	
	@Inject
	public MoveLineAnalyticServiceImpl(AnalyticToolService analyticToolService, AccountConfigService accountConfigService, AppAccountService appAccountService) {
		this.analyticToolService = analyticToolService;
		this.accountConfigService = accountConfigService;
		this.appAccountService = appAccountService;
	}
	
	  @Override
	  public MoveLine clearAnalyticAccounting(MoveLine moveLine) {
	    moveLine.setAxis1AnalyticAccount(null);
	    moveLine.setAxis2AnalyticAccount(null);
	    moveLine.setAxis3AnalyticAccount(null);
	    moveLine.setAxis4AnalyticAccount(null);
	    moveLine.setAxis5AnalyticAccount(null);
	    moveLine
	        .getAnalyticMoveLineList()
	        .forEach(analyticMoveLine -> analyticMoveLine.setMoveLine(null));
	    moveLine.getAnalyticMoveLineList().clear();
	    return moveLine;
	  }

	  @Override
	  public MoveLine printAnalyticAccount(MoveLine moveLine, Company company) throws AxelorException {
	    if (moveLine.getAnalyticMoveLineList() != null
	        && !moveLine.getAnalyticMoveLineList().isEmpty()
	        && company != null) {
	      List<AnalyticMoveLine> analyticMoveLineList = new ArrayList();
	      for (AnalyticAxisByCompany analyticAxisByCompany :
	          accountConfigService.getAccountConfig(company).getAnalyticAxisByCompanyList()) {
	        for (AnalyticMoveLine analyticMoveLine : moveLine.getAnalyticMoveLineList()) {
	          if (analyticMoveLine.getAnalyticAxis().equals(analyticAxisByCompany.getAnalyticAxis())) {
	            analyticMoveLineList.add(analyticMoveLine);
	          }
	        }
	        AnalyticMoveLine analyticMoveLine = analyticMoveLineList.get(0);
	        if (analyticMoveLineList.size() == 1
	            && analyticMoveLine.getPercentage().compareTo(new BigDecimal(100)) == 0) {
	        	AnalyticAccount analyticAccount = analyticMoveLine.getAnalyticAccount();
	          switch (analyticAxisByCompany.getOrderSelect()) {
	            case 1:
	              moveLine.setAxis1AnalyticAccount(analyticAccount);
	              break;
	            case 2:
	              moveLine.setAxis2AnalyticAccount(analyticAccount);
	              break;
	            case 3:
	              moveLine.setAxis3AnalyticAccount(analyticAccount);
	              break;
	            case 4:
	              moveLine.setAxis4AnalyticAccount(analyticAccount);
	              break;
	            case 5:
	              moveLine.setAxis5AnalyticAccount(analyticAccount);
	              break;
	            default:
	              break;
	          }
	        }
	        analyticMoveLineList.clear();
	      }
	    }
	    return moveLine;
	  }

	  @Override
	  public MoveLine checkAnalyticMoveLineForAxis(MoveLine moveLine) {
		  if (analyticToolService.isAnalyticAxisFilled(moveLine.getAxis1AnalyticAccount(),moveLine.getAnalyticMoveLineList())) {
			  moveLine.setAxis1AnalyticAccount(null);
		  }
		  if (analyticToolService.isAnalyticAxisFilled(moveLine.getAxis2AnalyticAccount(),moveLine.getAnalyticMoveLineList())) {
			  moveLine.setAxis2AnalyticAccount(null);
		  }
		  if (analyticToolService.isAnalyticAxisFilled(moveLine.getAxis3AnalyticAccount(),moveLine.getAnalyticMoveLineList())) {
			  moveLine.setAxis3AnalyticAccount(null);
		  }
		  if (analyticToolService.isAnalyticAxisFilled(moveLine.getAxis4AnalyticAccount(),moveLine.getAnalyticMoveLineList())) {
			  moveLine.setAxis4AnalyticAccount(null);
		  }
		  if (analyticToolService.isAnalyticAxisFilled(moveLine.getAxis5AnalyticAccount(),moveLine.getAnalyticMoveLineList())) {
			  moveLine.setAxis5AnalyticAccount(null);
		  }
	    return moveLine;
	  }

	  @Override
	  public BigDecimal getAnalyticAmount(MoveLine moveLine, AnalyticMoveLine analyticMoveLine) {
	    if (moveLine.getCredit().compareTo(BigDecimal.ZERO) > 0) {
	      return analyticMoveLine
	          .getPercentage()
	          .multiply(moveLine.getCredit())
	          .divide(new BigDecimal(100), RETURN_SCALE, RoundingMode.HALF_UP);
	    } else if (moveLine.getDebit().compareTo(BigDecimal.ZERO) > 0) {
	      return analyticMoveLine
	          .getPercentage()
	          .multiply(moveLine.getDebit())
	          .divide(new BigDecimal(100), RETURN_SCALE, RoundingMode.HALF_UP);
	    }
	    return BigDecimal.ZERO;
	  }

	  @Override
	  public boolean checkManageAnalytic(Move move) throws AxelorException {
	    return move != null
	        && move.getCompany() != null
	        && appAccountService.getAppAccount().getManageAnalyticAccounting()
	        && accountConfigService.getAccountConfig(move.getCompany()).getManageAnalyticAccounting();
	  }
	  
	  @Override
	  public boolean isAxisRequired(MoveLine moveLine, Move move, int position) throws AxelorException {
		  if (move != null
		          && checkManageAnalytic(move)
		          && moveLine != null
		          && moveLine.getAccount() != null
		          && moveLine.getAccount().getCompany() != null) {
		        Integer nbrAxis = accountConfigService
		                .getAccountConfig(moveLine.getAccount().getCompany())
		                .getNbrOfAnalyticAxisSelect();
		             return moveLine.getAccount() != null
		                  && moveLine.getAccount().getAnalyticDistributionAuthorized()
		                  && moveLine.getAccount().getAnalyticDistributionRequiredOnMoveLines()
		                  && moveLine.getAnalyticDistributionTemplate() == null
		                  && (position <= nbrAxis);
		        
		      }
		  return false;
	  }
	
}
