package org.saidone.utils;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public enum KProperties {

	INSTANCE;
	
	private final Properties properties = new Properties();

	KProperties() {
		try {
			properties.loadFromXML(new FileInputStream("etc/properties.xml"));
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public String getProperty(String key) {
		return properties.getProperty(key);
	}
	
}