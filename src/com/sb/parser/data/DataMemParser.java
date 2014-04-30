package com.sb.parser.data;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import org.apache.log4j.Logger;

import com.sb.core.memory.DataMemory;
import com.sb.parser.config.ConfigParser;

public class DataMemParser {

	// Single instance.
	private static DataMemParser instance = null;
	Logger log = Logger.getLogger(this.getClass().getName());

	protected DataMemParser() {
	}

	public static DataMemParser getInstance() {
		if (instance == null) {
			instance = new DataMemParser();
		}
		return instance;
	}
	
	public DataMemory parseMemory(String path) {
		DataMemory mem = null;
		BufferedReader inStream = null;
		
        try {
        	// Initialize the program object.
        	mem = DataMemory.getInstance();
        	
            inStream = new BufferedReader(new FileReader(path));
            String str;
            while ((str = inStream.readLine()) != null) {
                System.out.println();
                
                if (mem == null) {
                	log.error("Could not parse the instruction: " + str);
                } else {
                	mem.append(str);
                }
            }
        } catch (IOException e) {
        	log.error("Data Memory file does not exist. Please check the path: " + path);
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
        
        return mem;
	}
}
