package com.sb.parser.registers;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import org.apache.log4j.Logger;

import com.sb.core.memory.RegisterMemory;
import com.sb.parser.inst.ProgramParser;

public class RegMemParser {

	// Single instance.
	private static RegMemParser instance = null;
	Logger log = Logger.getLogger(this.getClass().getName());

	protected RegMemParser() {
	}

	public static RegMemParser getInstance() {
		if (instance == null) {
			instance = new RegMemParser();
		}
		return instance;
	}
	
	public RegisterMemory parseRegister(String path) {

		RegisterMemory regState = null;
		BufferedReader inStream = null;

		try {
			// Initialize the program object.
			regState = RegisterMemory.getInstance();

			inStream = new BufferedReader(new FileReader(path));
			String str;
			while ((str = inStream.readLine()) != null) {
				if (regState == null) {
					log.error("Could not parse the instruction: " + str);
				} else {
					regState.append(str);
				}
			}
		} catch (IOException e) {
			log.error("Register memory file does not exist. Please check the path: " + path);
		} finally {
			if (inStream != null) {
				try {
					inStream.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

		return regState;
	}
}
