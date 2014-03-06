package com.zuora.api.util;

import java.util.ArrayList;
import java.util.List;

import com.zuora.api.axis2.ZuoraServiceStub.Account;
import com.zuora.api.axis2.ZuoraServiceStub.QueryResult;

public class ZNuke {

	private ZApi zapi;
	
	public ZNuke() {
		zapi = new ZApi();
		zapi.zLogin();
	}
	
	public void launch() {
		// Query the existing billing account(s)
		QueryResult accountQueryResult = zapi.zQuery("SELECT Id FROM Account");
		Account[] accounts = (Account[]) accountQueryResult.getRecords();
		
		// Extract their IDs
		List<String> ids = new ArrayList<String>();
		for (Account a : accounts) {
			ids.add(a.getId().getID());
		}
		
		// Nuke'em
		zapi.zDelete(ids.toArray(new String[ids.size()]), "Account");
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
