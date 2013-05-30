package com.zuora.api.util;

import java.rmi.RemoteException;

import org.apache.axis2.AxisFault;
import org.apache.axis2.client.ServiceClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zuora.api.axis2.InvalidQueryLocatorFault;
import com.zuora.api.axis2.LoginFault;
import com.zuora.api.axis2.MalformedQueryFault;
import com.zuora.api.axis2.UnexpectedErrorFault;
import com.zuora.api.axis2.ZuoraServiceStub;
import com.zuora.api.axis2.ZuoraServiceStub.Account;
import com.zuora.api.axis2.ZuoraServiceStub.Login;
import com.zuora.api.axis2.ZuoraServiceStub.LoginResponse;
import com.zuora.api.axis2.ZuoraServiceStub.LoginResult;
import com.zuora.api.axis2.ZuoraServiceStub.Query;
import com.zuora.api.axis2.ZuoraServiceStub.QueryResponse;
import com.zuora.api.axis2.ZuoraServiceStub.QueryResult;
import com.zuora.api.axis2.ZuoraServiceStub.SessionHeader;
import com.zuora.api.axis2.ZuoraServiceStub.ZObject;

public class ZApi {

	/** The logger. */
	private static Logger logger = LoggerFactory.getLogger(ZApi.class);

	/** The Constant PROPERTY_ENDPOINT. */
	private static final String PROPERTY_ENDPOINT = "endpoint";

	/** The Constant PROPERTY_USERNAME. */
	private static final String PROPERTY_USERNAME = "username";

	/** The Constant PROPERTY_PASSWORD. */
	private static final String PROPERTY_PASSWORD = "password";

	/** The stub. */
	private ZuoraServiceStub stub;

	/** The header. */
	private SessionHeader header;

	/**
	 * Instantiates a new Zuora API Helper
	 */
	public ZApi() {

		logger.info("Creating a new ZAPI object");

		try {
			this.stub = new ZuoraServiceStub();
			// set new ENDPOINT
			String endpoint = ZuoraUtility.getPropertyValue(PROPERTY_ENDPOINT);

			if (endpoint != null && endpoint.trim().length() > 0) {
				ServiceClient client = stub._getServiceClient();
				client.getOptions().getTo().setAddress(endpoint);
			}

		} catch (AxisFault e) {
			logger.error(e.getMessage());
		}

	}

	/**
	 * zLogin.
	 * 
	 * @return LoginResult object
	 */
	public LoginResult zLogin() {

		LoginResult result = null;

		String username = ZuoraUtility.getPropertyValue(PROPERTY_USERNAME);
		String password = ZuoraUtility.getPropertyValue(PROPERTY_PASSWORD);

		logger.debug("Username = " + username + " | Password = " + password);

		// Prepare the login request
		Login login = new Login();

		login.setUsername(username);
		login.setPassword(password);

		// Get the response from Zuora
		LoginResponse resp;

		try {

			resp = stub.login(login);
			result = resp.getResult();

			// Create session for all subsequent calls
			this.header = new SessionHeader();
			this.header.setSession(result.getSession());

			logger.info("User `" + ZuoraUtility.getPropertyValue(PROPERTY_USERNAME) + "` successfully connected!");

		} catch (RemoteException e) {
			logger.error("Remote error while trying to login | " + e.getMessage());

		} catch (UnexpectedErrorFault e) {
			logger.error("Unexpected Error Fault | " + e.getMessage());

		} catch (LoginFault e) {
			logger.error("Login Fault | " + e.getMessage());
		}

		return result;
	}

	public QueryResult zQuery(String queryString) {

		QueryResult result = null;

		// Prepare the query
		Query query = new Query();

		query.setQueryString(queryString);
		logger.debug("Query String = " + queryString);

		// We set `null` for the 2nd parameter to return max. 2000 objects
		try {
			QueryResponse resp = stub.query(query, null, header);
			result = resp.getResult();
			logger.info("Query returned " + result.getSize() + " values");

		} catch (RemoteException e) {
			logger.error("Remote Exception | " + e.getMessage());

		} catch (MalformedQueryFault e) {
			logger.error("Malformed Query | " + e.getMessage());

		} catch (UnexpectedErrorFault e) {
			logger.error("Unexpected Error Fault | " + e.getMessage());

		} catch (InvalidQueryLocatorFault e) {
			logger.error("Invalid Query Locator | " + e.getMessage());

		}

		return result;
	}

}
