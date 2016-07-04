package com.axelor.studio.db.repo;

import com.axelor.meta.db.MetaField;
import com.axelor.studio.db.ViewItem;

public class ViewItemRepo extends ViewItemRepository {
	
	@Override
	public ViewItem save(ViewItem viewItem) {
		
		if (viewItem.getId() == null && viewItem.getMetaField() != null) {
			MetaField metaField = viewItem.getMetaField();
			if( viewItem.getWidget() == null && metaField.getTypeName().equals("MetaFile")){
				viewItem.setWidget("binary-link");
			}
		}
		
		return super.save(viewItem);
	}
	
	
}
