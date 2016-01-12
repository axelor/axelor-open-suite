package com.axelor.apps.base.db.repo;

import com.axelor.apps.base.db.Address;
import com.axelor.apps.base.service.AddressService;
import com.google.inject.Inject;

public class AddressBaseRepository extends AddressRepository{
	
	@Inject
	protected AddressService addressService;
	
	@Override
	public Address save (Address entity){
		
    	entity.setFullName(addressService.computeFullName(entity));
		
		return super.save(entity);
	}
	
}
