package com.example.cartesmopaye;

import android.app.ProgressDialog;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.telpo.tps550.api.TelpoException;
import com.telpo.tps550.api.nfc.Nfc;
import com.telpo.tps550.api.util.StringUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class ListCarte extends AppCompatActivity {

    ListView listView;
    String [] data1, data2;
    ArrayAdapter<String> adapter;
    ProgressBar progressBar;
    List listUser = new ArrayList();
    EditText editText ;
    Button btnSaveData;
    private ProgressDialog progressDialog;
    ///////////////////////////////////////////
    private EditText uid_editText = null;
    Thread readThread;
    Handler handler;
    private final int CHECK_NFC_TIMEOUT = 1;
    private final int SHOW_NFC_DATA = 2;
    private byte blockNum_1 = 1;
    private byte blockNum_2 = 2;
    private final byte B_CPU = 3;
    private final byte A_CPU = 1;
    private final byte A_M1 = 2;
    Nfc nfc = new Nfc(this);
    long time1, time2;
    private final String TAG = "ListCarte";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_carte);


        listView = (ListView) findViewById(R.id.listViewContent);
        progressBar = (ProgressBar)findViewById(R.id.progressBar);
        editText = (EditText)findViewById(R.id.edittext1);
        btnSaveData = (Button) findViewById(R.id.BtnSaveBD);
        progressDialog = new ProgressDialog(ListCarte.this);
        // Calling Method to Parese JSON data into listView.
        new GetHttpResponse(ListCarte.this).execute();


        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                //Updating Array Adapter ListView after typing inside EditText.
                ListCarte.this.adapter.getFilter().filter(s);
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(s.toString().equals("")){
                    ListCarte.this.adapter.getFilter().filter(s);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });



        btnSaveData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    nfc.open();

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            // On ajoute un message à notre progress dialog
                            progressDialog.setMessage("Passer la carte");
                            // On donne un titre à notre progress dialog
                            progressDialog.setTitle("En attente de carte");
                            // On spécifie le style
                            //  progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                            // On affiche notre message
                            progressDialog.show();
                            //build.setPositiveButton("ok", new View.OnClickListener()
                        }
                    });
                    readThread = new ReadThread();
                    readThread.start();

                } catch (TelpoException e) {
                    e.printStackTrace();
                }
            }
        });


        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case CHECK_NFC_TIMEOUT: {
                        Toast.makeText(getApplicationContext(), "Check card time out!", Toast.LENGTH_LONG).show();
                       /* open_btn.setEnabled(true);
                        close_btn.setEnabled(false);
                        check_btn.setEnabled(false);*/
                    }
                    break;
                    case SHOW_NFC_DATA: {
                        byte[] uid_data = (byte[]) msg.obj;
                        if (uid_data[0] == 0x42) {
                            // TYPE B类（暂时只支持cpu卡）
                            byte[] atqb = new byte[uid_data[16]];
                            byte[] pupi = new byte[4];
                            String type = null;

                            System.arraycopy(uid_data, 17, atqb, 0, uid_data[16]);
                            System.arraycopy(uid_data, 29, pupi, 0, 4);

                            if (uid_data[1] == B_CPU) {
                                type = "CPU";
                               /* sendApduBtn.setEnabled(true);
                                getAtsBtn.setEnabled(true);*/
                            } else {
                                type = "unknow";
                            }

                            /*new AlertDialog.Builder(EnregCarte.this)
                                    .setMessage(getString(R.string.card_type) + getString(R.string.type_b) + " " + type +
                                            "\r\n" + getString(R.string.atqb_data) + StringUtil.toHexString(atqb) +
                                            "\r\n" + getString(R.string.pupi_data) + StringUtil.toHexString(pupi))
                                    .setPositiveButton("OK", null)
                                    .setCancelable(false)
                                    .show();*/

                            uid_editText.setText(getString(R.string.card_type) + getString(R.string.type_b) + " " + type +
                                    "\r\n" + getString(R.string.atqb_data) + StringUtil.toHexString(atqb) +
                                    "\r\n" + getString(R.string.pupi_data) + StringUtil.toHexString(pupi));

                            progressDialog.dismiss();

                        } else if (uid_data[0] == 0x41) {
                            // TYPE A类（CPU, M1）
                            byte[] atqa = new byte[2];
                            byte[] sak = new byte[1];
                            byte[] uid = new byte[uid_data[5]];
                            String type = null;

                            System.arraycopy(uid_data, 2, atqa, 0, 2);
                            System.arraycopy(uid_data, 4, sak, 0, 1);
                            System.arraycopy(uid_data, 6, uid, 0, uid_data[5]);

                            if (uid_data[1] == A_CPU) {
                                type = "CPU";
                                /*sendApduBtn.setEnabled(true);
                                getAtsBtn.setEnabled(true);*/
                            } else if (uid_data[1] == A_M1) {
                                type = "M1";
                                // authenticateBtn.setEnabled(true);
                            } else {
                                type = "unknow";
                            }

                          /*  new AlertDialog.Builder(Recharge.this)
                                   .setMessage(getString(R.string.card_type) + getString(R.string.type_a) + " " + type +
                                            "\r\n" + getString(R.string.atqa_data) + StringUtil.toHexString(atqa) +
                                            "\r\n" + getString(R.string.sak_data) + StringUtil.toHexString(sak) +
                                            "\r\n" + getString(R.string.uid_data) + StringUtil.toHexString(uid))
                                    .setPositiveButton("OK", null)
                                    .setCancelable(false)
                                    .show();*/


                          if(readIDPrive() != null && readIDPublic() != null)
                            setData(StringUtil.toHexString(uid), readIDPrive(), readIDPublic());

                            progressDialog.dismiss();
                            try {
                                nfc.close();
                            } catch (TelpoException e) {
                                e.printStackTrace();
                            }

                        } else {
                            Log.e(TAG, "unknow type card!!");
                        }
                    }
                    break;

                    default:
                        break;
                }
            }
        };

    }


    // Creating GetHttpResponse message to parse JSON.
    public class GetHttpResponse extends AsyncTask<Void, Void, Void>
    {
        // Creating context.
        public Context context;

        // Creating string to hold Http response result.
        String ResultHolder;

        // Creating constructor .
        public GetHttpResponse(Context context)
        {
            this.context = context;
        }

        @Override
        protected void onPreExecute()
        {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... arg0)
        {
            getData();
            return null;
        }

        // This block will execute after done all background processing.
        @Override
        protected void onPostExecute(Void result)

        {
            // Hiding the progress bar after done loading JSON.
            //progressBar.setVisibility(View.GONE);

            // Showing the ListView after done loading JSON.
            //listView.setVisibility(View.VISIBLE);

            // Setting up the SubjectArrayList into Array Adapter.
            // arrayAdapter = new ArrayAdapter(ViewListContents.this,android.R.layout.simple_list_item_1, android.R.id.text1, SubjectArrayList);

            // Passing the Array Adapter into ListView.
            //listView.setAdapter(arrayAdapter);


            //ADAPTER
            //Toast.makeText(ViewListContents.this, listUser.toString(), Toast.LENGTH_SHORT).show();
            // adapter = new ArrayAdapter<String>(ViewListContents.this, android.R.layout.simple_list_item_1, android.R.id.text1, listUser);
            //listView.setAdapter(adapter);

        }
    }

    @Override
    protected void onDestroy() {
        try {
            nfc.close();
        } catch (TelpoException e) {
            e.printStackTrace();
        }
        super.onDestroy();
    }


    private void getData(){

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    final Uri.Builder builder = new Uri.Builder();
                    //builder.appendQueryParameter("pseudo",login);




                    //Connexion au serveur
                    URL url = new URL("http://192.168.20.11:1234/listing.php"+builder.build().toString());;
                    HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                    httpURLConnection.setConnectTimeout(5000);
                    httpURLConnection.setRequestMethod("POST");
                    httpURLConnection.connect();

                    InputStream inputStream = httpURLConnection.getInputStream();

                    final BufferedReader bufferedReader  =  new BufferedReader(new InputStreamReader(inputStream));

                    String string="";
                    String data="";
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(), "ooooo", Toast.LENGTH_SHORT).show();
                        }
                    });

                    while (bufferedReader.ready() || data==""){
                        data+=bufferedReader.readLine();
                    }
                    bufferedReader.close();
                    inputStream.close();


                    final String f = data;
                    // boolean d=data;

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            // Toast.makeText(getApplicationContext(), f, Toast.LENGTH_LONG).show();

                            //PARSE JSON DATA
                            try{
                                JSONArray ja = new JSONArray(f);
                                JSONObject jo = null;
                                List listUser2 = new ArrayList();

                                data1 = new String[ja.length()];
                                data2 = new String[ja.length()];

                                for(int i=0; i<ja.length(); i++){
                                    jo = ja.getJSONObject(i);
                                    data1[i] = jo.getString("ID_USER");
                                    data2[i] = jo.getString("NOM");
                                    //listUser.add(data1[i]);
                                    //Toast.makeText(ViewListContents.this, listUser.toString(), Toast.LENGTH_SHORT).show();
                                    //listUser2.add(data1[i] +  "-" +  data2[i]);
                                    listUser2.add(data2[i]);
                                }


                                adapter = new ArrayAdapter<String>(ListCarte.this, android.R.layout.simple_list_item_1, android.R.id.text1, listUser2);
                                listView.setAdapter(adapter);
                                progressBar.setVisibility(View.GONE);

                            }
                            catch (JSONException e) {
                                e.printStackTrace();
                            }


                        }


                    });

                    //    JSONObject jsonObject = new JSONObject(data);
                    //  jsonObject.getString("status");
                    JSONArray jsonArray = new JSONArray(data);
                    for (int i=0;i<jsonArray.length();i++){
                        final JSONObject jsonObject = jsonArray.getJSONObject(i);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    Toast.makeText(getApplicationContext(), jsonObject.getString("montant"), Toast.LENGTH_SHORT).show();
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                    }
                } catch (final IOException e) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                    e.printStackTrace();
                    try {
                        Thread.sleep(2000);
                        //Toast.makeText(Inscription.this, "Impossible de se connecter au serveur", Toast.LENGTH_SHORT).show();
                    } catch (InterruptedException e1) {
                        e1.printStackTrace();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
    private void setData(final String idCarte, final String idPrive, final String idPublic){

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    final Uri.Builder builder = new Uri.Builder();
                    builder.appendQueryParameter("id",idCarte);
                    builder.appendQueryParameter("idPrive",idPrive);
                    builder.appendQueryParameter("idPublic",idPublic);

                    //Connexion au serveur
                    URL url = new URL("http://192.168.20.6:1234/listing.php"+builder.build().toString());;
                    HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                    httpURLConnection.setConnectTimeout(5000);
                    httpURLConnection.setRequestMethod("POST");
                    httpURLConnection.connect();

                    InputStream inputStream = httpURLConnection.getInputStream();

                    final BufferedReader bufferedReader  =  new BufferedReader(new InputStreamReader(inputStream));

                    String string="";
                    String data="";
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(), "ooooo", Toast.LENGTH_SHORT).show();
                        }
                    });

                    while (bufferedReader.ready() || data==""){
                        data+=bufferedReader.readLine();
                    }
                    bufferedReader.close();
                    inputStream.close();


                    final String f = data;
                    // boolean d=data;

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            // Toast.makeText(getApplicationContext(), f, Toast.LENGTH_LONG).show();

                            //PARSE JSON DATA
                            try{
                                JSONArray ja = new JSONArray(f);
                                JSONObject jo = null;
                                List listUser2 = new ArrayList();

                                data1 = new String[ja.length()];
                                data2 = new String[ja.length()];

                                for(int i=0; i<ja.length(); i++){
                                    jo = ja.getJSONObject(i);
                                    data1[i] = jo.getString("ID_USER");
                                    data2[i] = jo.getString("NOM");
                                    //listUser.add(data1[i]);
                                    //Toast.makeText(ViewListContents.this, listUser.toString(), Toast.LENGTH_SHORT).show();
                                    //listUser2.add(data1[i] +  "-" +  data2[i]);
                                    listUser2.add(data2[i]);
                                }


                                adapter = new ArrayAdapter<String>(ListCarte.this, android.R.layout.simple_list_item_1, android.R.id.text1, listUser2);
                                listView.setAdapter(adapter);
                                progressBar.setVisibility(View.GONE);

                            }
                            catch (JSONException e) {
                                e.printStackTrace();
                            }


                        }


                    });

                    //    JSONObject jsonObject = new JSONObject(data);
                    //  jsonObject.getString("status");
                    JSONArray jsonArray = new JSONArray(data);
                    for (int i=0;i<jsonArray.length();i++){
                        final JSONObject jsonObject = jsonArray.getJSONObject(i);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    Toast.makeText(getApplicationContext(), jsonObject.getString("montant"), Toast.LENGTH_SHORT).show();
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                    }
                } catch (final IOException e) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                    e.printStackTrace();
                    try {
                        Thread.sleep(2000);
                        //Toast.makeText(Inscription.this, "Impossible de se connecter au serveur", Toast.LENGTH_SHORT).show();
                    } catch (InterruptedException e1) {
                        e1.printStackTrace();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }




    private class ReadThread extends Thread {
        byte[] nfcData = null;

        @Override
        public void run() {
            try {

                time1 = System.currentTimeMillis();
                nfcData = nfc.activate(10 * 1000); // 10s
                time2 = System.currentTimeMillis();
                Log.e("yw activate", (time2 - time1) + "");
                if (null != nfcData) {
                    handler.sendMessage(handler.obtainMessage(SHOW_NFC_DATA, nfcData));
                } else {
                    Log.d(TAG, "Check Card timeout...");
                    handler.sendMessage(handler.obtainMessage(CHECK_NFC_TIMEOUT, null));
                }
            } catch (TelpoException e) {
                Log.e("yw", e.toString());
                e.printStackTrace();
            }
        }
    }


    public String readIDPrive() {
        byte[] data = null;
        try {
            data = nfc.m1_read_value(blockNum_2);
        } catch (TelpoException e) {
            e.printStackTrace();
        }

        if (null == data) {
            Log.e(TAG, "readValueBtn fail!");
            Toast.makeText(this, getString(R.string.operation_fail), Toast.LENGTH_SHORT).show();
            return null;
        } else {
            //numCarteCourt.setText(StringUtil.toHexString(data));
            return StringUtil.toHexString(data);
        }
    }

    public String readIDPublic() {
        byte[] data = null;
        try {

            time1 = System.currentTimeMillis();
            data = nfc.m1_read_block(blockNum_1);
            time2 = System.currentTimeMillis();
            Log.e("yw read_block", (time2 - time1) + "");


        } catch (TelpoException e) {
            e.printStackTrace();
        }

        if (data == null) {
            Log.e(TAG, "readBlockBtn fail!");
            Toast.makeText(this, getString(R.string.operation_fail), Toast.LENGTH_SHORT).show();
            return null;
        } else {
            //numCarteLong.setText(StringUtil.toHexString(data));
            return StringUtil.toHexString(data);
        }
    }

}
