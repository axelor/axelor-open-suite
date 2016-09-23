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

import java.io.ObjectInputStream;
import com.axelor.exception.AxelorException;



/**
 * A mean to serialize and deserialize <code>Object</code>.
 * The manager should ensure serialization and deserialization
 * operations
 *
 * @author hachani
 *
 */
public interface SerializationManager {

  /**
   * Serializes a <code>Savable</code> object
   * @param object the <code>Savable</code> object$
   * @throws EbicsException serialization fails
   */
  public void serialize(Savable object) throws AxelorException;

  /**
   * Deserializes the given object input stream.
   * @param name the name of the serialized object
   * @return the corresponding object input stream
   * @throws EbicsException deserialization fails
   */
  public ObjectInputStream deserialize(String name) throws AxelorException;

  /**
   * Sets the serialization directory
   * @param serializationDir the serialization directory
   */
  public void setSerializationDirectory(String serializationDir);
}
