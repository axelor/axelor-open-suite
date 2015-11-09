package com.axelor.apps.base.db.repo;

import com.axelor.apps.base.db.Sequence;

public class SequenceBaseRepository extends SequenceRepository{
	
	@Override
	public Sequence copy(Sequence sequence, boolean deep) {
		
		sequence.clearSequenceVersionList();
		
		return super.copy(sequence, deep);
	}
}
