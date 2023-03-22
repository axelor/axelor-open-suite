package com.axelor.apps.account.service.move.record;

import java.util.Objects;
import java.util.Optional;

import com.axelor.apps.account.db.Journal;
import com.axelor.apps.account.db.JournalType;
import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.repo.JournalTypeRepository;
import com.axelor.apps.base.db.Partner;


public class MoveRecordServiceImpl implements MoveRecordService {

	@Override
	public Move setPaymentMode(Move move) {
		Objects.requireNonNull(move);
		
		Partner partner = move.getPartner();
		JournalType journalType = Optional.ofNullable(move.getJournal()).map(Journal::getJournalType).orElse(null);
		
		if (partner != null && journalType != null) {
			if (journalType.getTechnicalTypeSelect().equals(JournalTypeRepository.TECHNICAL_TYPE_SELECT_EXPENSE)) {
				move.setPaymentMode(partner.getOutPaymentMode());
			} 
			else if (journalType.getTechnicalTypeSelect().equals(JournalTypeRepository.TECHNICAL_TYPE_SELECT_SALE)) {
				move.setPaymentMode(partner.getInPaymentMode());
			} else {
				move.setPaymentMode(null);
			}
		} else {
			move.setPaymentMode(null);
		}
		return move;
	}

	@Override
	public Move setPaymentCondition(Move move) {
		Objects.requireNonNull(move);
		
		Partner partner = move.getPartner();
		JournalType journalType = Optional.ofNullable(move.getJournal()).map(Journal::getJournalType).orElse(null);
		
		if (partner != null && journalType != null && journalType.getTechnicalTypeSelect().equals(JournalTypeRepository.TECHNICAL_TYPE_SELECT_TREASURY)) {
			move.setPaymentCondition(partner.getPaymentCondition());
		} else {
			move.setPaymentCondition(null);
		}
		
		return move;
	}

}
