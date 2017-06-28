package de.joesch_it.interactivestory.model;

import de.joesch_it.interactivestory.R;

/**
 * Data class holding all data for displaying a Page.
 * Creates all seven Page objects and fills them with data.
 * Also creates all Choice (button text and target) objects
 * and fills them with data. Returns NO information about this data!
 */
public class Story {

    private Page[] mPages;

    public Story() {

        mPages = new Page[7];

        mPages[0] = new Page(R.drawable.page0,
                R.string.page0,
                new Choice(R.string.page0_choice1, 1),
                new Choice(R.string.page0_choice2, 2));

        mPages[1] = new Page(R.drawable.page1,
                R.string.page1,
                new Choice(R.string.page1_choice1, 3),
                new Choice(R.string.page1_choice2, 4));

        mPages[2] = new Page(R.drawable.page2,
                R.string.page2,
                new Choice(R.string.page2_choice1, 4),
                new Choice(R.string.page2_choice2, 6));

        mPages[3] = new Page(R.drawable.page3,
                R.string.page3,
                new Choice(R.string.page3_choice1, 4),
                new Choice(R.string.page3_choice2, 5));

        mPages[4] = new Page(R.drawable.page4,
                R.string.page4,
                new Choice(R.string.page4_choice1, 5),
                new Choice(R.string.page4_choice2, 6));

        mPages[5] = new Page(R.drawable.page5, R.string.page5);

        mPages[6] = new Page(R.drawable.page6, R.string.page6);

    }

    public Page getPage(int pageNumber) {
        if (pageNumber >= mPages.length) {
            pageNumber = 0;
        }
        return mPages[pageNumber];
    }
}
