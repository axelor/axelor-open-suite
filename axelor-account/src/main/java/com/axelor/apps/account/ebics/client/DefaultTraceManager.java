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

package com.axelor.apps.account.ebics.client;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.jdom.JDOMException;

import com.axelor.apps.account.ebics.exception.EbicsException;
import com.axelor.apps.account.ebics.interfaces.TraceManager;
import com.axelor.apps.account.ebics.io.FileCache;
import com.axelor.apps.account.ebics.io.IOUtils;
import com.axelor.exception.AxelorException;


/**
 * The <code>DefaultTraceManager</code> aims to trace an ebics
 * transferable element in an instance of <code>java.io.File</code>
 * then saved to a trace directory.
 * The manager can delete all traces file if the configuration does
 * not offer tracing support.
 * see {@link Configuration#isTraceEnabled() isTraceEnabled()}
 *
 * @author hachani
 *
 */
public class DefaultTraceManager implements TraceManager {

  /**
   * Constructs a new <code>TraceManger</code> to manage transfer traces.
   * @param traceDir the trace directory
   * @param isTraceEnabled is trace enabled?
   */
  public DefaultTraceManager(File traceDir, boolean isTraceEnabled) {
    this.traceDir = traceDir;
    cache = new FileCache(isTraceEnabled);
  }

  /**
   * Constructs a new <code>TraceManger</code> to manage transfer traces.
   * @param isTraceEnabled is trace enabled?
   */
  public DefaultTraceManager(boolean isTraceEnabled) {
    this(null, isTraceEnabled);
  }

  /**
   * Constructs a new <code>TraceManger</code> with trace option enabled.
   */
  public DefaultTraceManager() {
    this(null, true);
  }

  @Override
  public void trace(EbicsRootElement element) throws AxelorException {
    try {
      FileOutputStream		out;
      File			file;

      file = IOUtils.createFile(traceDir, element.getName());
      out = new FileOutputStream(file);
      element.save(out);
      cache.add(file);
    } catch (Exception e) {
      throw new AxelorException(e.getMessage(), 1);
    }
  }

  @Override
  public void remove(EbicsRootElement element) {
    cache.remove(element.getName());
  }

  @Override
  public void clear() {
    cache.clear();
  }

  @Override
  public void setTraceDirectory(String traceDir) {
    this.traceDir = IOUtils.createFile(traceDir);
  }

  @Override
  public void setTraceEnabled(boolean enabled) {
    cache.setTraceEnabled(enabled);
  }

  // --------------------------------------------------------------------
  // DATA MEMBERS
  // --------------------------------------------------------------------

  private File				traceDir;
  private FileCache			cache;
}
