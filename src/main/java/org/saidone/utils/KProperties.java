package org.saidone.utils;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Properties;

public enum KProperties {

	INSTANCE;
	
	private final Properties properties = new Properties();

	KProperties() {
		try (FileInputStream stream = new FileInputStream("etc/properties.xml")) {
			properties.loadFromXML(stream);
		}
		catch (IOException e) {
			throw new UncheckedIOException("Cannot load etc/properties.xml", e);
		}
	}
	
	public String getProperty(String key) {
		return properties.getProperty(key);
	}
	
}
