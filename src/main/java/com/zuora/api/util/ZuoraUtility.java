package com.zuora.api.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ZuoraUtility {

	/** The logger (using logback, but can easily be configured for log4j). */
	private static final Logger logger = LoggerFactory.getLogger(ZuoraUtility.class);

	/** Filename for properties file. */
	private static final String FILE_PROPERTY_NAME = "config.properties";

	/** The properties, loaded from the file. */
	private static Properties properties = null;

	/**
	 * Load the properties.
	 */
	public static void loadProperties() {

		// Retrieve resource
		InputStream is = ZuoraUtility.class.getResourceAsStream("/" + FILE_PROPERTY_NAME);

		properties = new Properties();

		try {
			properties.load(is);
			logger.info("Properties successfully loaded from `" + FILE_PROPERTY_NAME + "`");

		} catch (IOException e) {
			logger.error("Error loading properties file | " + e.getMessage());

		} catch (NullPointerException e) {
			logger.error("Error loading properties file (null pointer exception while loading properties) | "
					+ e.getMessage());
		}

	}

	/**
	 * Get the properties (singleton pattern).
	 */
	public static Properties getProperties() {

		if (properties == null) {
			loadProperties();
		}

		return properties;
	}

	/**
	 * Get a property value.
	 */
	public static String getPropertyValue(String propertyName) {

		return getProperties().getProperty(propertyName);
	}
}
