package com.example.cartesmopaye;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    Button write, BtnListCarteEnreg, BtnInfo, BtnEnregCarte, reset;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        BtnInfo = (Button) findViewById(R.id.BtnInfo);
        write = (Button) findViewById(R.id.BtnWrite);
        BtnListCarteEnreg = (Button) findViewById(R.id.BtnListCarteEnreg);
        BtnEnregCarte = (Button) findViewById(R.id.BtnEnregCarte);
        reset = (Button) findViewById(R.id.BtnReset);

        BtnInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), EnregCarte.class);
                startActivity(intent);
            }
        });

        write.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), EcrireCarte.class);
                startActivity(intent);
            }
        });

        BtnListCarteEnreg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), ListCarte.class);
                startActivity(intent);
            }
        });

        BtnEnregCarte.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), SaveBD.class);
                startActivity(intent);
            }
        });

        reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), Reset.class);
                startActivity(intent);
            }
        });
    }
}
