/*
 * Copyright 2017 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.zalo.servicetraining.fundamental.userlist;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;

import android.view.View;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.zalo.servicetraining.R;
import com.zalo.servicetraining.model.UserData;
import com.zalo.servicetraining.mainui.base.AbsListActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class UserDataListActivity extends AbsListActivity implements UserDataAdapter.ItemClickListener{
    private static final String TAG="UserDataListActivity";
    private static final String JSON_FILE = "data.json";

    UserDataAdapter mAdapter;

    @Override
    protected void onInitRecyclerView() {
        getRecyclerView().setLayoutManager(new LinearLayoutManager(this,RecyclerView.VERTICAL,false));
        mAdapter = new UserDataAdapter();
        mAdapter.setClickListener(this);
        getRecyclerView().setAdapter(mAdapter);
    }

    @Override
    protected void refreshData() {
        String json = readFileFromAsset(this, JSON_FILE);
        mAdapter.setData(convertJSonToArray(json));
        getSwipeRefreshLayout().setRefreshing(false);
    }

    @Override
    public void onItemClick(View view, int position, Bitmap avatar, String username, int id) {
        ByteArrayOutputStream bStream = new ByteArrayOutputStream();
        avatar.compress(Bitmap.CompressFormat.PNG, 100, bStream);
        byte[] byteArray = bStream.toByteArray();

        Intent anotherIntent = new Intent(this, UserProfileActivity.class);
        anotherIntent.putExtra("avatar", byteArray);
        anotherIntent.putExtra("username",username);
        anotherIntent.putExtra("id",id);
        startActivity(anotherIntent);

    }

    private static ArrayList<UserData> convertJSonToArray(String jsonString) {
        ArrayList<UserData> arrayList= new ArrayList<>();
        try {

            JSONArray jsonArray = new JSONArray(jsonString);
            for (int i=0;i<jsonArray.length();i++) {
              JSONObject jo = jsonArray.getJSONObject(i);
              arrayList.add(new UserData(jo.getInt("userId"),jo.getString("displayName"),jo.getString("avatar")));

            }
         } catch (JSONException e) {
            e.printStackTrace();
        }
        return arrayList;
    }

    /**
     * Read String File From Assets
     * @param fileName the name of the file
     * @return the text content of the jsong
     */
    private static String readFileFromAsset(Context context, String fileName){

        BufferedReader reader = null;
        InputStream inputStream = null;
        StringBuilder builder = new StringBuilder();

        try{
            inputStream = context.getAssets().open(fileName);
            reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;

            while((line = reader.readLine()) != null)
            {
                builder.append(line);
                builder.append("\n");
            }
        } catch (IOException ioe){
            ioe.printStackTrace();
        } finally {

            if(inputStream != null)
            {
                try {
                    inputStream.close();
                } catch (IOException ioe){
                    ioe.printStackTrace();
                }
            }

            if(reader != null)
            {
                try {
                    reader.close();
                } catch (IOException ioe)
                {
                    ioe.printStackTrace();
                }
            }
        }
        return builder.toString();
    }

    @Override
    protected int title() {
        return R.string.users;
    }
}
