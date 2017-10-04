package com.example.ashishpsah.testweather;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    EditText emailText;
    TextView responseView,textViewLoc,textViewCond,textViewRefresh;
    ImageView fetch_image_cond_logo,fetch_image_refresh;
    ProgressBar progressBar;
    String city,API_URL;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        responseView = (TextView) findViewById(R.id.responseView);
        textViewLoc = (TextView) findViewById(R.id.textViewLoc);
        textViewCond = (TextView) findViewById(R.id.textViewCond);
        textViewRefresh = (TextView) findViewById(R.id.textViewRefresh);
        emailText = (EditText) findViewById(R.id.emailText);
        fetch_image_cond_logo = (ImageView) findViewById(R.id.fetch_image_cond_logo);
        fetch_image_refresh = (ImageView) findViewById(R.id.fetch_image_refresh);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        Button queryButton = (Button) findViewById(R.id.queryButton);
        queryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                city = emailText.getText().toString();
                if(!(city.isEmpty())){
                API_URL = "https://query.yahooapis.com/v1/public/yql?q=select%20*%20from%20weather.forecast%20where%20woeid%20in%20(select%20woeid%20from%20geo.places(1)%20where%20text%3D%22"+city+"%22)&format=json&env=store%3A%2F%2Fdatatables.org%2Falltableswithkeys";
                new RetrieveFeedTask().execute();}
                else {
                    Toast.makeText(MainActivity.this,"Please fill valid city",Toast.LENGTH_LONG).show();
                }
            }
        });
        fetch_image_refresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                API_URL = "https://query.yahooapis.com/v1/public/yql?q=select%20*%20from%20weather.forecast%20where%20woeid%20in%20(select%20woeid%20from%20geo.places(1)%20where%20text%3D%22"+city+"%22)&format=json&env=store%3A%2F%2Fdatatables.org%2Falltableswithkeys";
                new RetrieveFeedTask().execute();
            }
        });
    }

    class RetrieveFeedTask extends AsyncTask<Void, Void, String> {



        protected void onPreExecute() {
            progressBar.setVisibility(View.VISIBLE);
            responseView.setText("");
        }

        protected String doInBackground(Void... urls) {

            try {
                URL url = new URL(API_URL);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                try {
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                    StringBuilder stringBuilder = new StringBuilder();
                    String line;
                    while ((line = bufferedReader.readLine()) != null) {
                        stringBuilder.append(line).append("\n");
                    }
                    bufferedReader.close();
                    return stringBuilder.toString();
                }
                finally{
                    urlConnection.disconnect();
                }
            }
            catch(Exception e) {
                Log.e("ERROR", e.getMessage(), e);
                return null;
            }
        }

        protected void onPostExecute(String response) {
            if(response == null) {
                response = "THERE WAS AN ERROR";
            }
            progressBar.setVisibility(View.GONE);
            Log.i("INFO", response);
            try {
                JSONObject jsonObj = new JSONObject(response);
                JSONObject tempquery = jsonObj.getJSONObject("query").getJSONObject("results").getJSONObject("channel").getJSONObject("item").getJSONObject("condition");
                JSONObject locquery = jsonObj.getJSONObject("query").getJSONObject("results").getJSONObject("channel").getJSONObject("location");
                String location = locquery.getString("city")+", "+locquery.getString("country");
                String temperature = tempquery.getString("temp");
                String condition = tempquery.getString("text");
                String code =tempquery.getString("code");
                Date currentTime = Calendar.getInstance().getTime();
                SimpleDateFormat timeformat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String refreshedOn = timeformat.format(currentTime);
                int c = Integer.parseInt(temperature);
                c = (int)((c-32)/1.8);
                responseView.setText(""+c+"^C");
                textViewLoc.setText(location);
                textViewCond.setText(condition);
                textViewRefresh.setText("Refreshed on:\n"+refreshedOn);
                int resourceId = getResources().getIdentifier("drawable/l"+code,null,getPackageName());
                fetch_image_cond_logo.setImageResource(resourceId);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}

