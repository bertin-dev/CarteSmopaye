package com.example.cartesmopaye;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.telpo.tps550.api.TelpoException;
import com.telpo.tps550.api.nfc.Nfc;
import com.telpo.tps550.api.util.StringUtil;

import java.text.SimpleDateFormat;
import java.util.Date;

import static android.content.ContentValues.TAG;

public class EnregCarte extends AppCompatActivity {


    private EditText idCarte, numCarteLong, numCarteCourt, typeCarte1, atqa1, sak1;
    private Button BtnOpenNFC;
    private AlertDialog.Builder build;
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
    private final String TAG = "EnregCarte";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_enreg_carte);

        idCarte = (EditText) findViewById(R.id.idCarte);
        numCarteLong = (EditText) findViewById(R.id.numCarteLong);
        numCarteCourt = (EditText) findViewById(R.id.numCarteCourt);
        typeCarte1 = (EditText) findViewById(R.id.typeCarte1);
        atqa1 = (EditText) findViewById(R.id.atqa1);
        sak1 = (EditText) findViewById(R.id.sak1);


        BtnOpenNFC = (Button) findViewById(R.id.BtnOpenNFC);

        build = new AlertDialog.Builder(this);
        progressDialog = new ProgressDialog(EnregCarte.this);

        //numCarte.setText("00112233445566778899AABBCCDDEEFF");

        /*BtnSaveCarte.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(idCarte.length() > 0 && numCarteLong.length() > 0 && numCarteCourt.length() > 0){
                    Toast.makeText(EnregCarte.this, "Réussie", Toast.LENGTH_SHORT).show();
                }
                else{
                    build.setMessage("Un ou Tous les champs sont vides");
                    build.setCancelable(true);
                    AlertDialog dialog = build.create();
                    dialog.show();
                }
            }
        });*/


        BtnOpenNFC.setOnClickListener(new View.OnClickListener() {
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


               /* try {
                    nfc.open();
                    readThread = new ReadThread();
                    readThread.start();
                    m1CardAuthenticate();
                    nfc.close();
                } catch (TelpoException e) {
                    e.printStackTrace();
                }*/

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

                            idCarte.setText(StringUtil.toHexString(uid));
                            typeCarte1.setText( "A " + type);
                            atqa1.setText(StringUtil.toHexString(atqa));
                            sak1.setText(StringUtil.toHexString(sak));
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
            //writeBlockData();
            readBlockData();

            //OwriteValueData();
            readValueData();
        } else {
            Toast.makeText(this, getString(R.string.operation_fail), Toast.LENGTH_SHORT).show();
            Log.d(TAG, "m1CardAuthenticate fail!");
        }
    }


    public void writeBlockData() {
        byte[] blockData = null;
        String blockStr;
        Boolean status = true;

        Log.d(TAG, "writeBlockData...");
        blockStr = numCarteLong.getText().toString();
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


    public void readBlockData() {
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
        } else {
            numCarteLong.setText(StringUtil.toHexString(data));
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


    public void readValueData() {
        byte[] data = null;
        try {
            data = nfc.m1_read_value(blockNum_2);
        } catch (TelpoException e) {
            e.printStackTrace();
        }

        if (null == data) {
            Log.e(TAG, "readValueBtn fail!");
            Toast.makeText(this, getString(R.string.operation_fail), Toast.LENGTH_SHORT).show();
        } else {
            numCarteCourt.setText(StringUtil.toHexString(data));
        }
    }

    public void writeValueData() {
        byte[] valueData = null;
        String valueStr;
        boolean status = true;

        Log.d(TAG, "writeValueBtn...");
        valueStr = numCarteCourt.getText().toString();
        valueData = toByteArray(valueStr);

        try {
            nfc.m1_write_value(blockNum_2, valueData, valueData.length);
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



}
