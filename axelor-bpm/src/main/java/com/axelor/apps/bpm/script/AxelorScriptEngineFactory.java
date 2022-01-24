/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2022 Axelor (<http://axelor.com>).
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
package com.axelor.apps.bpm.script;

import groovy.lang.GroovySystem;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;

public class AxelorScriptEngineFactory implements ScriptEngineFactory {

  private static final String VERSION = "1.0";

  private static final String SHORT_NAME = "axelor";

  private static final String LANGUAGE_NAME = "axelor";

  @Override
  public ScriptEngine getScriptEngine() {
    return new AxelorScriptEngine(this);
  }

  @Override
  public String getEngineName() {
    return "Axelor Script Engine";
  }

  @Override
  public String getEngineVersion() {
    return VERSION;
  }

  @Override
  public List<String> getExtensions() {
    return EXTENSIONS;
  }

  @Override
  public String getLanguageName() {
    return LANGUAGE_NAME;
  }

  @Override
  public String getLanguageVersion() {
    return GroovySystem.getVersion();
  }

  @Override
  public String getMethodCallSyntax(String obj, String m, String... args) {
    String ret = obj + "." + m + "(";
    int len = args.length;
    if (len == 0) {
      ret += ")";
      return ret;
    }

    for (int i = 0; i < len; i++) {
      ret += args[i];
      if (i != len - 1) {
        ret += ",";
      } else {
        ret += ")";
      }
    }
    return ret;
  }

  @Override
  public List<String> getMimeTypes() {
    return MIME_TYPES;
  }

  @Override
  public List<String> getNames() {
    return NAMES;
  }

  @Override
  public String getOutputStatement(String toDisplay) {
    StringBuilder buf = new StringBuilder();
    buf.append("println(\"");
    int len = toDisplay.length();
    for (int i = 0; i < len; i++) {
      char ch = toDisplay.charAt(i);
      switch (ch) {
        case '"':
          buf.append("\\\"");
          break;
        case '\\':
          buf.append("\\\\");
          break;
        default:
          buf.append(ch);
          break;
      }
    }
    buf.append("\")");
    return buf.toString();
  }

  @Override
  public Object getParameter(String key) {
    if (ScriptEngine.NAME.equals(key)) {
      return SHORT_NAME;
    } else if (ScriptEngine.ENGINE.equals(key)) {
      return getEngineName();
    } else if (ScriptEngine.ENGINE_VERSION.equals(key)) {
      return VERSION;
    } else if (ScriptEngine.LANGUAGE.equals(key)) {
      return LANGUAGE_NAME;
    } else if (ScriptEngine.LANGUAGE_VERSION.equals(key)) {
      return GroovySystem.getVersion();
    } else if ("THREADING".equals(key)) {
      return "MULTITHREADED";
    } else {
      throw new IllegalArgumentException("Invalid key");
    }
  }

  @Override
  public String getProgram(String... statements) {
    StringBuilder ret = new StringBuilder();
    for (String statement : statements) {
      ret.append(statement).append('\n');
    }
    return ret.toString();
  }

  private static final List<String> NAMES;
  private static final List<String> EXTENSIONS;
  private static final List<String> MIME_TYPES;

  static {
    List<String> n = new ArrayList<String>(2);
    n.add(SHORT_NAME);
    n.add(LANGUAGE_NAME);
    NAMES = Collections.unmodifiableList(n);

    n = new ArrayList<String>(1);
    n.add("axelor");
    EXTENSIONS = Collections.unmodifiableList(n);

    n = new ArrayList<String>(1);
    n.add("application/x-groovy");
    MIME_TYPES = Collections.unmodifiableList(n);
  }
}
