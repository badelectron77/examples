package de.joesch_it.unittestsmockito;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity implements MainActivityView {

    LinearLayout linearLayout;
    EditText editText;
    TextView textView;
    Spinner colorSpinner;
    Button launchActivityButton;
    MainActivityPresenter mPresenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mPresenter = new MainActivityPresenter(this);

        // Initialize Views
        textView = (TextView) findViewById(R.id.textView);
        editText = (EditText) findViewById(R.id.editText);
        launchActivityButton = (Button) findViewById(R.id.launchActivityButton);
        linearLayout = (LinearLayout) findViewById(R.id.linearLayout);
        colorSpinner = (Spinner) findViewById(R.id.colorSpinner);

        // Setup Spinner
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.colors_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        colorSpinner.setAdapter(adapter);

        editText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView tv, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    String text = tv.getText().toString();
                    mPresenter.editTextUpdated(text);
                }
                return false;
            }
        });

        colorSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int index, long id) {
                mPresenter.colorSelected(index);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        launchActivityButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPresenter.launchOtherActivityButtonClicked(OtherActivity.class);
            }
        });
    }

    @Override
    public void changeTextViewText(String text) {
        textView.setText(text);
    }

    @Override
    public void changeBackgroundColor(int color) {
        linearLayout.setBackgroundColor(color);
    }

    @Override
    public void launchOtherActivity(Class activity) {
        Intent intent = new Intent(MainActivity.this, activity);
        startActivity(intent);
    }
}
