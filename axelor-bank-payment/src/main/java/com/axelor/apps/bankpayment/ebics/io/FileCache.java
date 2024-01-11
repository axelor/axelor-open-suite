/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2024 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.axelor.apps.bankpayment.ebics.io;

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

import java.io.File;
import java.util.Hashtable;
import java.util.Map;

/**
 * A simple mean to cache created files.
 *
 * @author hachani
 */
public class FileCache {

  /**
   * Constructs a new <code>FileCache</code> object
   *
   * @param isTraceEnabled is trace enabled?
   */
  public FileCache(boolean isTraceEnabled) {
    this.isTraceEnabled = isTraceEnabled;
    cache = new Hashtable<String, File>();
  }

  /**
   * Cache a new <code>java.io.File</code> in the cache buffer
   *
   * @param file the file to cache
   * @return True if the file is cached
   */
  public boolean add(File file) {
    if (cache.containsKey(file.getName())) {
      return false;
    }

    cache.put(file.getName(), file);

    return true;
  }

  /**
   * Removes the given <code>java.io.file</code> from the cache.
   *
   * @param filename the file to remove
   * @return True if the file is removed
   */
  public boolean remove(String filename) {
    if (!cache.containsKey(filename)) {
      return false;
    }

    cache.remove(filename);

    return true;
  }

  /** Clears the cache buffer */
  public void clear() {
    if (isTraceEnabled) {
      for (File file : cache.values()) {
        file.delete();
      }
    }

    cache.clear();
  }

  /**
   * Sets the trace ability.
   *
   * @param enabled is trace enabled?
   */
  public void setTraceEnabled(boolean enabled) {
    this.isTraceEnabled = enabled;
  }

  // --------------------------------------------------------------------
  // DATA MEMBERS
  // --------------------------------------------------------------------

  private Map<String, File> cache;
  private boolean isTraceEnabled;
}
