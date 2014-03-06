package com.zuora.api.util;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zuora.api.axis2.ZuoraServiceStub.Account;
import com.zuora.api.axis2.ZuoraServiceStub.DeleteResult;
import com.zuora.api.axis2.ZuoraServiceStub.QueryResult;
import com.zuora.api.axis2.ZuoraServiceStub.ZObject;

public class ZNuke {

	/** The Zuora API helper instance */
	private ZApi zapi;
	
	/** The logger */
	private static Logger logger = LoggerFactory.getLogger(ZNuke.class);
	
	public ZNuke() {
		zapi = new ZApi();
		zapi.zLogin();
	}
	
	public void launch() {
		logger.debug("* * * NUKE LAUNCHED!!! * * *");
		// Query the existing billing account(s)
		QueryResult accountQueryResult = zapi.zQuery("SELECT Id FROM Account");
		
		// Extract their IDs
		List<String> ids = new ArrayList<String>();
		for (ZObject zobj : accountQueryResult.getRecords()) {
			Account a = (Account) zobj;
			ids.add(a.getId().getID());
		}
		logger.debug("* * * Estimated casualties: " + ids.size() + " * * *");
		
		// Nuke'em
		DeleteResult[] deleted = zapi.zDelete(ids.toArray(new String[ids.size()]), "Account");
		
		// Check the result
		boolean hasFailure = false;
		for (DeleteResult d : deleted) {
			if (!d.getSuccess()) {
				hasFailure = true;
				logger.error("* * * Could not nuke the target * * *");
				for (com.zuora.api.axis2.ZuoraServiceStub.Error e : d.getErrors()) {
					logger.error("Field: " + e.getField() + " | Message: " + e.getMessage() + " | Code: " + e.getCode());
				}
			}
		}
		
		if (!hasFailure) {
			logger.debug("* * * Successfully nuked this tenant! Congrats. * * *");
		}
	}
	
	public static void main(String[] args) {
		// Change this to "True" to proceed WITH CAUTION!!!
		boolean proceed = false;
		
		// ALL account(s) will be erased from the associated tenant!
		if (proceed) {
			new ZNuke().launch();
		}
	}

}
