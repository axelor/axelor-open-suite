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

package com.axelor.apps.account.ebics.interfaces;

import com.axelor.exception.AxelorException;
import com.axelor.apps.account.ebics.client.EbicsRootElement;

/**
 * A mean to make EBICS transfer logged by saving
 * requests and responses from the EBICS bank server.
 * This can be done using the <code>trace(EbicsRootElement)</code>
 *
 * @author hachani
 *
 */
public interface TraceManager {

  /**
   * Saves the <code>EbicsRootElement</code> in the traces
   * directory. This directory may be specified by the
   * <code>EbicsConfiguration</code> client configuration.
   *
   * @param element the element to trace
   * @throws EbicsException cannot trace the ebics element
   *
   * @see org.kopi.ebics.interfaces.Configuration#isTraceEnabled() isTraceEnabled()
   */
  public void trace(EbicsRootElement element) throws AxelorException;

  /**
   * Removes an <code>EbicsRootElement</code> from trace
   * directory.
   * @param element the element to ve removed.
   */
  public void remove(EbicsRootElement element);

  /**
   * Clears the traces created for a given ebics session
   */
  public void clear();

  /**
   * Sets the trace directory
   */
  public void setTraceDirectory(String traceDir);

  /**
   * Enables or disables the trace feature
   * @param enabled is trace enabled?
   */
  public void setTraceEnabled(boolean enabled);

}
