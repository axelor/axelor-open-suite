package com.axelor.apps.base.db.repo;

import com.axelor.apps.base.db.Address;
import com.google.common.base.Strings;

public class AddressBaseRepository extends AddressRepository{
	
	@Override
	public Address save (Address entity){
		
		String l2 = null;
    	String l3 = null;
    	String l4 = null;
    	String l5 = null;
    	String l6 = null;
    	if(Strings.isNullOrEmpty(entity.getAddressL2()))
    		l2 = entity.getAddressL2();
    	if(Strings.isNullOrEmpty(entity.getAddressL3()))
    		l3 = entity.getAddressL3();
    	if(Strings.isNullOrEmpty(entity.getAddressL4()))
    		l4 = entity.getAddressL4();
    	if(Strings.isNullOrEmpty(entity.getAddressL5()))
    		l5 = entity.getAddressL5();
    	if(Strings.isNullOrEmpty(entity.getAddressL6()))
    		l6 = entity.getAddressL6();
    	entity.setFullName((l2 != null ? l2 : "") + (l3 != null ? " "+l3 : "") + (l4 != null ? " "+l4 : "") + (l5 != null ? " "+l5 : "") + (l6 != null ? " "+l6 : ""));
		
		return super.save(entity);
	}
	
}
