package com.axelor.apps.business.project.web;

import java.math.BigDecimal;
import java.util.Map;

import com.axelor.apps.base.db.PriceList;
import com.axelor.apps.base.db.PriceListLine;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.service.PriceListService;
import com.axelor.apps.businessproject.db.ElementsToInvoice;
import com.axelor.apps.project.db.ProjectTask;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Inject;

public class ElementsToInvoiceController {


	@Inject
	private PriceListService priceListService;

	public void getProductInformation(ActionRequest request, ActionResponse response){
		ElementsToInvoice elementToInvoice = request.getContext().asType(ElementsToInvoice.class);
		ProjectTask project = elementToInvoice.getProject();
		if(project == null){
			project = request.getContext().getParentContext().asType(ProjectTask.class);
		}
		Product product = elementToInvoice.getProduct();
		if(project != null && product != null){
			elementToInvoice.setCostPrice(product.getCostPrice());
			elementToInvoice.setUnit(product.getUnit());
			BigDecimal price = product.getSalePrice();
			if(project.getCustomer()!= null){
				PriceList priceList = project.getCustomer().getSalePriceList();
				if(priceList != null)  {
					PriceListLine priceListLine = priceListService.getPriceListLine(product, elementToInvoice.getQty(), priceList);

					Map<String, Object> discounts = priceListService.getDiscounts(priceList, priceListLine, price);
					price = priceListService.computeDiscount(price, (int) discounts.get("discountTypeSelect"), (BigDecimal) discounts.get("discountAmount"));
				}
			}
			elementToInvoice.setSalePrice(price);
		}
		response.setValues(elementToInvoice);
	}
}
