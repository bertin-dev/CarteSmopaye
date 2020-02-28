package com.example.cartesmopaye;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.net.Uri;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
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
import java.util.Calendar;
import java.util.List;
import java.util.Random;

import static java.sql.Types.NULL;

public class EcrireCarte extends AppCompatActivity {

    private Button BtnEcrireCarte, BtnEcrireCarteSaveBDAuto;
    private EditText numCartePrive, numCartePublic, numCartePriveAuto, numCartePublicAuto;
    private static final String encrypted_password = "Iyz4BVU2Hlt0cIeIPBlB7Wq15kMDI4NGRmOTNi";
    private static final String salt = "d0284df93b";

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
    private final String TAG = "EcrireCarte";
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ecrire_carte);

        BtnEcrireCarte = (Button) findViewById(R.id.BtnEcrireCarte);
        numCartePrive = (EditText) findViewById(R.id.numCartePrive);
        numCartePublic = (EditText) findViewById(R.id.numCartePublic);

        BtnEcrireCarteSaveBDAuto = (Button) findViewById(R.id.BtnEcrireCarteSaveBDAuto);
        numCartePriveAuto = (EditText) findViewById(R.id.numCartePriveAuto);
        numCartePublicAuto = (EditText) findViewById(R.id.numCartePublicAuto);

        progressDialog = new ProgressDialog(EcrireCarte.this);

        BtnEcrireCarte.setEnabled(false);
        BtnEcrireCarte.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(numCartePrive.length() <= 0 || numCartePublic.length() <= 0){
                    Toast.makeText(EcrireCarte.this, getString(R.string.champsVide), Toast.LENGTH_SHORT).show();
                }
                else {

                    if(numCartePrive.length() < 8 ){
                        Toast.makeText(EcrireCarte.this, getString(R.string.nbrCaractere) + " 8", Toast.LENGTH_SHORT).show();
                    }

                    else if(numCartePublic.length() < 32){
                        Toast.makeText(EcrireCarte.this, getString(R.string.nbrCaractere) + " 32", Toast.LENGTH_SHORT).show();
                    }
                    else {
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
                }


            }
        });



     //ECRIRE ET SAUVEGARDER AUTOMATIQUEMENT DANS LA BD
        BtnEcrireCarteSaveBDAuto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(!numCartePriveAuto.getText().toString().equalsIgnoreCase(""))
                    numCartePriveAuto.setText("");
                if(!numCartePublicAuto.getText().toString().equalsIgnoreCase(""))
                    numCartePublicAuto.setText("");


                //Compte a rebour de 30 secondes
                /*new CountDownTimer(30000, 1000) {
                    public void onTick(long millisUntilFinished) {
                        numCartePriveAuto.setText(millisUntilFinished / 1000 + "");
                        numCartePublicAuto.setText(millisUntilFinished / 1000 + "");
                    }
                    public void onFinish() {
                        Toast.makeText(EcrireCarte.this, "Time Over", Toast.LENGTH_SHORT).show();
                    }
                }.start();*/


                Calendar rightNow = Calendar.getInstance();

                long offset = rightNow.get(Calendar.ZONE_OFFSET) +  rightNow.get(Calendar.DST_OFFSET);

                String sinceMidnight = Long.toString((rightNow.getTimeInMillis() + offset) %  (24 * 60 * 60 * 1000));


                String resultatLetter = genererLetter("abcdef");

                String resultTotal = resultatLetter.toUpperCase() + sinceMidnight;
                numCartePriveAuto.setText(resultTotal.substring(0, 8));

                // numCartePublicAuto.setText(result);

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

                            /*idCarte.setText(StringUtil.toHexString(uid));
                            typeCarte1.setText( "A " + type);
                            atqa1.setText(StringUtil.toHexString(atqa));
                            sak1.setText(StringUtil.toHexString(sak));
                            m1CardAuthenticate();*/

                            m1CardAuthenticate();
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


    private String genererLetter(String alphabet){
       // String alphabet= "abcdefghijklmnopqrstuvwxyz";
        String s = "";
        Random random = new Random();
        int randomLen = 1+random.nextInt(9);
        for (int i = 0; i < randomLen; i++) {
            char c = alphabet.charAt(random.nextInt(6));
            s+=c;
        }
        return s;
    }



    public static String generateCode() {

        String alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        String fullalphabet = alphabet + alphabet.toLowerCase() + "123456789";
        Random random = new Random();

        char code = fullalphabet.charAt(random.nextInt(9));

        return Character.toString(code);

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


    public void m1CardAuthenticate() {
        Boolean status = true;
        byte[] passwd = {(byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff};
        try {

            time1 = System.currentTimeMillis();
            nfc.m1_authenticate(blockNum_1, (byte) 0x0B, passwd);//0x0B
            time2 = System.currentTimeMillis();
            Log.e("yw m1_authenticate", (time2 - time1) + "");


        } catch (TelpoException e) {
            status = false;
            e.printStackTrace();
            Log.e("yw", e.toString());
        }

        if (status) {
            Log.d(TAG, "m1CardAuthenticate success!");
            //writeDataPublic();

            Toast.makeText(this, "N°: " + readIDPrive(), Toast.LENGTH_SHORT).show();

            if(readIDPrive().equalsIgnoreCase("00000000"))
                writeDataPrive();
            else
            {
                new AlertDialog.Builder(EcrireCarte.this)
                        .setMessage("Votre Carte contient déjà du contenu")
                        .setPositiveButton("OK", null)
                        .setCancelable(false)
                        .show();
                Toast.makeText(this, "Votre Carte contient déjà du contenu", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, getString(R.string.operation_fail), Toast.LENGTH_SHORT).show();
            Log.d(TAG, "m1CardAuthenticate fail!");
        }
    }


    public void writeDataPublic() {
        byte[] blockData = null;
        String blockStr;
        Boolean status = true;

        Log.d(TAG, "writeBlockData...");
        blockStr = numCartePublic.getText().toString();
        blockData = toByteArray(blockStr);

        try {
            nfc.m1_write_block(blockNum_1, blockData, blockData.length);
        } catch (TelpoException e) {
            status = false;
            Log.e("yw", e.toString());
            e.printStackTrace();
        }

        if (status) {
            Log.d(TAG, "writeBlockData success!");
            Toast.makeText(this, getString(R.string.operation_succss), Toast.LENGTH_SHORT).show();
        } else {
            Log.e(TAG, "writeBlockData fail!");
            Toast.makeText(this, getString(R.string.operation_fail), Toast.LENGTH_SHORT).show();
        }
    }



    public static byte[] toByteArray(String hexString) {
        int hexStringLength = hexString.length();
        byte[] byteArray = null;
        int count = 0;
        char c;
        int i;

        // Count number of hex characters
        for (i = 0; i < hexStringLength; i++) {
            c = hexString.charAt(i);
            if (c >= '0' && c <= '9' || c >= 'A' && c <= 'F' || c >= 'a' && c <= 'f') {
                count++;
            }
        }

        byteArray = new byte[(count + 1) / 2];
        boolean first = true;
        int len = 0;
        int value;
        for (i = 0; i < hexStringLength; i++) {
            c = hexString.charAt(i);
            if (c >= '0' && c <= '9') {
                value = c - '0';
            } else if (c >= 'A' && c <= 'F') {
                value = c - 'A' + 10;
            } else if (c >= 'a' && c <= 'f') {
                value = c - 'a' + 10;
            } else {
                value = -1;
            }

            if (value >= 0) {

                if (first) {
                    byteArray[len] = (byte) (value << 4);
                } else {
                    byteArray[len] |= value;
                    len++;
                }
                first = !first;
            }
        }
        return byteArray;
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


    public void writeDataPrive() {
        byte[] valueData = null;
        String valueStr;
        boolean status = true;

        Log.d(TAG, "writeValueBtn...");
        valueStr = numCartePriveAuto.getText().toString();
        //valueStr = "00000000"; //initialisation
        valueData = toByteArray(valueStr);

        try {
            nfc.m1_write_value(blockNum_2, valueData, valueData.length);
            //insertDataInDB(valueStr);
        } catch (TelpoException e) {
            status = false;
            e.printStackTrace();
        }

        if (status) {
            Log.d(TAG, "writeValueData success!");
            Toast.makeText(this, getString(R.string.operation_succss), Toast.LENGTH_SHORT).show();
        } else {
            Log.e(TAG, "writeValueData fail!");
            Toast.makeText(this, getString(R.string.operation_fail), Toast.LENGTH_SHORT).show();
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


    private void insertDataInDB(final String idPrive){

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    final Uri.Builder builder = new Uri.Builder();


                    builder.appendQueryParameter("auth", "Card");
                    builder.appendQueryParameter("login", "register");
                    builder.appendQueryParameter("CARDN",idPrive);
                    builder.appendQueryParameter("fgfggergJHGS",encrypted_password);
                    builder.appendQueryParameter("uhtdgG18",salt);

                    //Connexion au serveur
                    URL url = new URL("http://192.168.20.6:1234/index.php"+builder.build().toString());
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
                            Toast.makeText(EcrireCarte.this, "ooooo", Toast.LENGTH_SHORT).show();
                        }
                    });

                    while (bufferedReader.ready() || data==""){
                        data+=bufferedReader.readLine();
                    }
                    bufferedReader.close();
                    inputStream.close();


                    final String f = data;

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            new AlertDialog.Builder(EcrireCarte.this)
                                    .setMessage(f)
                                    .setPositiveButton("OK", null)
                                    .setCancelable(false)
                                    .show();
                            Toast.makeText(EcrireCarte.this, f, Toast.LENGTH_SHORT).show();
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
                                    Toast.makeText(EcrireCarte.this, jsonObject.getString("montant"), Toast.LENGTH_SHORT).show();
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
                            Toast.makeText(EcrireCarte.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                    e.printStackTrace();
                    try {
                        Thread.sleep(2000);
                        // Toast.makeText(Consulter_Solde.this, "Impossible de se connecter au serveur", Toast.LENGTH_SHORT).show();
                    } catch (InterruptedException e1) {
                        e1.printStackTrace();
                    }
                    progressDialog.dismiss();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }).start();
        //-----------FIN
    }

}
