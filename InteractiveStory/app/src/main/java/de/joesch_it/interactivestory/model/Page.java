package de.joesch_it.interactivestory.model;

/**
 *  Page properties. Gives information about built
 *  page objects.
 */
public class Page {

    private int     mImageId;
    private int     mTextId;
    private Choice  mChoice1;
    private Choice  mChoice2;
    private boolean mIsFinalPage = false;

    public Page(int imageId, int textId) {
        mImageId     = imageId;
        mTextId      = textId;
        mIsFinalPage = true;
    }

    public Page(int imageId, int textId, Choice choice1, Choice choice2) {
        mImageId = imageId;
        mTextId  = textId;
        mChoice1 = choice1;
        mChoice2 = choice2;
    }

    public boolean isFinalPage() {
        return mIsFinalPage;
    }

    public int getImageId() {
        return mImageId;
    }

    public int getTextId() {
        return mTextId;
    }

    public Choice getChoice1() {
        return mChoice1;
    }

    public Choice getChoice2() {
        return mChoice2;
    }
}
