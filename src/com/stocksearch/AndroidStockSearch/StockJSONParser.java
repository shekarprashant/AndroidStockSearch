package com.stocksearch.AndroidStockSearch;
 
import java.util.ArrayList;
import java.util.List;
 

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;
 
public class StockJSONParser {
 
/** Receives a JSONObject and returns a list */
    public List<String> parse(JSONObject jObject){
 
        JSONArray jStocks = null;
        try {
            /** Retrieves all the elements in the 'places' array */
        	jStocks = jObject.getJSONArray("Result");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        /** Invoking getPlaces with the array of json object
         * where each json object represent a place
        */
        return getStocks(jStocks);
    }
 
    private List<String> getStocks(JSONArray jStocks){

    	List<String>stockList = new ArrayList<String>(jStocks.length());
	    try {

	        for (int i = 0; i < jStocks.length(); i++) {
	        	stockList.add(jStocks.getJSONObject(i).getString("symbol"));
	        }
	    } catch (JSONException e) {
	        Log.e("StockJSONParser", "Cannot process JSON results", e);
	    }

 
        return stockList;
    }
 
}