package com.stocksearch.AndroidStockSearch;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup.LayoutParams;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.*;
import com.facebook.widget.WebDialog;
import com.facebook.widget.WebDialog.OnCompleteListener;

public class MainActivity extends ActionBarActivity implements OnItemClickListener, OnClickListener{



	private AutoCompleteTextView autoCompView;
	private Button btnSearch;
	private TextView symbolDisplay;
	private TextView priceDisplay;
	private TextView changeDisplay;
	private ImageView changeImageDisplay;
	private TableLayout stockTableDisplay;
	private ImageView stockChartDisplay;
	private Button btnNews;
	private Button btnFacebook;
	private JSONObject resultJSONObject;
	
	private ShowDetailsTask DisplayTask;
	
	private static String Name;
	private static String Symbol;
	private static String StockChartURL;
	private static String ChangeType;
	private static String Change;
	private static String ChangeinPercent;
	private static String LastTradePrice;
	private static String StockLink;
	
	private static final String LOG_TAG = "StockSearch";

	private static final String YAHOO_API = "http://autoc.finance.yahoo.com/autoc";
	private static final String CALLBACK = "YAHOO.Finance.SymbolSuggest.ssCallback";

	private static final String SERVLET_LINK = "http://cs-server.usc.edu:32878/examples/servlet/SearchServlet";
	private static final String ERROR_TEXT = "Stock information not available.";
	
	private static final String UP_IMAGE_LINK = "http://www-scf.usc.edu/~csci571/2014Spring/hw6/up_g.gif";
	private static final String DOWN_IMAGE_LINK = "http://www-scf.usc.edu/~csci571/2014Spring/hw6/down_r.gif";
	
	private class StockAutoCompleteAdapter extends ArrayAdapter<String> implements Filterable {
	    private ArrayList<String> resultList;

	    public StockAutoCompleteAdapter(Context context, int textViewResourceId) {
	        super(context, textViewResourceId);
	    }

	    @Override
	    public int getCount() {
	        return resultList.size();
	    }

	    @Override
	    public String getItem(int index) {
	        return resultList.get(index);
	    }

	    @Override
	    public Filter getFilter() {
	        Filter filter = new Filter() {
	            @Override
	            protected FilterResults performFiltering(CharSequence constraint) {
	                FilterResults filterResults = new FilterResults();
	                if (constraint != null) {
	                    // Retrieve the autocomplete results.
	                    resultList = autocomplete(constraint.toString());

	                    // Assign the data to the FilterResults
	                    filterResults.values = resultList;
	                    filterResults.count = resultList.size();
	                }
	                return filterResults;
	            }

	            @Override
	            protected void publishResults(CharSequence constraint, FilterResults results) {
	                if (results != null && results.count > 0) {
	                    notifyDataSetChanged();
	                }
	                else {
	                    notifyDataSetInvalidated();
	                }
	            }};
	        return filter;
	    }
	}
	
	private ArrayList<String> autocomplete(String input) {

	    StringBuilder jsonpResults = new StringBuilder();
	    ArrayList<String> resultList = null;
	    HttpURLConnection conn = null;

	    try {
	        StringBuilder sb = new StringBuilder(YAHOO_API);			        
	        sb.append("?query=" + URLEncoder.encode(input, "utf8"));
	        sb.append("&callback=" + CALLBACK);

	        URL url = new URL(sb.toString());
	        conn = (HttpURLConnection) url.openConnection();
	        InputStreamReader in = new InputStreamReader(conn.getInputStream());

	        // Load the results into a StringBuilder
	        int read;
	        char[] buff = new char[1024];
	        while ((read = in.read(buff)) != -1) {
	            jsonpResults.append(buff, 0, read);
	        }
	    } catch (MalformedURLException e) {
	        Log.e(LOG_TAG, "Error connecting to Yahoo Finance API URL", e);
	        return resultList;
	    } catch (IOException e) {
	    	Log.e(LOG_TAG, "Error connecting to Yahoo Finance API", e);
	        return resultList;
	    } finally {
	        if (conn != null) {
	            conn.disconnect();
	        }
	    }

	    try {
	        // Create a JSON object hierarchy from the results
	    	int start = jsonpResults.indexOf("(") + 1, end = jsonpResults.lastIndexOf(")");
	    	
	    	start = (start < 0)?0:start;
	    	end = (end < 0)?jsonpResults.length():end;
	        JSONObject jsonObj = new JSONObject(jsonpResults.substring(start, end));

	        JSONArray resJsonArray = jsonObj.getJSONObject("ResultSet").getJSONArray("Result");

	        // Extract the Place descriptions from the results
	        resultList = new ArrayList<String>(resJsonArray.length());
	        for (int i = 0; i < resJsonArray.length(); i++) {
	            resultList.add(resJsonArray.getJSONObject(i).getString("symbol") + ", " 
	            				+ resJsonArray.getJSONObject(i).getString("name") + "("
	            				+ resJsonArray.getJSONObject(i).getString("exch") + ")");
	        }
	    } catch (JSONException e) {
	    	Log.e(LOG_TAG, "Cannot process JSON results", e);
	    }

		return resultList;
	}
	


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        
        autoCompView = (AutoCompleteTextView) findViewById(R.id.InputStockSearch);
        btnSearch = (Button) findViewById(R.id.btn_search);
        autoCompView.setThreshold(1);
        autoCompView.setAdapter(new StockAutoCompleteAdapter(this, R.layout.autocomplete_list_item)); 
        autoCompView.setOnItemClickListener(this);
        btnSearch.setOnClickListener(this);
        
