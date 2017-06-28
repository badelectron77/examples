package de.joesch_it.myfirstapp;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

public class DisplayMessageActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_message);

        // Get the Intent that started this activity and extract the string
        Intent intent = getIntent();
        String message = intent.getStringExtra(MainActivity.KEY_INTENT_MESSAGE);
        String email = intent.getStringExtra(MainActivity.KEY_INTENT_EMAIL);

        // Capture the layout's TextView and set the string as its text
        TextView textView = (TextView) findViewById(R.id.textView);
        TextView textViewEmail = (TextView) findViewById(R.id.textViewEmail);
        textView.setText(message);
        textViewEmail.setText(email);

    }
}
