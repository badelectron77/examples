package de.joesch_it.counter;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button myButton = (Button) findViewById(R.id.button);
        final TextView textView = (TextView) findViewById(R.id.textView);

        myButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String s = textView.getText().toString();
                int i = Integer.parseInt(s);
                String newText = String.valueOf(i + 1);
                textView.setText(newText);

                /**
                 * Extrem kurze Alternative:
                 * Oben
                 *
                 *      int ct = 0;
                 *
                 * dann in onClick():
                 *
                 *      ct++;
                 *      textView.setText(ct + "");
                 */
            }
        });
    }
}