        symbolDisplay = (TextView)findViewById(R.id.symbol_display);
        priceDisplay = (TextView)findViewById(R.id.price_display);
        changeImageDisplay = (ImageView)findViewById(R.id.change_img_display);
        changeDisplay = (TextView)findViewById(R.id.change_display);
        stockTableDisplay = (TableLayout)findViewById(R.id.stock_table_display);
        stockChartDisplay = (ImageView)findViewById(R.id.stock_chart_display);
        btnNews = (Button)findViewById(R.id.btn_news);
        btnNews.setOnClickListener(this);
        btnFacebook = (Button)findViewById(R.id.btn_facebook);
        btnFacebook.setOnClickListener(this);
        
        OnFocusChangeListener ofcListener = new MyFocusChangeListener();
        autoCompView.setOnFocusChangeListener(ofcListener);

    }
    
	private void toggleTextVisibility(boolean visible, boolean success, boolean news){
		
		symbolDisplay.setVisibility(View.GONE);
		priceDisplay.setVisibility(View.GONE);
		changeImageDisplay.setVisibility(View.GONE);
		changeDisplay.setVisibility(View.GONE);
		stockTableDisplay.setVisibility(View.GONE);
		stockChartDisplay.setVisibility(View.GONE);
		btnNews.setVisibility(View.GONE);
		btnFacebook.setVisibility(View.GONE);
		
		if (visible){
	    	if (success){
		    	symbolDisplay.setVisibility(View.VISIBLE);
		    	priceDisplay.setVisibility(View.VISIBLE);
		    	changeImageDisplay.setVisibility(View.VISIBLE);
		    	changeDisplay.setVisibility(View.VISIBLE);
		    	stockTableDisplay.setVisibility(View.VISIBLE);
		    	stockChartDisplay.setVisibility(View.VISIBLE);
		    	
		    	if (news)
		    		btnNews.setVisibility(View.VISIBLE);
		    	
		    	btnFacebook.setVisibility(View.VISIBLE);
	    	}
	    	else
	    		priceDisplay.setVisibility(View.VISIBLE);
		}
    }
	
	
	private void addRowToTable(TableLayout Table, String label, String value){
		
		TableRow row = new TableRow(this);
		row.setLayoutParams(new LayoutParams(
				LayoutParams.MATCH_PARENT,
				LayoutParams.WRAP_CONTENT));
		
		TextView labelText = new TextView(this);
		labelText.setText(label);
        row.addView(labelText);

        TextView valueText = new TextView(this);
        valueText.setText(value);  
        row.addView(valueText);
        
        Table.addView(row, new TableLayout.LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT));

	}
	
	
	private class GetImage extends AsyncTask<Void, String, Drawable> 
	{ 
		public String imgUrl = null;
		public ImageView imageView;
		
		public GetImage (String imgUrl, ImageView imageView) 
		{ 
			this.imgUrl = imgUrl; 
			this.imageView = imageView;
		} 
		
		protected Drawable doInBackground(Void... url) { 
			Drawable image = null;
			if (this.imgUrl == null || this.imgUrl == "")
				return null;
			try { 
				InputStream in = (InputStream) new URL(this.imgUrl).getContent(); 
				image = Drawable.createFromStream(in, "src"); 
			} catch (Exception e) { 
				Log.e(LOG_TAG, "Couldn't fetch image", e);
			} 
			return image; 
		}
		
		protected void onPostExecute (Drawable image) {
			if (image == null || imageView == null)
				return;			
			imageView.setImageDrawable(image);
		}
	}
    private class ShowDetailsTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {
        	

        	StringBuilder response = new StringBuilder();
        	for (String url : urls) {        	          	
        		DefaultHttpClient client = new DefaultHttpClient();
        		HttpGet httpGet = new HttpGet(url);
        		try {
        			HttpResponse execute = client.execute(httpGet);
        			InputStream content = execute.getEntity().getContent();

        			BufferedReader buffer = new BufferedReader(new InputStreamReader(content));
        			String s = "";
        			while ((s = buffer.readLine()) != null) {
        				response.append(s);
        			}

        		} catch (Exception e) {
        			e.printStackTrace();
        		}
        	}
        	return response.toString();
        }


        @Override
        protected void onPostExecute(String result) {
        	
    	    try {
    	            	    	
    	    	resultJSONObject = new JSONObject(result);    	        
    	    	
    	    	if (!resultJSONObject.has("result"))
    	    	{
    	    		priceDisplay.setText(ERROR_TEXT);
    	    		toggleTextVisibility(true, false, false);    	    		
    	    		return;
    	    	}
    	    	
    	    	resultJSONObject = resultJSONObject.getJSONObject("result");    	    	
    	    	if (!resultJSONObject.has("Quote") || 
    	    			!resultJSONObject.getJSONObject("Quote").has("Change"))
    	    	{
    	    		priceDisplay.setText(ERROR_TEXT);    	    		
    	    		toggleTextVisibility(true, false, false);
    	    		return;
    	    	}
    	    	
    	    	
    	    	Name = "NA";
    	    	Symbol = "";
    	    	StockLink = "http://finance.yahoo.com";
    	    	StringBuilder sb = new StringBuilder();    	    	
    	    	if (resultJSONObject.has("Name"))
    	    	{
    	    		sb.append(resultJSONObject.getString("Name"));
    	    		Name = resultJSONObject.getString("Name");
    	    		
    	    	}
   	    		if (resultJSONObject.has("Symbol"))
   	    		{
   	    			sb.append("(");
   	    			sb.append(resultJSONObject.getString("Symbol"));
   	    			sb.append(")");
   	    			Symbol = "(" + resultJSONObject.getString("Symbol") + ")";
   	    			StockLink = "http://finance.yahoo.com/q?s=" + resultJSONObject.getString("Symbol");
   	    		}
    	    	symbolDisplay.setText(sb.toString());
    	    	
    	    	
    	    	LastTradePrice = "NA";
    	    	sb = new StringBuilder();
    	    	JSONObject quoteJSONObject = resultJSONObject.getJSONObject("Quote");
    	    	if (quoteJSONObject.has("LastTradePriceOnly"))
    	    	{
    	    		sb.append(quoteJSONObject.getString("LastTradePriceOnly"));
    	    		LastTradePrice  = quoteJSONObject.getString("LastTradePriceOnly");
    	    	}
    	    	priceDisplay.setText(sb.toString());
    	    	
    	    	
    	    	ChangeType = "NA";    	    	
    	    	if (quoteJSONObject.has("ChangeType"))
    	    		ChangeType = quoteJSONObject.getString("ChangeType");
    	    	
    	    	sb = new StringBuilder();
    	    	if (quoteJSONObject.has("ChangeType") && quoteJSONObject.getString("ChangeType").compareTo("-") == 0)
    	    			sb.append(DOWN_IMAGE_LINK);    	    	
    	    	else
    	    		sb.append(UP_IMAGE_LINK);
    	    	GetImage changeImgTask = new GetImage(sb.toString(), changeImageDisplay);    	    	
    	    	changeImgTask.execute();
    	    	
    	    	stockTableDisplay.removeAllViews();
    	    	
    	    	Change = "NA";
    	    	sb = new StringBuilder();
    	    	if (quoteJSONObject.has("Change"))
    	    	{
    	    		sb.append(quoteJSONObject.getString("Change"));
    	    		Change = quoteJSONObject.getString("Change");
    	    	}

    	    	ChangeinPercent = "NA";
    	    	if (quoteJSONObject.has("ChangeinPercent"))
    	    	{
   	    			sb.append(" (");
   	    			sb.append(quoteJSONObject.getString("ChangeinPercent"));
   	    			sb.append(")");
   	    			
   	    			ChangeinPercent = quoteJSONObject.getString("ChangeinPercent");
    	    	}
    	    	changeDisplay.setText(sb.toString());        	    	
    	    	
    	    	sb = new StringBuilder();
    	    	if (quoteJSONObject.has("PreviousClose"))
    	    		sb.append(quoteJSONObject.getString("PreviousClose"));
    	    	addRowToTable(stockTableDisplay, "Prev Close", sb.toString());

    	    	sb = new StringBuilder();
    	    	if (quoteJSONObject.has("Open"))
    	    		sb.append(quoteJSONObject.getString("Open"));
    	    	addRowToTable(stockTableDisplay, "Open", sb.toString());
    	    	
    	    	sb = new StringBuilder();
    	    	if (quoteJSONObject.has("Bid"))
    	    		sb.append(quoteJSONObject.getString("Bid"));
    	    	addRowToTable(stockTableDisplay, "Bid", sb.toString());
    	    	
    	    	sb = new StringBuilder();
    	    	if (quoteJSONObject.has("Ask"))
    	    		sb.append(quoteJSONObject.getString("Ask"));
    	    	addRowToTable(stockTableDisplay, "Ask", sb.toString());
    	    	
    	    	sb = new StringBuilder();
    	    	if (quoteJSONObject.has("OneYearTargetPrice"))
    	    		sb.append(quoteJSONObject.getString("OneYearTargetPrice"));
    	    	addRowToTable(stockTableDisplay, "1yr Target Est", sb.toString());

    	    	sb = new StringBuilder();
    	    	if (quoteJSONObject.has("DaysLow") && quoteJSONObject.has("DaysHigh") 
    	    			&& quoteJSONObject.getString("DaysLow").length() != 0 && quoteJSONObject.getString("DaysHigh").length() != 0 )
    	    	{
    	    		sb.append(quoteJSONObject.getString("DaysLow"));
    	    		sb.append(" - ");
    	    		sb.append(quoteJSONObject.getString("DaysHigh"));
    	    	}
    	    	addRowToTable(stockTableDisplay, "Day Range", sb.toString());

    	    	sb = new StringBuilder();
    	    	if (quoteJSONObject.has("YearLow") && quoteJSONObject.has("YearHigh") 
    	    			&& quoteJSONObject.getString("YearLow").length() != 0 && quoteJSONObject.getString("YearHigh").length() != 0 )
    	    	{
    	    		sb.append(quoteJSONObject.getString("YearLow"));
    	    		sb.append(" - ");
    	    		sb.append(quoteJSONObject.getString("YearHigh"));
    	    	}
    	    	addRowToTable(stockTableDisplay, "52 wk Range", sb.toString());
    	    	
    	    	
    	    	sb = new StringBuilder();
    	    	if (quoteJSONObject.has("Volume"))
    	    		sb.append(quoteJSONObject.getString("Volume"));
    	    	addRowToTable(stockTableDisplay, "Volume", sb.toString());

    	    	sb = new StringBuilder();
    	    	if (quoteJSONObject.has("AverageDailyVolume"))
    	    		sb.append(quoteJSONObject.getString("AverageDailyVolume"));
    	    	addRowToTable(stockTableDisplay, "Avg Vol(3m)", sb.toString());


    	    	sb = new StringBuilder();
    	    	if (quoteJSONObject.has("MarketCapitalization"))
    	    		sb.append(quoteJSONObject.getString("MarketCapitalization"));
    	    	addRowToTable(stockTableDisplay, "Market Cap", sb.toString());

    	    	StockChartURL = "";
    	    	sb = new StringBuilder();
    	    	if (resultJSONObject.has("StockChartImageURL"))
    	    	{
    	    		sb.append(resultJSONObject.getString("StockChartImageURL"));
    	    		StockChartURL = resultJSONObject.getString("StockChartImageURL");
    	    	}
    	    	GetImage stockChartTask = new GetImage(sb.toString(), stockChartDisplay);
    	    	stockChartTask.execute();
    	    	
    	    	if (!resultJSONObject.has("News") || 
						!resultJSONObject.getJSONObject("News").has("Item"))
    	    		toggleTextVisibility(true, true, false);
    	    	else
    	    		toggleTextVisibility(true, true, true);

    	    } catch (JSONException e) {
	    		priceDisplay.setText(ERROR_TEXT);
	    		toggleTextVisibility(true, false, false);
    	    	Log.e(LOG_TAG, "Cannot process JSON results", e);
    	    }
    	    //symbolDisplay.setText(result);
        }
      }
    
    private class MyFocusChangeListener implements OnFocusChangeListener {

        public void onFocusChange(View v, boolean hasFocus){

            if(v.getId() == R.id.InputStockSearch) {

                InputMethodManager imm =  (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                if (!hasFocus)
                	imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                else
                	imm.showSoftInput(v, 0);

            }

        }
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    

	@Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
		
        String str = (String) adapterView.getItemAtPosition(position);	        
        int commaIndex = str.indexOf(',');
        commaIndex = (commaIndex < 0) ? str.length() : commaIndex;	        
        str = str.substring(0, commaIndex);
        autoCompView.setText(str);
        autoCompView.setSelection(str.length());
        
    	StringBuilder sb = new StringBuilder(SERVLET_LINK);
    	
    	try {
			
    		sb.append("?symbol=" + URLEncoder.encode(str, "utf8"));
			
		} catch (UnsupportedEncodingException e) {

			Log.e(LOG_TAG, "Encoding not supported", e);
			e.printStackTrace();
		}
    	
    	DisplayTask = new ShowDetailsTask();
    	DisplayTask.execute(sb.toString());
    }



	@Override
	public void onClick(View view) {
		
		if (view == btnSearch){
			String str = autoCompView.getText().toString().trim();
			if (str.length() == 0)
			{
				autoCompView.clearFocus();
				AlertDialog.Builder alertDialog= new AlertDialog.Builder(this);
				alertDialog.setMessage("Please enter stock symbol");
	            alertDialog.setNeutralButton("OK", new DialogInterface.OnClickListener() {
	                public void onClick(DialogInterface dialog, int which) {
	                	autoCompView.requestFocus();
	                }
	            });
	            alertDialog.show();
	            return;
			}
	    	StringBuilder sb = new StringBuilder(SERVLET_LINK);
	    	
	    	try {
				
	    		sb.append("?symbol=" + URLEncoder.encode(str, "utf8"));
				
			} catch (UnsupportedEncodingException e) {
	
				Log.e(LOG_TAG, "Encoding not supported", e);
				e.printStackTrace();
			}
	    	DisplayTask = new ShowDetailsTask();
	    	DisplayTask.execute(sb.toString());
		}
		else if (view == btnNews){
			
			if (resultJSONObject == null)
				return;
			
			try {
				
				JSONArray News = resultJSONObject.getJSONObject("News").getJSONArray("Item");
				Intent intent = new Intent(MainActivity.this, NewsActivity.class);
				intent.putExtra("NewsArray", News.toString());
				startActivity(intent);
				
			} catch (JSONException e) {

    	    	Log.e(LOG_TAG, "Cannot process JSON results", e);
			}
			
		}
		else if (view == btnFacebook){
			publishFeedDialog();
		}
		
	}
    
	private void publishFeedDialog() {
	    Bundle params = new Bundle();

	    params.putString("name", Name);
	    params.putString("caption", Name + " " + Symbol);
	    params.putString("description", "Last Trade Price: " +  LastTradePrice + ", Change: " + ChangeType + 
	    					Change + "(" + ChangeinPercent + ")");	    
	    params.putString("link", StockLink);
	    params.putString("picture", StockChartURL);

	    WebDialog feedDialog = (
	        new WebDialog.FeedDialogBuilder(this,
	            Session.getActiveSession(),
	            params))
	        .setOnCompleteListener(new OnCompleteListener() {

	            @Override
	            public void onComplete(Bundle values,
	                FacebookException error) {
	                if (error == null) {
	                    // When the story is posted, echo the success
	                    // and the post Id.
	                    final String postId = values.getString("post_id");
	                    if (postId != null) {
	                        Toast.makeText(getApplicationContext(),
	                            "Posted successfully: "+postId,
	                            Toast.LENGTH_SHORT).show();
	                    } else {
	                        // User clicked the Cancel button
	                        Toast.makeText(getApplicationContext(), 
	                            "Post cancelled", 
	                            Toast.LENGTH_SHORT).show();
	                    }
	                } else if (error instanceof FacebookOperationCanceledException) {
	                    // User clicked the "x" button
	                    Toast.makeText(getApplicationContext(), 
	                        "Post cancelled", 
	                        Toast.LENGTH_SHORT).show();
	                } else {
	                    // Generic, ex: network error
	                    Toast.makeText(getApplicationContext(), 
	                        "Error posting story", 
	                        Toast.LENGTH_SHORT).show();
	                }
	            }

	        })
	        .build();
	    feedDialog.show();
	}
}
