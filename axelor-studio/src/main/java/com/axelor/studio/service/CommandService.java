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
