package com.axelor.apps.account.service.generator.batch;

import java.math.BigDecimal;
import java.util.List;

import javax.inject.Inject;

import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.Reimbursement;
import com.axelor.apps.account.service.ReimbursementImportService;
import com.axelor.apps.account.service.RejectImportService;
import com.axelor.apps.base.db.Company;
import com.axelor.db.JPA;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.IException;
import com.axelor.exception.service.TraceBackService;

public class BatchReimbursementImport extends BatchStrategy {

	private static final Logger LOG = LoggerFactory.getLogger(BatchReimbursementImport.class);

	private boolean stop = false;
	
	private BigDecimal totalAmount = BigDecimal.ZERO;

	
	@Inject
	public BatchReimbursementImport(ReimbursementImportService reimbursementImportService, RejectImportService rejectImportService) {
		
		super(reimbursementImportService, rejectImportService);
		
	}

	@Override
	protected void start() throws IllegalArgumentException, IllegalAccessException {
	
		super.start();
		
		Company company = batch.getAccountingBatch().getCompany();
		
		company = Company.find(company.getId());
				
		try {
			reimbursementImportService.testCompanyField(company);
		} catch (AxelorException e) {
			TraceBackService.trace(new AxelorException("", e, e.getcategory()), IException.REIMBURSEMENT, batch.getId());
			incrementAnomaly();
			stop = true;
		}
		checkPoint();

	}

	@Override
	protected void process() {
		if(!stop)  {
		
			Company company = batch.getAccountingBatch().getCompany();
			
			company = Company.find(company.getId());
			
			Move move = null;
			
			LocalDate rejectDate = null;
			
			List<String[]> rejectList = null;
			try  {
				rejectList = rejectImportService.getCFONBFile(company.getReimbursementImportFolderPathCFONB(), company.getTempReimbImportFolderPathCFONB(),company, 0);
	
				if(rejectList != null && !rejectList.isEmpty())  {
					
					rejectDate = rejectImportService.createRejectDate(rejectList.get(0)[0]);
					
					move = reimbursementImportService.createMoveReject(Company.find(company.getId()), rejectDate);
				}	
				
			} catch (AxelorException e) {
				
				TraceBackService.trace(new AxelorException(String.format("Batch d'import des remboursements %s", batch.getId()), e, e.getcategory()), IException.REIMBURSEMENT, batch.getId());
				
				incrementAnomaly();
				
				stop();
				
			} catch (Exception e) {
				
				TraceBackService.trace(new Exception(String.format("Batch d'import des remboursements %s", batch.getId()), e), IException.REIMBURSEMENT, batch.getId());
				
				incrementAnomaly();
				
				LOG.error("Bug(Anomalie) généré(e) pour le batch d'import des remboursements {}", batch.getId());
			
				stop();
			}	
			
			int seq = 1;
			
			int i = 0;
			
			if(move != null)  {
				for(String[] reject : rejectList)  {
					
					try  {
						
						Reimbursement reimbursement = reimbursementImportService.createReimbursementRejectMoveLine(reject, Company.find(company.getId()), seq, Move.find(move.getId()));
						if(reimbursement != null)  {
							LOG.debug("Remboursement n° {} traité", reimbursement.getRef());
							seq++;
							i++;
							updateReimbursement(reimbursement);
						}
					} catch (AxelorException e) {
						
						TraceBackService.trace(new AxelorException(String.format("Rejet de remboursement %s", reject[1]), e, e.getcategory()), IException.REIMBURSEMENT, batch.getId());
						
						incrementAnomaly();
						
					} catch (Exception e) {
						
						TraceBackService.trace(new Exception(String.format("Rejet de remboursement %s", reject[1]), e), IException.REIMBURSEMENT, batch.getId());
						
						incrementAnomaly();
						
						LOG.error("Bug(Anomalie) généré(e) pour le rejet de remboursement {}", reject[1]);
						
					} finally {
						
						if (i % 10 == 0) { JPA.clear(); }
			
					}	
				}
				
				try  {
					if(seq != 1)  {
						MoveLine oppositeMoveLine = reimbursementImportService.createOppositeRejectMoveLine(Move.find(move.getId()), seq, rejectDate);
						reimbursementImportService.validateMove(Move.find(move.getId()));
						this.totalAmount = this.totalAmount.add(MoveLine.find(oppositeMoveLine.getId()).getDebit());
					}
					else {
						reimbursementImportService.deleteMove(Move.find(move.getId()));
					}
				} catch (AxelorException e) {
					
					TraceBackService.trace(new AxelorException(String.format("Batch d'import des remboursements %s", batch.getId()), e, e.getcategory()), IException.REIMBURSEMENT, batch.getId());
					
					incrementAnomaly();
					
				} catch (Exception e) {
					
					TraceBackService.trace(new Exception(String.format("Batch d'import des remboursements %s", batch.getId()), e), IException.REIMBURSEMENT, batch.getId());
					
					incrementAnomaly();
					
					LOG.error("Bug(Anomalie) généré(e) pour le batch d'import des remboursements {}", batch.getId());
				
				}
			}
		}
	}
	
	
	
	/**
	 * As {@code batch} entity can be detached from the session, call {@code Batch.find()} get the entity in the persistant context.
	 * Warning : {@code batch} entity have to be saved before.
	 */
	@Override
	protected void stop() {
		String comment = "";
		comment = "Compte rendu de l'import des rejets de remboursement :\n";
		comment += String.format("\t* %s remboursement(s) rejeté(s)\n", batch.getDone());
		comment += String.format("\t* Montant total : %s \n", this.totalAmount);
		comment += String.format("\t* %s anomalie(s)", batch.getAnomaly());

		super.stop();
		addComment(comment);
		
	}

}
