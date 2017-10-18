/**
 * Axelor Business Solutions
 *
 * Copyright (C) 2017 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or  modify
 * it under the terms of the GNU Affero General Public License, version 3,
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.axelor.apps.account.service.move;

import com.axelor.apps.account.db.Journal;
import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.repo.MoveRepository;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.IAdministration;
import com.axelor.apps.base.service.administration.SequenceService;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.IException;
import com.axelor.i18n.I18n;
import com.google.common.base.Strings;
import com.google.inject.Inject;

public class MoveSequenceService {

	private SequenceService sequenceService;

	@Inject
	public MoveSequenceService(SequenceService sequenceService) {

		this.sequenceService = sequenceService;
		
	}

	
	protected String getDraftSequence(Company company) throws AxelorException  {
		String seq = sequenceService.getSequenceNumber(IAdministration.DOCUMENT_DRAFT, company);
		if (seq == null)  {
			throw new AxelorException(String.format(I18n.get(com.axelor.apps.base.exceptions.IExceptionMessage.DRAFT_SEQUENCE_1),company.getName()),
							IException.CONFIGURATION_ERROR);
		}
		return seq;
	}		
			
	public void setDraftSequence(Move move) throws AxelorException  {
			
		if (move.getId() != null && Strings.isNullOrEmpty(move.getReference())
			&& move.getStatusSelect() == MoveRepository.STATUS_DRAFT)  {		
			move.setReference(this.getDraftSequence(move.getCompany()));
		}		
		
	}		
	
	
	public void setSequence(Move move)  {
		
		Journal journal = move.getJournal();
		
		move.setReference( sequenceService.getSequenceNumber(journal.getSequence()) );

	}
		
}