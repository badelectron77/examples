package de.joesch_it.interactivestory.ui;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Stack;

import de.joesch_it.interactivestory.R;
import de.joesch_it.interactivestory.model.Page;
import de.joesch_it.interactivestory.model.Story;

public class StoryActivity extends AppCompatActivity {

    //public static final String TAG = StoryActivity.class.getSimpleName();

    private String         mName;
    private Story          mStory;
    private ImageView      mStoryImageView;
    private TextView       mStoryTextView;
    private Button         mChoice1Button;
    private Button         mChoice2Button;
    private Stack<Integer> mPageStack       = new Stack<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_story);

        mStoryImageView = (ImageView) findViewById(R.id.storyImageView);
        mStoryTextView  = (TextView)  findViewById(R.id.storyTextView);
        mChoice1Button  = (Button)    findViewById(R.id.choice1Button);
        mChoice2Button  = (Button)    findViewById(R.id.choice2Button);

        Intent intent = getIntent();
        mName = intent.getStringExtra(getString(R.string.key_name));
        if (mName == null || mName.isEmpty()) {
            mName = "Friend";
        }

        mStory = new Story();
        loadPage(0);
    }

    /**
     * Loads Page data into activity views.
     *
     * @param pageNumber
     */
    private void loadPage(int pageNumber) {

        // for navigation
        mPageStack.push(pageNumber);

        // for retrieving page data infos
        final Page page = mStory.getPage(pageNumber);

        // Image
        Drawable image = ContextCompat.getDrawable(this, page.getImageId());
        mStoryImageView.setImageDrawable(image);

        // Text
        String pageText = getString(page.getTextId());
        // Add mName if placeholder included. Won't add if not.
        pageText = String.format(pageText, mName);
        mStoryTextView.setText(pageText);

        // Buttons
        if (page.isFinalPage()) {
            mChoice1Button.setVisibility(View.INVISIBLE);
            mChoice2Button.setText(R.string.play_again_button_text);
            mChoice2Button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    loadPage(0);
                }
            });
        }
        else {
            loadButtons(page);
        }

    }

    // external part of loadPage() for loading the buttons
    private void loadButtons(final Page page) {

        mChoice1Button.setVisibility(View.VISIBLE);
        mChoice1Button.setText(page.getChoice1().getTextId());
        mChoice1Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int nextPage = page.getChoice1().getNextPage();
                loadPage(nextPage);
            }
        });

        mChoice2Button.setVisibility(View.VISIBLE);
        mChoice2Button.setText(page.getChoice2().getTextId());
        mChoice2Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int nextPage = page.getChoice2().getNextPage();
                loadPage(nextPage);
            }
        });
    }

    /**
     * For back navigation.
     */
    @Override
    public void onBackPressed() {
        mPageStack.pop();
        if (mPageStack.isEmpty()) {
            super.onBackPressed();
        }
        else {
            loadPage(mPageStack.pop());
        }
    }
}
