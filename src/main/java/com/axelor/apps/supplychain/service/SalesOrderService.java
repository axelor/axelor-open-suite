package com.axelor.apps.supplychain.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.base.db.IProduct;
import com.axelor.apps.base.db.Unit;
import com.axelor.apps.base.db.UnitConversion;
import com.axelor.apps.base.service.CurrencyService;
import com.axelor.apps.base.service.UnitConversionService;
import com.axelor.apps.base.service.administration.GeneralService;
import com.axelor.apps.base.service.administration.SequenceService;
import com.axelor.apps.organisation.db.PlanningLine;
import com.axelor.apps.organisation.db.Task;
import com.axelor.apps.supplychain.db.ISalesOrder;
import com.axelor.apps.supplychain.db.IStockMove;
import com.axelor.apps.supplychain.db.SalesOrder;
import com.axelor.apps.supplychain.db.SalesOrderLine;
import com.axelor.apps.supplychain.db.SalesOrderLineVat;
import com.axelor.apps.supplychain.db.SalesOrderSubLine;
import com.axelor.apps.supplychain.db.StockMove;
import com.axelor.apps.supplychain.db.StockMoveLine;
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
	
	@Inject
	SequenceService sequenceService;
	
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
	 * Cette fonction permet de déterminer les tva d'un devis. 
	 * </p>
	 * 
	 * @param salesOrder
	 * 
	 * @throws AxelorException
	 */
	public void _populateSalesOrder(SalesOrder salesOrder) throws AxelorException {
		
		LOG.debug("Peupler un devis => lignes de devis: {} ", new Object[] { salesOrder.getSalesOrderLineList().size() });
		
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

	/**
	 * Permet de faire la somme des durées des sous-lignes du salesOrderLine.
	 * @param SalesOrderSubLineList Liste des sous-lignes du salesOrderLine
	 * @return La somme des durées
	 */
	public BigDecimal computeDuration(List<SalesOrderSubLine> SalesOrderSubLineList) {
		
		BigDecimal sum = BigDecimal.ZERO;
		
		for(SalesOrderSubLine salesOrderSubLine : SalesOrderSubLineList)  {
			
			sum = sum.add(salesOrderSubLine.getQty());
		}
		return sum;
	}
	
	/**
	 * Permet de vérifier si l'objet Unit est le même pour toutes les lignes de la liste planningLineList.
	 * @param planningLineList La liste de PlanningLine
	 * @return Vrai si les lignes de planning ont le même Unit, faux sinon 
	 */
	public boolean checkSameUnitPlanningLineList(List<PlanningLine> planningLineList) {
		
		int iteration = 0;
		long id = -1;
		
		for(PlanningLine planningLine : planningLineList)  {
			/* Save the first unit of the planning line list */
			if (iteration == 0 && id == -1) {
				id = planningLine.getUnit().getId();
				iteration++;
			}
			else {		
				/* Compare the unit saved with the others unit of the list */
				if(id != planningLine.getUnit().getId()) {
					return false;
				}
			}
		}
		return true;
	}
	
	/**
	 * Méthode permettant de calculer la somme des durées de la liste de planning et 
	 * d'assigner la quantité, l'unité, et la date de fin à la tâche courante.
	 * @param planningLineList La liste des lignes de planning
	 * @param task La tâche courante
	 * @throws AxelorException Les unités demandés ne se trouvent pas dans la liste de conversion
	 */
	public void setUnitPlanningLineList(List<PlanningLine> planningLineList, Task task) throws AxelorException {
		
		UnitConversionService ucs = new UnitConversionService();
		BigDecimal sum = new BigDecimal(0);
		boolean sameUnit = checkSameUnitPlanningLineList(planningLineList);
		Unit projectUnit = task.getProject().getProjectUnit();
		List<UnitConversion> unitConversionList = UnitConversion.all().fetch();
		LocalDateTime laterDate = task.getEndDateT();
		
		for(PlanningLine planningLine : planningLineList)  {
			/* If all lines of the planningLineList have the same unit we do the sum of the duration of all planning lines */
			if(sameUnit) {
				sum = sum.add(planningLine.getDuration());
			}
			else {
				/* Call the convert method of the UnitConversionService object to convert to the right unit */
				BigDecimal qtyConverted = ucs.convert(unitConversionList, planningLine.getUnit(), projectUnit, planningLine.getDuration());
				sum = sum.add(qtyConverted);
			}

			if(laterDate == null || laterDate.compareTo(planningLine.getToDateTime()) < 0) {
				laterDate = planningLine.getToDateTime();
			}
		}
		/* If sameUnit is true so all lines of the planning line list have the same unit */
		if(sameUnit) {
			/* Set the unit to the task by taking the unit of the first element of the planning line list */
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

	/**
	 * Méthode permettant de créer une tâche.
	 * @param salesOrder L'object SaleOrder courant
	 * @throws AxelorException Les unités demandés ne se trouvent pas dans la liste de conversion
	 */
	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public void createTasks(SalesOrder salesOrder) throws AxelorException  {
		
		if(salesOrder.getSalesOrderLineList() != null)  {
			/* Loop of the salesOrderLineList */
			for(SalesOrderLine salesOrderLine : salesOrder.getSalesOrderLineList())  {
				if(salesOrderLine.getHasToCreateTask()) {
					/* Create a new Task */
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
					task.setStatusSelect(ISalesOrder.DRAFT); // 1 = draft
					/* Check if there is a planning line list in the salesOrderLine task */
					if(salesOrderLine.getTask() != null && salesOrderLine.getTask().getPlanningLineList() != null) {
						/* Call the method to set the unit of the task */
						setUnitPlanningLineList(salesOrderLine.getTask().getPlanningLineList(), task);
					}
					else {
						task.setTotalTaskQty(salesOrderLine.getQty());
						task.setTotalTaskUnit(salesOrderLine.getUnit());
					}
						
					/* If the subline list of the salesOrderLine is not empty */
					if(salesOrderLine.getSalesOrderSubLineList() != null) {
						
						task.setPlanningLineList(new ArrayList<PlanningLine>());
						BigDecimal duration = computeDuration(salesOrderLine.getSalesOrderSubLineList());
						/* Loop of the salesOrderSubLineList */
						for(SalesOrderSubLine salesOrderSubLine : salesOrderLine.getSalesOrderSubLineList())  {
							/* Create a new PlanningLine */
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
	
	@Transactional
	public void createStocksMoves(SalesOrder salesOrder) {
		
		StockMove stockMove = new StockMove();
		
		String ref = sequenceService.getSequence(IStockMove.INTERNAL, salesOrder.getCompany(), null, false);
		if(ref.equals("")) {
			ref = sequenceService.getSequence(IStockMove.OUTGOING, salesOrder.getCompany(), null, false);
			if(ref.equals(""))  {
				ref = sequenceService.getSequence(IStockMove.INCOMING, salesOrder.getCompany(), null, false);			
			}
		}
		stockMove.setStockMoveSeq(ref);
		stockMove.setName(ref);
		stockMove.setToAddress(salesOrder.getDeliveryAddress());
		stockMove.setCompany(salesOrder.getCompany());
		stockMove.setStatusSelect(IStockMove.CONFIRMED);
		stockMove.setRealDate(new LocalDate());
		stockMove.setEstimatedDate(new LocalDate());
		
		if(salesOrder.getSalesOrderLineList() != null) {
			stockMove.setStockMoveLineList(new ArrayList<StockMoveLine>());
			for(SalesOrderLine salesOrderLine: salesOrder.getSalesOrderLineList()) {
				if(salesOrderLine.getProduct() != null && salesOrderLine.getProduct().getApplicationTypeSelect() == IProduct.PRODUCT_TYPE && salesOrderLine.getProduct().getProductTypeSelect().equals(IProduct.STOCKABLE)) {
					StockMoveLine stockMoveLine = new StockMoveLine();
					stockMoveLine.setStockMove(stockMove);
					stockMoveLine.setProduct(salesOrderLine.getProduct());
					stockMoveLine.setQty(salesOrderLine.getQty().intValue());
					
					stockMove.getStockMoveLineList().add(stockMoveLine);
					stockMoveLine.save();
				}
			}
		}
		stockMove.save();
	}
}


