package de.joesch_it.interactivestory.model;


/**
 *  Choice properties. Gives information about existing
 *  Choice objects.
 */
public class Choice {

    private int mTextId;
    private int mNextPage;

    public Choice(int textId, int nextPage) {
        mTextId = textId;
        mNextPage = nextPage;
    }

    public int getTextId() {
        return mTextId;
    }

    public int getNextPage() {
        return mNextPage;
    }
}
