package com.axelor.apps.account.service.move;

import java.util.List;

import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.repo.MoveRepository;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class MoveRemoveService {
	
	
	protected MoveRepository moveRepository;

	@Inject
	public MoveRemoveService(MoveRepository moveRepository) {

		this.moveRepository = moveRepository;
		
	}

	@Transactional
	public void archiveMove(Move move) {
		move.setArchived(true);
		for(MoveLine moveLine : move.getMoveLineList())  {
			moveLine.setArchived(true);
		}
		moveRepository.save(move);
	}
	
	@Transactional
	public void deleteMultiple(List<? extends Move> moveList) {
		for (Move move : moveList) {
			this.archiveMove(move);
		}
	}
	
}