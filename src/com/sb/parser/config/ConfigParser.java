package com.sb.parser.config;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.sb.core.config.SBConfig;
import com.sb.core.fu.FuncUnitController;

public class ConfigParser {
	
	Logger log = Logger.getLogger(this.getClass().getName());

	// Single instance.
	private static ConfigParser instance = null;

	protected ConfigParser() {
	}

	public static ConfigParser getInstance() {
		if (instance == null) {
			instance = new ConfigParser();
		}
		return instance;
	}
	
	public SBConfig parseConfig(String path) {
		
		SBConfig conf = null;
		BufferedReader inStream = null;
		
        try {
        	// Initialize the program object.
        	conf = SBConfig.getInstance();
        	
            inStream = new BufferedReader(new FileReader(path));
            String str;
            while ((str = inStream.readLine()) != null) {
                parseConfLine(conf, str);
            }
        } catch (IOException e) {
        	log.error("Config file does not exist. Please check the path: " + path);
		} finally {
            if (inStream != null) {
                try {
					inStream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
            }
        }
        
        return conf;
	}

	private void parseConfLine(SBConfig conf, String str) {
		
		// Parse the config line. 	
		String delim = "[ ]";
		String [] tokens = str.split(delim);
		List<String> list = new ArrayList<String>();
	    for(String s : tokens) {
	       if(s != null && s.length() > 0) {
	          list.add(s);
	       }
	    }
	    
	    // Prune the empty tokens from the string array.
	    tokens = list.toArray(new String[list.size()]);
	    
		try {
			// TODO Auto-generated method stub
			if (tokens[0].equals("FPAdder")) {
				int i = Integer.parseInt(tokens[1]);
				conf.setNoOfFPAdder(i);
			} else if (tokens[0].equals("FPMultiplier")) {
				int i = Integer.parseInt(tokens[1]);
				conf.setNoOfFPMultiplier(i);
			} else if (tokens[0].equals("FPDivider")) {
				int i = Integer.parseInt(tokens[1]);
				conf.setNoOfFPDivider(i);
			} else if (tokens[0].equals("IntegerUnit")) {
				int i = Integer.parseInt(tokens[1]);
				conf.setNoOfIntegerUnit(i);
			} else {
				log.error("Invalid config option provided");
			}
		} catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			log.error("Invalid config value provided");
		}
	}
}
