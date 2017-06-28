package de.joesch_it.myfirstapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    public SharedPreferences sharedPref;
    public static final String KEY_INTENT_MESSAGE = "de.joesch-it.myfirstapp.USER_MESSAGE";
    public static final String KEY_INTENT_EMAIL = "de.joesch-it.myfirstapp.USER_EMAIL";
    public static final String KEY_PREF_FILE = "de.joesch-it.myfirstapp.PREFERENCE_FILE_KEY";
    public static final String KEY_PREF_USER_MESSAGE = "userMessage";
    public static final String KEY_PREF_USER_EMAIL = "userEmail";
    public String userMessage = String.valueOf(R.string.nothing_written);
    public String userEmail = String.valueOf(R.string.nothing_written);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //Context context = MainActivity.this;
        //Toast.makeText(context, "onCreate() called", Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Context context = MainActivity.this;
        //Toast.makeText(context, "onResume() called", Toast.LENGTH_LONG).show();
        sharedPref = context.getSharedPreferences(KEY_PREF_FILE, Context.MODE_PRIVATE);
        EditText editText = (EditText) findViewById(R.id.editText);
        EditText editTextEmail = (EditText) findViewById(R.id.editTextEmail);

        if (sharedPref.contains(KEY_PREF_USER_MESSAGE)) {
            userMessage = sharedPref.getString(KEY_PREF_USER_MESSAGE,
                    getString(R.string.nothing_written));
            // Put userMessage into input field
            editText.setText(userMessage);
            editText.setSelection(editText.getText().length());
        }

        if (sharedPref.contains(KEY_PREF_USER_EMAIL)) {
            userEmail = sharedPref.getString(KEY_PREF_USER_EMAIL,
                    getString(R.string.nothing_written));
            // Put userMessage into input field
            editTextEmail.setText(userEmail);
            editTextEmail.setSelection(editTextEmail.getText().length());
        }

        editText.requestFocus();

        // "Enter" should send the message
        editText.setOnEditorActionListener((v, actionId, e) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                Button button = (Button) findViewById(R.id.button);
                button.performClick(); // calls "sendMessage"
                return true;
            }
            return false;
        });

        editTextEmail.setOnEditorActionListener((v, actionId, e) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                Button button = (Button) findViewById(R.id.button);
                button.performClick(); // calls "sendMessage"
                return true;
            }
            return false;
        });
    }

    /** Called when the user taps the Send button, View is necessary */
    public void sendMessage(View v) {

        EditText editText = (EditText) findViewById(R.id.editText);
        EditText editTextEmail = (EditText) findViewById(R.id.editTextEmail);

        String message  = editText.getText().toString();
        String email    = editTextEmail.getText().toString();
        SharedPreferences.Editor editor = sharedPref.edit();

        if (message.trim().isEmpty()) {
            message = getString(R.string.nothing_written);
            editor.putString(KEY_PREF_USER_MESSAGE, "");
        } else {
            editor.putString(KEY_PREF_USER_MESSAGE, message);
        }

        if (email.trim().isEmpty()) {
            email = getString(R.string.nothing_written);
            editor.putString(KEY_PREF_USER_EMAIL, "");
        } else {
            editor.putString(KEY_PREF_USER_EMAIL, email);
        }

        editor.apply();

        Intent intent = new Intent(this, DisplayMessageActivity.class);
        intent.putExtra(KEY_INTENT_EMAIL, email);
        intent.putExtra(KEY_INTENT_MESSAGE, message);
        startActivity(intent);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        // Remove KEY_PREF_USER_MESSAGE in SharedPreferences
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.remove(KEY_PREF_USER_MESSAGE);
        editor.remove(KEY_PREF_USER_EMAIL);
        editor.apply();
    }
}
