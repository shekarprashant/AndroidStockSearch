package com.stocksearch.AndroidStockSearch;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;


public class NewsActivity extends Activity {
	
	private static final String LOG_TAG = "NewsDisplay";
	private ListView newsDisplay;
	

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        String jsonArray = intent.getStringExtra("NewsArray");
        setContentView(R.layout.news_activity);
        final ArrayList<String> Links = new ArrayList<String>();
        ArrayList<String> Titles = new ArrayList<String>();
        try {
            JSONArray array = new JSONArray(jsonArray);
            newsDisplay = (ListView)findViewById(R.id.news_display);
            int count = 0;
            for (int i = 0; i < array.length(); i++){
            	JSONObject newsItem = array.getJSONObject(i);
            	
            	if(newsItem.has("Title") && newsItem.has("Link") )
            	{
            		/*try
            		{
            			Titles.add(URLDecoder.decode(newsItem.getString("Title"), "UTF-8"));
            		} 
            		catch (UnsupportedEncodingException e) {
            			Log.e(LOG_TAG, "Character encoding error", e);
            		}*/
            		Titles.add(newsItem.getString("Title"));
            		Links.add(newsItem.getString("Link"));
            		count++;
            	}
            }
        	ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, Titles);
        	newsDisplay.setAdapter(adapter);	
        	
            Toast.makeText(getApplicationContext(), "Showing " + count + " headlines", Toast.LENGTH_LONG).show();
            
        	newsDisplay.setOnItemClickListener(new OnItemClickListener() {
        		public void onItemClick(AdapterView<?> parent,View view,int pos,long id)
        		{
        			final String link = Links.get(pos);
        			AlertDialog.Builder alertDialog = new AlertDialog.Builder(NewsActivity.this);
    				alertDialog.setTitle("View News");
    	            alertDialog.setItems(new CharSequence[]{"View", "Cancel"},
    	            	new DialogInterface.OnClickListener() {
    		                public void onClick(DialogInterface dialog, int which) {
    		                	
    		                	switch(which){
    		                	case 0:
    		                		Intent intent = new Intent(Intent.ACTION_VIEW);
    			                	intent.setData(Uri.parse(link));
    			                	startActivity(intent);
    			                	break;
    		                	case 1:
    		                		dialog.cancel();
    		                	}
    		                	
    			            }
    			        });	            
    	            alertDialog.show();
        		}
        	});
        } catch (JSONException e) {
        	Log.e(LOG_TAG, "Cannot process JSON results", e);
        }
    }
}

