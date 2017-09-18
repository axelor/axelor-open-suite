package com.axelor.apps.contract.web;

import com.axelor.apps.account.db.TaxLine;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.service.tax.AccountManagementService;
import com.axelor.apps.contract.db.Contract;
import com.axelor.apps.contract.db.ContractLine;
import com.axelor.apps.contract.db.ContractVersion;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.db.EntityHelper;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.rpc.Context;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

public class ContractLineController {
	
	public void changeProduct(ActionRequest request, ActionResponse response) {
		ContractLine contractLine = request.getContext().asType(ContractLine.class);
		Contract contract = getContract(request.getContext());
		Product product = contractLine.getProduct();
		
		if(contractLine == null || product == null) {
			this.resetProductInformation(response);
			return;
		}

		try  {
			TaxLine taxLine = Beans.get(AccountManagementService.class).getTaxLine(
					LocalDate.now(), product, contract.getCompany(), contract.getPartner().getFiscalPosition(), false);
			response.setValue("taxLine", taxLine);

			BigDecimal price = product.getSalePrice();
			if(taxLine != null && product.getInAti()) {
				price = price.divide(taxLine.getValue().add(BigDecimal.ONE), 2, BigDecimal.ROUND_HALF_UP);
			}

			response.setValue("productName", product.getName());
			response.setValue("unit", product.getSalesUnit() == null ? product.getUnit() : product.getSalesUnit());
			response.setValue("price", price);

		}
		catch(Exception e)  {
			response.setFlash(e.getMessage());
			this.resetProductInformation(response);
		}
	}

	private Contract getContract(Context context) {
		Context contractContext = context.getParentContext();
		//ContractVersion contractVersion = versionContext.asType(ContractVersion.class);
		
		return contractContext.asType(Contract.class);

		/*if(versionContext.getParentContext() != null && versionContext.getParentContext().getContextClass().toString().equals(Contract.class.toString())){
			return EntityHelper.getEntity(versionContext.getParentContext().asType(Contract.class));
		}

		return contractVersion.getContractNext() != null ? contractVersion.getContractNext() : contractVersion.getContract();*/
	}

	private void resetProductInformation(ActionResponse response) {
		response.setValue("taxLine", null);
		response.setValue("productName", null);
		response.setValue("unit", null);
		response.setValue("price", null);
		response.setValue("exTaxTotal", null);
		response.setValue("inTaxTotal", null);
	}

	public void compute(ActionRequest request, ActionResponse response) {
		ContractLine contractLine = request.getContext().asType(ContractLine.class);
		Product product = contractLine.getProduct();

		if(contractLine == null || product == null) {
			this.resetProductInformation(response);
			return;
		}

		BigDecimal taxRate = BigDecimal.ZERO;
		if(contractLine.getTaxLine() != null)  {  taxRate = contractLine.getTaxLine().getValue();  }
		BigDecimal exTaxTotal = contractLine.getQty().multiply(contractLine.getPrice()).setScale(2, RoundingMode.HALF_EVEN);
		BigDecimal inTaxTotal = exTaxTotal.add(exTaxTotal.multiply(taxRate));

		response.setValue("exTaxTotal", exTaxTotal);
		response.setValue("inTaxTotal", inTaxTotal);
	}
}
