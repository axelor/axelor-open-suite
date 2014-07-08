/**
 * Axelor Business Solutions
 *
 * Copyright (C) 2012-2014 Axelor (<http://axelor.com>).
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
package com.axelor.apps.account.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.MoveTemplate;
import com.axelor.apps.account.db.MoveTemplateLine;
import com.axelor.apps.base.db.Partner;
import com.axelor.exception.AxelorException;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class MoveTemplateService {
	
	private static final Logger LOG = LoggerFactory.getLogger(MoveTemplateService.class);
	
	@Inject
	MoveService moveService;
	
	@Inject
	MoveLineService moveLineService;
	
	@Transactional
	public void validateMoveTemplateLine(MoveTemplate moveTemplate){
		moveTemplate.setIsValid(true);
		for(MoveTemplateLine line : moveTemplate.getMoveTemplateLineList())
			line.setIsValid(true);
		moveTemplate.save();
	}
	
	@SuppressWarnings("unchecked")
	@Transactional
	public List<Long> generateMove(MoveTemplate moveTemplate, List<HashMap<String,Object>> dataList){
		try {
			List<Long> moveList = new ArrayList<Long>();
			BigDecimal hundred = new BigDecimal(100);
			for(HashMap<String,Object> data : dataList){
				LocalDate moveDate = new LocalDate(data.get("date").toString());
				Partner debitPartner = null;
				Partner creditPartner = null;
				BigDecimal moveBalance = new BigDecimal(data.get("moveBalance").toString());
				Partner partner = null;
				if(data.get("debitPartner") != null){
					debitPartner = Partner.find(Long.parseLong(((HashMap<String,Object>) data.get("debitPartner")).get("id").toString()));
					partner = debitPartner;
				}	
				if(data.get("creditPartner") != null){
					creditPartner = Partner.find(Long.parseLong(((HashMap<String,Object>) data.get("creditPartner")).get("id").toString()));
					partner = creditPartner;
				}
				Move move = moveService.createMove(moveTemplate.getJournal(), moveTemplate.getJournal().getCompany(), null, partner,moveDate, null);
				for(MoveTemplateLine line : moveTemplate.getMoveTemplateLineList()){
					partner = null;
					if(line.getDebitCreditSelect().equals("0")){
						if(line.getHasPartnerToDebit())
							partner = debitPartner;
						MoveLine moveLine = moveLineService.createMoveLine(move, partner, line.getAccount(), moveBalance.multiply(line.getPercentage()).divide(hundred), true, false, moveDate, moveDate, 0, line.getName());
						move.getMoveLineList().add(moveLine);
					}
					else{
						if(line.getHasPartnerToDebit())
							partner = creditPartner;
						MoveLine moveLine = moveLineService.createMoveLine(move, partner, line.getAccount(), moveBalance.multiply(line.getPercentage()).divide(hundred), false, false, moveDate, moveDate, 0, line.getName());
						move.getMoveLineList().add(moveLine);
					}
				}
				move.save();
				moveList.add(move.getId());
			}
			return moveList;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

}
