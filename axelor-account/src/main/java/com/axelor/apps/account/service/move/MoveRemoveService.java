package com.axelor.apps.account.service.move;

import java.util.List;

import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.repo.MoveLineRepository;
import com.axelor.apps.account.db.repo.MoveRepository;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class MoveRemoveService {
	
	
	protected MoveRepository moveRepo;
	
	protected MoveLineRepository moveLineRepo;

	@Inject
	public MoveRemoveService(MoveRepository moveRepo, MoveLineRepository moveLineRepo) {

		this.moveRepo = moveRepo;
		this.moveLineRepo = moveLineRepo;
		
	}

	@Transactional
	public void archiveMove(Move move) {
		moveRepo.remove(move);
		for(MoveLine moveLine : move.getMoveLineList())  {
			moveLineRepo.remove(moveLine);
		}
		
	}
	
	@Transactional
	public void deleteMultiple(List<? extends Move> moveList) {
		for (Move move : moveList) {
			this.archiveMove(move);
		}
	}
	
}