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

import java.io.IOException;
import java.io.ObjectOutputStream;

/**
 * The <code>Savable</code> is an element that can be stored in a disk support,
 * files or databases. The save process can be launched via the method
 * {@linkplain Savable#save(ObjectOutputStream)}
 *
 * @author hachani
 *
 */
public interface Savable {

  /**
   * Writes all persistable attributes to the given stream.
   * @param oos the given stream.
   * @throws IOException save process failed
   */
  public void save(ObjectOutputStream oos) throws IOException;

  /**
   * Returns the save name of this savable object.
   * @return the save name
   */
  public String getSaveName();
}
