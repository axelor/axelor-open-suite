package com.axelor.apps.account.db.repo;

import javax.persistence.PersistenceException;

import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.exception.IExceptionMessage;
import com.axelor.i18n.I18n;

public class MoveLineManagementRepository extends MoveLineRepository{

	
	@Override
	public void remove(MoveLine entity){
		if(!entity.getMove().getStatusSelect().equals(MoveRepository.STATUS_DRAFT)){
			throw new PersistenceException(I18n.get(IExceptionMessage.MOVE_ARCHIVE_NOT_OK));
		}else{
			entity.setArchived(true);
		}
	}
		
	
}
