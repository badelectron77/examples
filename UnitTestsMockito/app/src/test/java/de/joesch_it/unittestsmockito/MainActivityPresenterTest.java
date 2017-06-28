package de.joesch_it.unittestsmockito;

import android.graphics.Color;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;


@RunWith(MockitoJUnitRunner.class)
public class MainActivityPresenterTest {

    private MainActivityPresenter mPresenter;

    @Mock
    private MainActivityView mView;

    @Before
    public void setUp() throws Exception {
        mPresenter = new MainActivityPresenter(mView);
    }

    @Test
    public void editTextUpdated() throws Exception {
        // Arrange
        String givenString = "test123";

        // Act
        mPresenter.editTextUpdated(givenString);

        // Assert
        Mockito.verify(mView).changeTextViewText(givenString);
    }

    @Test
    public void colorSelected() throws Exception {
        // Arrange
        int index = 2;
        int givenColor = Color.GREEN;

        // Act
        mPresenter.colorSelected(index);

        // Assert
        Mockito.verify(mView).changeBackgroundColor(givenColor);
    }

    @Test
    public void launchOtherActivityButtonClicked() throws Exception {
        // Arrange
        Class clazz = OtherActivity.class;

        // Act
        mPresenter.launchOtherActivityButtonClicked(clazz);

        // Assert
        Mockito.verify(mView).launchOtherActivity(clazz);
    }

}