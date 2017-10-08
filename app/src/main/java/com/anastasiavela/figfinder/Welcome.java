package com.anastasiavela.figfinder;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class Welcome extends Activity{

    public Button buttonStart;
    public Button buttonYelp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.welcome);
        buttonFind();
    }

    public void buttonFind() {
        buttonStart = (Button) findViewById(R.id.buttonFind);
        buttonStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent map = new Intent(Welcome.this, MapsActivity.class);
                startActivity(map);
            }
        });
        buttonYelp = (Button) findViewById(R.id.yelp);
        buttonYelp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent map = new Intent(Welcome.this, YelpSearchActivity.class);
                startActivity(map);
            }
        });
    }
}