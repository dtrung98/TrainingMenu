package com.zalo.servicetraining.fundamental.weather;


import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.zalo.servicetraining.R;
import com.zalo.servicetraining.model.LocalArea;
import com.zalo.servicetraining.mainui.base.AbsListActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;


import es.dmoral.toasty.Toasty;

public class DemoWeatherApp extends AbsListActivity implements LocalAreaAdapter.OnItemClickListener {
    private static final String TAG = "DemoWeatherApp";

    public static final int DETAIL_REQUEST_CODE = 1;

    FloatingActionButton mAddButton;

    private LocalAreaAdapter mAdapter;


    @Override
    protected void onInitRecyclerView() {
        getRecyclerView().setLayoutManager(new GridLayoutManager(this,2,RecyclerView.VERTICAL,false));

        mAdapter = new LocalAreaAdapter();
        mAdapter.setListener(this);
        getRecyclerView().setAdapter(mAdapter);
    }

    @Override
    protected void refreshData() {
        getSwipeRefreshLayout().setRefreshing(true);
        if(mTask!=null) mTask.cancel();
        mTask = new LoadWeatherDataTask(this);
        mTask.execute();

    }

    private void reportResult(List<LocalArea> list) {
        Log.d(TAG, "reportResult: ");
        getSwipeRefreshLayout().setRefreshing(false);
        mAdapter.setData(list);
    }

    private LoadWeatherDataTask mTask = null;

    @Override
    protected void onDestroy() {

        super.onDestroy();
    }

    @Override
    public void onLocalAreaItemClick(LocalArea item) {
        Toasty.success(this,"looking weather for "+item.getLocalizedName()).show();
    }

    @Override
    protected int title() {
        return R.string.weather;
    }

    private static class LoadWeatherDataTask extends AsyncTask<Void,Void,List<LocalArea>> {
        private final WeakReference<DemoWeatherApp> mWeakRefWeatherApp;
        LoadWeatherDataTask(DemoWeatherApp app) {
            mWeakRefWeatherApp = new WeakReference<>(app);
        }

        public void cancel() {
        cancel(true);
        if(mWeakRefWeatherApp.get()!=null) {
            mWeakRefWeatherApp.get().mTask = null;
            mWeakRefWeatherApp.clear();
        }
        }

        @Override
        protected List<LocalArea> doInBackground(Void... voids) {
            String result = null;

            Uri uri = new Uri.Builder()
                    .scheme("https")
                    .authority("dataservice.accuweather.com")
                    .path("/locations/v1/adminareas/VN")
                    .appendQueryParameter("apikey","GACdKl5Xg9cNKJTc9batyPSSfIOZeEEo")
                    .appendQueryParameter("language","vi-VN")
                    .build();
            URL url;

            try {
                url= new URL(uri.toString());
            } catch (MalformedURLException e) {
                url = null;
            }

            if(url!=null) {
                try {
                    result = makeHttpRequest(url);
                    Log.d(TAG, "doInBackground: " + result);
                } catch (IOException ignored) {
                    result = null;
                }
            }
           if(result==null) return null;
           return parseResponse(result);
        }

        @Override
        protected void onPostExecute(List<LocalArea> list) {
            DemoWeatherApp app = mWeakRefWeatherApp.get();
            if(app!=null) app.reportResult(list);
        }

        private String makeHttpRequest(URL url) throws IOException {
            String response = "";
            HttpsURLConnection urlConnection = null;
            InputStream inputStream = null;

            try {
                urlConnection = (HttpsURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.setReadTimeout(10000);
                urlConnection.setConnectTimeout(15000);
                urlConnection.connect();
                if (urlConnection.getResponseCode() == 200) {
                    inputStream = urlConnection.getInputStream();
                    response = readTextFromInputStream(inputStream);
                }
            } catch (IOException ignored) {
            } finally {
                if (urlConnection != null) urlConnection.disconnect();
                if (inputStream != null) inputStream.close();
            }
            return response;
        }

        private String readTextFromInputStream(InputStream inputStream) {
            StringBuilder output = new StringBuilder();
            if (inputStream != null) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader reader = new BufferedReader(inputStreamReader);
                String line = null;
                do {
                    try {
                        line = reader.readLine();
                    } catch (IOException ignored) {
                    }

                    if (line != null)
                        output.append(line);
                } while (line != null);
            }
            return output.toString();
        }
        private ArrayList<LocalArea> parseResponse(String response) {
            ArrayList<LocalArea> list = new ArrayList<>();
            try {
                JSONArray jsonArray = new JSONArray(response);
                int length = jsonArray.length();
                for(int i = 0; i < length; i++) {
                    JSONObject jo = jsonArray.getJSONObject(i);
                    LocalArea localArea = new LocalArea();
                    localArea.setID(jo.getString("ID"));
                    localArea.setLocalizedName(jo.getString("LocalizedName"));
                    localArea.setEnglishName(jo.getString("EnglishName"));
                    localArea.setLevel(jo.getInt("Level"));
                    localArea.setLocalizedType(jo.getString("LocalizedType"));
                    localArea.setEnglishType(jo.getString("EnglishType"));
                    localArea.setCountryID(jo.getString("CountryID"));

                    list.add(localArea);
                }
            } catch (JSONException ignored) {}

            return list;
        }
    }


}
