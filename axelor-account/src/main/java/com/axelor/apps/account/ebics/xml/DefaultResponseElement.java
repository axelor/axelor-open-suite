/*
 * Copyright (c) 1990-2012 kopiLeft Development SARL, Bizerte, Tunisia
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License version 2.1 as published by the Free Software Foundation.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 * $Id$
 */

package com.axelor.apps.account.ebics.xml;

import java.io.IOException;

import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;

import com.axelor.apps.account.db.EbicsUser;
import com.axelor.apps.account.ebics.exception.ReturnCode;
import com.axelor.apps.account.ebics.interfaces.ContentFactory;
import com.axelor.apps.account.ebics.service.EbicsUserService;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.IException;
import com.axelor.inject.Beans;


/**
 * The <code>DefaultResponseElement</code> is the common element for
 * all ebics server responses.
 *
 * @author Hachani
 *
 */
public abstract class DefaultResponseElement extends DefaultEbicsRootElement {

  /**
   * Constructs a new ebics response element.
   * @param factory the content factory containing the response.
   * @param name the element name
   */
  public DefaultResponseElement(ContentFactory factory, String name, EbicsUser ebicsUser) {
    this.factory = factory;
    this.name = name;
    this.ebicsUser = ebicsUser;
  }

  /**
   * Parses the content of a <code>ContentFactory</code>
   * @param factory the content factory
   * @throws EbicsException parse error
   */
  protected void parse(ContentFactory factory) throws AxelorException {
    try {
      document = XmlObject.Factory.parse(factory.getContent());
    } catch (XmlException e) {
      throw new AxelorException(e.getMessage(), IException.CONFIGURATION_ERROR );
    } catch (IOException e) {
      throw new AxelorException(e.getMessage(), IException.CONFIGURATION_ERROR );
    }
  }

  /**
   * Reports the return code to the user.
   * @throws EbicsException request fails.
   */
  public void report(boolean fromBuild) throws AxelorException {
	  
	if (!fromBuild ||  !returnCode.isOk()) {
		log();
	}
	
    if (!returnCode.isOk()) {
      returnCode.throwException();
    }
    
  }
  
  protected void log() {
	  if (ebicsUser != null && name != null && returnCode != null) {
		  Beans.get(EbicsUserService.class).logRequest(ebicsUser, name.substring(0,3), returnCode.getSymbolicName());
		  ebicsUser = null; // Prevent further log on same request
	  }
  }
  

  @Override
  public String getName() {
    return name + ".xml";
  }

  // --------------------------------------------------------------------
  // DATA MEMBERS
  // --------------------------------------------------------------------

  private String name;
  private EbicsUser ebicsUser;
  protected ContentFactory	factory;
  protected ReturnCode	returnCode;
}
