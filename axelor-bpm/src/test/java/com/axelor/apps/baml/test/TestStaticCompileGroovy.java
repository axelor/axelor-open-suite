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
package com.axelor.apps.baml.test;

import com.axelor.auth.db.User;
import com.axelor.rpc.Context;
import com.axelor.script.GroovyScriptHelper;
import org.junit.Test;

public class TestStaticCompileGroovy {

  @Test
  public void test() {
    String script =
        "import com.axelor.auth.db.User\n"
            + "import groovy.transform.CompileStatic\n"
            + "@CompileStatic\n"
            + "void execute(){\n"
            + "User user = new User()\n"
            + "user.code = 'abc'\n"
            + "def x = user\n"
            + "println(x.code)\n"
            + "}\n"
            + "execute()";
    Context ctx = new Context(User.class);
    GroovyScriptHelper helper = new GroovyScriptHelper(ctx);
    helper.eval(script);
  }
}
