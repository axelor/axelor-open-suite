/**
 * Axelor Business Solutions
 *
 * Copyright (C) 2017 Axelor (<http://axelor.com>).
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
package com.axelor.studio.service;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.Executor;
import org.apache.commons.exec.PumpStreamHandler;

public class CommandService {
	
	public int execute(File workingDir, Map<String, String> enviroment, String command, StringBuffer result) {
		
		final CommandLine commandLine = CommandLine.parse(command);
		
		final Executor executor = new DefaultExecutor();
		executor.setWorkingDirectory(workingDir);
		
		OutStream out = new OutStream(result);
		
		PumpStreamHandler streams = new PumpStreamHandler(out);
		executor.setStreamHandler(streams);
		
		int status = 0;
		try {
			status = executor.execute(commandLine, enviroment);
		} catch (IOException e) {
			status = 1;
			e.printStackTrace();
		}
		
		return status;
	}
	
	class OutStream extends OutputStream {
		
		public StringBuffer buffer =  null;
		
		public OutStream(StringBuffer buffer) {
			this.buffer = buffer;
		}
		
		@Override
		public void write(int b) throws IOException {
			
			buffer.append((char)b);
			
		}
		
	}
}
