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
package com.axelor.apps.message.service;

import java.util.List;

import com.axelor.apps.base.db.Template;
import com.axelor.apps.message.db.EmailAddress;
import com.axelor.apps.message.db.Message;
import com.axelor.exception.AxelorException;
import com.axelor.tool.template.TemplateMaker;

public interface TemplateMessageService {

	public Message generateMessage(Object object, long objectId, String model, String tag, Template template) throws ClassNotFoundException, InstantiationException, IllegalAccessException, AxelorException;
	
	public List<EmailAddress> getEmailAddress(String recipients);
	
	public TemplateMaker initMaker(long objectId, String model, String tag) throws InstantiationException, IllegalAccessException, ClassNotFoundException;
	
	
}
