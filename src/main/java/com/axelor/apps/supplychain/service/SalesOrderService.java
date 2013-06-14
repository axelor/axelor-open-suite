package com.axelor.apps.supplychain.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.joda.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.base.db.Unit;
import com.axelor.apps.base.db.UnitConversion;
import com.axelor.apps.base.service.CurrencyService;
import com.axelor.apps.base.service.UnitConversionService;
import com.axelor.apps.base.service.administration.GeneralService;
import com.axelor.apps.organisation.db.PlanningLine;
import com.axelor.apps.organisation.db.Task;
import com.axelor.apps.supplychain.db.SalesOrder;
import com.axelor.apps.supplychain.db.SalesOrderLine;
import com.axelor.apps.supplychain.db.SalesOrderLineVat;
import com.axelor.apps.supplychain.db.SalesOrderSubLine;
import com.axelor.exception.AxelorException;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class SalesOrderService {

	private static final Logger LOG = LoggerFactory.getLogger(SalesOrderService.class); 

	@Inject
	private SalesOrderLineService salesOrderLineService;
	
	@Inject
	private CurrencyService currencyService;
	
	@Inject
	private SalesOrderLineVatService salesOrderLineVatService;
	
	public SalesOrder _computeSalesOrderLines(SalesOrder salesOrder)  {
		
		if(salesOrder.getSalesOrderLineList() != null)  {
			for(SalesOrderLine salesOrderLine : salesOrder.getSalesOrderLineList())  {
				salesOrderLine.setExTaxTotal(salesOrderLineService.computeSalesOrderLine(salesOrderLine));
			}
		}
		
		return salesOrder;
	}


	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public void computeSalesOrder(SalesOrder salesOrder) throws AxelorException  {
		
		this.initSalesOrderLineVats(salesOrder);
		
		this._computeSalesOrderLines(salesOrder);
		
		this._populateSalesOrder(salesOrder);
		
		this._computeSalesOrder(salesOrder);
		
		salesOrder.save();
	}
	
	
	/**
	 * Peupler un devis.
	 * <p>
	 * Cette fonction permet de déterminer les tva d'un devis à partir des lignes de factures passées en paramètres. 
	 * </p>
	 * 
	 * @param invoice
	 * @param contractLine
	 * @param invoiceLines
	 * @param invoiceLineTaxes
	 * @param standard
	 * 
	 * @throws AxelorException
	 */
	public void _populateSalesOrder(SalesOrder salesOrder) throws AxelorException {
		
		LOG.debug("Peupler une facture => lignes de devis: {} ", new Object[] { salesOrder.getSalesOrderLineList().size() });
		
		// create Tva lines
		salesOrder.getSalesOrderLineVatList().addAll(salesOrderLineVatService.createsSalesOrderLineVat(salesOrder, salesOrder.getSalesOrderLineList()));
		
	}
	
	/**
	 * Calculer le montant d'une facture.
	 * <p> 
	 * Le calcul est basé sur les lignes de TVA préalablement créées.
	 * </p>
	 * 
	 * @param invoice
	 * @param vatLines
	 * @throws AxelorException 
	 */
	public void _computeSalesOrder(SalesOrder salesOrder) throws AxelorException {
		
		salesOrder.setExTaxTotal(BigDecimal.ZERO);
		salesOrder.setVatTotal(BigDecimal.ZERO);
		salesOrder.setInTaxTotal(BigDecimal.ZERO);
		
		for (SalesOrderLineVat salesOrderLineVat : salesOrder.getSalesOrderLineVatList()) {
			
			// Dans la devise de la comptabilité du tiers
			salesOrder.setExTaxTotal(salesOrder.getExTaxTotal().add( salesOrderLineVat.getExTaxBase() ));
			salesOrder.setVatTotal(salesOrder.getVatTotal().add( salesOrderLineVat.getVatTotal() ));
			salesOrder.setInTaxTotal(salesOrder.getInTaxTotal().add( salesOrderLineVat.getInTaxTotal() ));
			
		}
		
		salesOrder.setAmountRemainingToBeInvoiced(salesOrder.getInTaxTotal());
		
		LOG.debug("Montant de la facture: HTT = {},  HT = {}, TVA = {}, TTC = {}",
			new Object[] { salesOrder.getExTaxTotal(), salesOrder.getVatTotal(), salesOrder.getInTaxTotal() });
		
	}

	
	/**
	 * Permet de réinitialiser la liste des lignes de TVA
	 * @param salesOrder
	 * 			Un devis
	 */
	public void initSalesOrderLineVats(SalesOrder salesOrder) {
		
		if (salesOrder.getSalesOrderLineVatList() == null) { salesOrder.setSalesOrderLineVatList(new ArrayList<SalesOrderLineVat>()); }
		
		else { salesOrder.getSalesOrderLineVatList().clear(); }
		
	}

	public BigDecimal computeDuration(List<SalesOrderSubLine> SalesOrderSubLineList) {
		
		BigDecimal sum = new BigDecimal(0);
		
		for(SalesOrderSubLine salesOrderSubLine : SalesOrderSubLineList)  {
			
			sum = sum.add(salesOrderSubLine.getQty());
		}
		return sum;
	}
	
	public boolean checkSameUnitPlanningLineList(List<PlanningLine> planningLineList) {
		
		int iteration = 0;
		long id = -1;
		
		for(PlanningLine planningLine : planningLineList)  {
			
			if (iteration == 0 && id == -1) {
				id = planningLine.getUnit().getId();
				iteration++;
			}
			else {		
				if(id != planningLine.getUnit().getId()) {
					return false;
				}
			}
		}
		return true;
	}
	
	public void setUnitPlanningLineList(List<PlanningLine> planningLineList, Task task) {
		
		UnitConversionService ucs = new UnitConversionService();
		BigDecimal sum = new BigDecimal(0);
		boolean sameUnit = checkSameUnitPlanningLineList(planningLineList);
		Unit projectUnit = task.getProject().getProjectUnit();
		List<UnitConversion> unitConversionList = UnitConversion.all().fetch();
		LocalDateTime laterDate = task.getEndDateT();
		
		for(PlanningLine planningLine : planningLineList)  {
			
			if(sameUnit) {
				sum = sum.add(planningLine.getDuration());
			}
			else {
				BigDecimal qtyConverted = ucs.convert(unitConversionList, planningLine.getUnit(), projectUnit, planningLine.getDuration());
				sum = sum.add(qtyConverted);
			}
			
			if(laterDate == null || laterDate.compareTo(planningLine.getToDateTime()) < 0) {
				laterDate = planningLine.getToDateTime();
			}
		}
		
		if(sameUnit) {
			if(planningLineList.get(0) != null) {
				task.setTotalTaskUnit(planningLineList.get(0).getUnit());
			}
			task.setTotalTaskQty(sum);
		}
		else {
			task.setTotalTaskQty(sum);
			task.setTotalTaskUnit(projectUnit);
		}
		task.setEndDateT(laterDate);
	}

	@Transactional
	public void createTasks(SalesOrder salesOrder)  {
		
		if(salesOrder.getSalesOrderLineList() != null)  {
			for(SalesOrderLine salesOrderLine : salesOrder.getSalesOrderLineList())  {
				if(salesOrderLine.getHasToCreateTask()) {
					
					Task task = new Task();
					task.setProject(salesOrder.getProject());
					task.setSalesOrderLine(salesOrderLine);
					task.setName(salesOrderLine.getProductName());
					task.setDescription(salesOrderLine.getDescription());
					task.setStartDateT(new LocalDateTime(GeneralService.getTodayDateTime()));
					
					task.setIsTimesheetAffected(true);
					task.setIsToInvoice(salesOrderLine.getIsToInvoice());
					task.setInvoicingDate(salesOrderLine.getInvoicingDate());
					task.setAmountToInvoice(salesOrderLine.getAmountRemainingToBeInvoiced());
					task.setStatusSelect(1); // 1 = draft
					
					if(salesOrderLine.getTask() != null && salesOrderLine.getTask().getPlanningLineList() != null) {
						
						setUnitPlanningLineList(salesOrderLine.getTask().getPlanningLineList(), task);
					}
					else {
						task.setTotalTaskQty(salesOrderLine.getQty());
						task.setTotalTaskUnit(salesOrderLine.getUnit());
					}
						
					
					if(salesOrderLine.getSalesOrderSubLineList() != null) {
						
						task.setPlanningLineList(new ArrayList<PlanningLine>());
						BigDecimal duration = computeDuration(salesOrderLine.getSalesOrderSubLineList());
						
						for(SalesOrderSubLine salesOrderSubLine : salesOrderLine.getSalesOrderSubLineList())  {
							
							PlanningLine pl = new PlanningLine();
							
							pl.setTask(task);
							pl.setEmployee(salesOrderSubLine.getEmployee());
							pl.setProduct(salesOrderSubLine.getProduct());
							pl.setFromDateTime(new LocalDateTime(GeneralService.getTodayDateTime()));
							pl.setDuration(duration);
							pl.setUnit(salesOrderSubLine.getUnit());
							pl.setToDateTime(pl.getFromDateTime().plusDays(duration.intValue()));
							
							task.getPlanningLineList().add(pl);
							pl.save();
						}
					}
					task.save();
				}
			}
		}		
	}
}


