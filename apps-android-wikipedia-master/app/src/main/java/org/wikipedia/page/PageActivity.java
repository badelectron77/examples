package org.wikipedia.page;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.SearchManager;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;
import android.support.design.widget.BottomSheetDialog;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.Toolbar;
import android.text.format.DateUtils;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import net.hockeyapp.android.metrics.MetricsManager;

import org.apache.commons.lang3.StringUtils;
import org.wikipedia.Constants;
import org.wikipedia.R;
import org.wikipedia.WikipediaApp;
import org.wikipedia.activity.ThemedActionBarActivity;
import org.wikipedia.analytics.IntentFunnel;
import org.wikipedia.analytics.LinkPreviewFunnel;
import org.wikipedia.dataclient.WikiSite;
import org.wikipedia.descriptions.DescriptionEditRevertHelpView;
import org.wikipedia.events.ChangeTextSizeEvent;
import org.wikipedia.gallery.GalleryActivity;
import org.wikipedia.history.HistoryEntry;
import org.wikipedia.language.LangLinksActivity;
import org.wikipedia.page.linkpreview.LinkPreviewDialog;
import org.wikipedia.page.snippet.CompatActionMode;
import org.wikipedia.page.tabs.TabsProvider;
import org.wikipedia.page.tabs.TabsProvider.TabPosition;
import org.wikipedia.readinglist.AddToReadingListDialog;
import org.wikipedia.search.SearchFragment;
import org.wikipedia.search.SearchInvokeSource;
import org.wikipedia.settings.SettingsActivity;
import org.wikipedia.staticdata.MainPageNameData;
import org.wikipedia.theme.ThemeChooserDialog;
import org.wikipedia.tooltip.ToolTipUtil;
import org.wikipedia.useroption.sync.UserOptionContentResolver;
import org.wikipedia.util.ClipboardUtil;
import org.wikipedia.util.DeviceUtil;
import org.wikipedia.util.FeedbackUtil;
import org.wikipedia.util.ShareUtil;
import org.wikipedia.util.log.L;
import org.wikipedia.widgets.WidgetProviderFeaturedPage;
import org.wikipedia.wiktionary.WiktionaryDialog;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

import static org.wikipedia.util.DeviceUtil.isBackKeyUp;
import static org.wikipedia.util.UriUtil.visitInExternalBrowser;

public class PageActivity extends ThemedActionBarActivity implements PageFragment.Callback,
        LinkPreviewDialog.Callback, SearchFragment.Callback, WiktionaryDialog.Callback {

    public static final String ACTION_PAGE_FOR_TITLE = "org.wikipedia.page_for_title";
    public static final String ACTION_SHOW_TAB_LIST = "org.wikipedia.show_tab_list";
    public static final String ACTION_RESUME_READING = "org.wikipedia.resume_reading";
    public static final String EXTRA_PAGETITLE = "org.wikipedia.pagetitle";
    public static final String EXTRA_HISTORYENTRY  = "org.wikipedia.history.historyentry";

    private static final String LANGUAGE_CODE_BUNDLE_KEY = "language";

    @BindView(R.id.tabs_container) View tabsContainerView;
    @BindView(R.id.page_progress_bar) ProgressBar progressBar;
    @BindView(R.id.page_toolbar_container) View toolbarContainerView;
    @BindView(R.id.page_toolbar) Toolbar toolbar;
    @Nullable private Unbinder unbinder;

    private PageFragment pageFragment;

    private WikipediaApp app;
    @Nullable private Bus bus;
    private EventBusMethods busMethods;
    private CompatActionMode currentActionMode;

    private PageToolbarHideHandler toolbarHideHandler;

    private ExclusiveBottomSheetPresenter bottomSheetPresenter = new ExclusiveBottomSheetPresenter();
    @Nullable private PageLoadCallbacks pageLoadCallbacks;

    private DialogInterface.OnDismissListener listDialogDismissListener = new DialogInterface.OnDismissListener() {
        @Override
        public void onDismiss(DialogInterface dialogInterface) {
            pageFragment.updateBookmark();
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        app = (WikipediaApp) getApplicationContext();
        MetricsManager.register(app, app);
        app.checkCrashes(this);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS,
                    WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }

        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

        try {
            setContentView(R.layout.activity_page);
        } catch (Exception e) {
            if (e.getMessage().contains("WebView")) {
                // If the system failed to inflate our activity because of the WebView (which could
                // be one of several types of exceptions), it likely means that the system WebView
                // is in the process of being updated. In this case, show the user a message and
                // bail immediately.
                Toast.makeText(app, R.string.error_webview_updating, Toast.LENGTH_LONG).show();
                finish();
                return;
            }
            throw e;
        }

        unbinder = ButterKnife.bind(this);

        busMethods = new EventBusMethods();
        registerBus();

        updateProgressBar(false, true, 0);

        pageFragment = (PageFragment) getSupportFragmentManager().findFragmentById(R.id.page_fragment);

        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        toolbarHideHandler = new PageToolbarHideHandler(toolbarContainerView);

        boolean languageChanged = false;
        if (savedInstanceState != null) {
            if (savedInstanceState.getBoolean("isSearching")) {
                openSearchFragment(SearchInvokeSource.TOOLBAR, null);
            }
            String language = savedInstanceState.getString(LANGUAGE_CODE_BUNDLE_KEY);
            languageChanged = !app.getAppOrSystemLanguageCode().equals(language);

            // Note: when system language is enabled, and the system language is changed outside of
            // the app, MRU languages are not updated. There's no harm in doing that here but since
            // the user didin't choose that language in app, it may be unexpected.
        }

        if (languageChanged) {
            app.resetWikiSite();
            loadMainPageInForegroundTab();
        }

        if (savedInstanceState == null) {
            // if there's no savedInstanceState, and we're not coming back from a Theme change,
            // then we must have been launched with an Intent, so... handle it!
            handleIntent(getIntent());
        }

        UserOptionContentResolver.requestManualSync();
    }

    private void finishActionMode() {
        currentActionMode.finish();
    }

    private void nullifyActionMode() {
        currentActionMode = null;
    }

    public void hideSoftKeyboard() {
        DeviceUtil.hideSoftKeyboard(this);
    }

    // Note: this method is invoked even when in CAB mode.
    @Override
    public boolean dispatchKeyEvent(@NonNull KeyEvent event) {
        return isBackKeyUp(event) && ToolTipUtil.dismissToolTip(this)
                || super.dispatchKeyEvent(event);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                if (shouldRecreateMainActivity()) {
                    startActivity(getSupportParentActivityIntent()
                            .putExtra(Constants.INTENT_RETURN_TO_MAIN, true));
                }
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onSearchRequested() {
        showToolbar();
        openSearchFragment(SearchInvokeSource.TOOLBAR, null);
        return true;
    }

    public void showToolbar() {
        // TODO: make toolbar visible, via CoordinatorLayout
    }

    /** @return True if the contextual action bar is open. */
    public boolean isCabOpen() {
        return currentActionMode != null;
    }

    @NonNull
    public static Intent newIntent(@NonNull Context context) {
        return new Intent(ACTION_RESUME_READING).setClass(context, PageActivity.class);
    }

    @NonNull
    public static Intent newIntent(@NonNull Context context, @NonNull String title) {
        PageTitle pageTitle = new PageTitle(title, WikipediaApp.getInstance().getWikiSite());
        return newIntent(context, new HistoryEntry(pageTitle, HistoryEntry.SOURCE_INTERNAL_LINK), pageTitle);
    }

    @NonNull
    public static Intent newIntent(@NonNull Context context,
                                   @NonNull HistoryEntry entry,
                                   @NonNull PageTitle title) {
        return new Intent(ACTION_PAGE_FOR_TITLE)
                .setClass(context, PageActivity.class)
                .putExtra(EXTRA_HISTORYENTRY, entry)
                .putExtra(EXTRA_PAGETITLE, title);
    }

    @NonNull
    public static Intent newIntentForTabList(@NonNull Context context) {
        return new Intent(ACTION_SHOW_TAB_LIST).setClass(context, PageActivity.class);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handleIntent(intent);
    }

    private void handleIntent(@NonNull Intent intent) {
        if (Intent.ACTION_VIEW.equals(intent.getAction()) && intent.getData() != null) {
            WikiSite wiki = new WikiSite(intent.getData().getAuthority());
            PageTitle title = wiki.titleForUri(intent.getData());
            HistoryEntry historyEntry = new HistoryEntry(title, HistoryEntry.SOURCE_EXTERNAL_LINK);
            loadPageInForegroundTab(title, historyEntry);
        } else if (ACTION_PAGE_FOR_TITLE.equals(intent.getAction())) {
            PageTitle title = intent.getParcelableExtra(EXTRA_PAGETITLE);
            HistoryEntry historyEntry = intent.getParcelableExtra(EXTRA_HISTORYENTRY);
            loadPageInForegroundTab(title, historyEntry);
            if (intent.hasExtra(Constants.INTENT_EXTRA_REVERT_QNUMBER)) {
                showDescriptionEditRevertDialog(intent.getStringExtra(Constants.INTENT_EXTRA_REVERT_QNUMBER));
            }
        } else if (ACTION_SHOW_TAB_LIST.equals(intent.getAction())
                || ACTION_RESUME_READING.equals(intent.getAction())) {
            // do nothing, since this will be handled indirectly by PageFragment.
        } else if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            PageTitle title = new PageTitle(query, app.getWikiSite());
            HistoryEntry historyEntry = new HistoryEntry(title, HistoryEntry.SOURCE_SEARCH);
            loadPageInForegroundTab(title, historyEntry);
        } else if (intent.hasExtra(Constants.INTENT_FEATURED_ARTICLE_FROM_WIDGET)) {
            new IntentFunnel(app).logFeaturedArticleWidgetTap();
            loadMainPageInForegroundTab();
        } else {
            loadMainPageInCurrentTab();
        }
    }

    /**
     * Update the state of the main progress bar that is shown inside the ActionBar of the activity.
     * @param visible Whether the progress bar is visible.
     * @param indeterminate Whether the progress bar is indeterminate.
     * @param value Value of the progress bar (may be between 0 and 10000). Ignored if the
     *              progress bar is indeterminate.
     */
    public void updateProgressBar(boolean visible, boolean indeterminate, int value) {
        progressBar.setIndeterminate(indeterminate);
        if (!indeterminate) {
            progressBar.setProgress(value);
        }
        progressBar.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    /**
     * Returns whether we're currently in a "searching" state (i.e. the search fragment is shown).
     * @return True if currently searching, false otherwise.
     */
    public boolean isSearching() {
        SearchFragment searchFragment = searchFragment();
        return searchFragment != null && searchFragment.isSearchActive();
    }

    /**
     * Load a new page, and put it on top of the backstack.
     * @param title Title of the page to load.
     * @param entry HistoryEntry associated with this page.
     */
    public void loadPage(@NonNull PageTitle title, @NonNull HistoryEntry entry) {
        loadPage(title, entry, TabPosition.CURRENT_TAB);
    }

    /**
     * Load a new page, and put it on top of the backstack, optionally allowing state loss of the
     * fragment manager. Useful for when this function is called from an AsyncTask result.
     * @param title Title of the page to load.
     * @param entry HistoryEntry associated with this page.
     * @param position Whether to open this page in the current tab, a new background tab, or new
     *                 foreground tab.
     */
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    public void loadPage(@NonNull final PageTitle title,
                         @NonNull final HistoryEntry entry,
                         @NonNull final TabPosition position) {
        if (isDestroyed()) {
            return;
        }

        if (entry.getSource() != HistoryEntry.SOURCE_INTERNAL_LINK || !app.isLinkPreviewEnabled()) {
            new LinkPreviewFunnel(app, entry.getSource()).logNavigate();
        }

        app.putCrashReportProperty("api", title.getWikiSite().authority());
        app.putCrashReportProperty("title", title.toString());

        if (title.isSpecial()) {
            visitInExternalBrowser(this, Uri.parse(title.getMobileUri()));
            return;
        }

        tabsContainerView.post(new Runnable() {
            @Override
            public void run() {
                if (!pageFragment.isAdded()) {
                    return;
                }
                // Close the link preview, if one is open.
                hideLinkPreview();

                pageFragment.closeFindInPage();
                if (position == TabPosition.CURRENT_TAB) {
                    pageFragment.loadPage(title, entry, true);
                } else if (position == TabPosition.NEW_TAB_BACKGROUND) {
                    pageFragment.openInNewBackgroundTabFromMenu(title, entry);
                } else {
                    pageFragment.openInNewForegroundTabFromMenu(title, entry);
                }
                app.getSessionFunnel().pageViewed(entry);
            }
        });
    }

    public void loadPageInForegroundTab(@NonNull PageTitle title, @NonNull HistoryEntry entry) {
        loadPage(title, entry, TabPosition.NEW_TAB_FOREGROUND);
    }

    public void loadMainPageInForegroundTab() {
        loadMainPage(TabPosition.NEW_TAB_FOREGROUND);
    }

    private void loadMainPageInCurrentTab() {
        loadMainPage(TabPosition.CURRENT_TAB);
    }

    /**
     * Go directly to the Main Page of the current Wiki, optionally allowing state loss of the
     * fragment manager. Useful for when this function is called from an AsyncTask result.
     */
    public void loadMainPage(TabPosition position) {
        PageTitle title = new PageTitle(MainPageNameData.valueFor(app.getAppOrSystemLanguageCode()), app.getWikiSite());
        HistoryEntry historyEntry = new HistoryEntry(title, HistoryEntry.SOURCE_MAIN_PAGE);
        loadPage(title, historyEntry, position);
    }

    public void showLinkPreview(@NonNull PageTitle title, int entrySource) {
        showLinkPreview(title, entrySource, null);
    }

    public void showLinkPreview(@NonNull PageTitle title, int entrySource, @Nullable Location location) {
        bottomSheetPresenter.show(getSupportFragmentManager(),
                LinkPreviewDialog.newInstance(title, entrySource, location));
    }

    private void hideLinkPreview() {
        bottomSheetPresenter.dismiss(getSupportFragmentManager());
    }

    public void showAddToListDialog(@NonNull PageTitle title, @NonNull AddToReadingListDialog.InvokeSource source) {
        bottomSheetPresenter.showAddToListDialog(getSupportFragmentManager(), title, source, listDialogDismissListener);
    }

    // Note: back button first handled in {@link #onOptionsItemSelected()};
    @Override
    public void onBackPressed() {
        if (ToolTipUtil.dismissToolTip(this)) {
            return;
        }
        if (isCabOpen()) {
            finishActionMode();
            return;
        }

        SearchFragment searchFragment = searchFragment();
        if (searchFragment != null && searchFragment.onBackPressed()) {
            if (searchFragment.isLaunchedFromIntent()) {
                finish();
            }
            return;
        }
        getSupportActionBar().setTitle("");
        app.getSessionFunnel().backPressed();
        if (pageFragment.onBackPressed()) {
            return;
        }
        finish();
    }

    @Override
    public void onPageShowBottomSheet(@NonNull BottomSheetDialog dialog) {
        bottomSheetPresenter.show(getSupportFragmentManager(), dialog);
    }

    @Override
    public void onPageShowBottomSheet(@NonNull BottomSheetDialogFragment dialog) {
        bottomSheetPresenter.show(getSupportFragmentManager(), dialog);
    }

    @Override
    public void onPageDismissBottomSheet() {
        bottomSheetPresenter.dismiss(getSupportFragmentManager());
    }

    @Nullable
    @Override
    public PageToolbarHideHandler onPageGetToolbarHideHandler() {
        return toolbarHideHandler;
    }

    @Override
    public void onPageLoadPage(@NonNull PageTitle title, @NonNull HistoryEntry entry) {
        loadPage(title, entry);
    }

    @Override
    public void onPageShowLinkPreview(@NonNull PageTitle title, int source) {
        showLinkPreview(title, source);
    }

    @Override
    public void onPageLoadMainPageInForegroundTab() {
        loadMainPageInForegroundTab();
    }

    @Override
    public void onPageUpdateProgressBar(boolean visible, boolean indeterminate, int value) {
        updateProgressBar(visible, indeterminate, value);
    }

    @Override
    public boolean onPageIsSearching() {
        return isSearching();
    }

    @Nullable
    @Override
    public Fragment onPageGetTopFragment() {
        return pageFragment;
    }

    @Override
    public void onPageShowThemeChooser() {
        bottomSheetPresenter.show(getSupportFragmentManager(), new ThemeChooserDialog());
    }

    @Nullable
    @Override
    public ActionMode onPageStartSupportActionMode(@NonNull ActionMode.Callback callback) {
        return startSupportActionMode(callback);
    }

    @Override
    public void onPageShowToolbar() {
        showToolbar();
    }

    @Override
    public void onPageHideSoftKeyboard() {
        hideSoftKeyboard();
    }

    @Nullable
    @Override
    public PageLoadCallbacks onPageGetPageLoadCallbacks() {
        return pageLoadCallbacks;
    }

    @Override
    public void onPageLoadPage(@NonNull PageTitle title, @NonNull HistoryEntry entry,
                               @NonNull TabPosition tabPosition) {
        loadPage(title, entry, tabPosition);
    }

    @Override
    public void onPageAddToReadingList(@NonNull PageTitle title,
                                @NonNull AddToReadingListDialog.InvokeSource source) {
        showAddToListDialog(title, source);
    }

    @Override
    public void onPageRemoveFromReadingLists(@NonNull PageTitle title) {
        if (!pageFragment.isAdded()) {
            return;
        }
        FeedbackUtil.showMessage(getActivity(),
                getString(R.string.reading_list_item_deleted, title.getDisplayText()));
        pageFragment.updateBookmark();
    }

    @Nullable
    @Override
    public View onPageGetContentView() {
        return pageFragment.getView();
    }

    @Nullable
    @Override
    public View onPageGetTabsContainerView() {
        return tabsContainerView;
    }

    @Override
    public void onPagePopFragment() {
        finish();
    }

    @Override
    public void onPageInvalidateOptionsMenu() {
        supportInvalidateOptionsMenu();
    }

    @Override
    public void onPageSearchRequested() {
        openSearchFragment(SearchInvokeSource.TOOLBAR, null);
    }

    @Override
    public void onPageLoadError(@NonNull PageTitle title) {
        getSupportActionBar().setTitle(title.getDisplayText());
    }

    @Override
    public void onPageLoadErrorRetry() {
        getSupportActionBar().setTitle("");
    }

    @Override
    public void onPageLoadErrorBackPressed() {
        finish();
    }

    @Override
    public void onSearchSelectPage(@NonNull HistoryEntry entry, boolean inNewTab) {
        loadPage(entry.getTitle(), entry, inNewTab ? TabsProvider.TabPosition.NEW_TAB_BACKGROUND
                : TabsProvider.TabPosition.CURRENT_TAB);
    }

    @Override
    public void onSearchOpen() {
        toolbarContainerView.setVisibility(View.GONE);
    }

    @Override
    public void onSearchClose(boolean launchedFromIntent) {
        SearchFragment fragment = searchFragment();
        if (fragment != null) {
            closeSearchFragment(fragment);
        }
        toolbarContainerView.setVisibility(View.VISIBLE);
        hideSoftKeyboard();
    }

    @Override
    public void onSearchResultAddToList(@NonNull PageTitle title, @NonNull AddToReadingListDialog.InvokeSource source) {
        showAddToListDialog(title, source);
    }

    @Override
    public void onSearchResultShareLink(@NonNull PageTitle title) {
        ShareUtil.shareText(this, title);
    }

    @Override
    public void onLinkPreviewLoadPage(@NonNull PageTitle title, @NonNull HistoryEntry entry, boolean inNewTab) {
        loadPage(title, entry, inNewTab ? TabPosition.NEW_TAB_BACKGROUND : TabPosition.CURRENT_TAB);
    }

    @Override
    public void onLinkPreviewCopyLink(@NonNull PageTitle title) {
        copyLink(title.getCanonicalUri());
        showCopySuccessMessage();
    }

    @Override
    public void onLinkPreviewAddToList(@NonNull PageTitle title) {
        showAddToListDialog(title, AddToReadingListDialog.InvokeSource.LINK_PREVIEW_MENU);
    }

    @Override
    public void onLinkPreviewShareLink(@NonNull PageTitle title) {
        ShareUtil.shareText(this, title);
    }

    @Override
    public void onSearchResultCopyLink(@NonNull PageTitle title) {
        copyLink(title.getCanonicalUri());
        showCopySuccessMessage();
    }

    @Override
    public void wiktionaryShowDialogForTerm(@NonNull String term) {
        pageFragment.getShareHandler().showWiktionaryDefinition(term);
    }

    @Override
    public boolean shouldLoadFromBackStack() {
        return getIntent() != null
                && (ACTION_SHOW_TAB_LIST.equals(getIntent().getAction())
                || ACTION_RESUME_READING.equals(getIntent().getAction()));
    }

    @Override
    public boolean shouldShowTabList() {
        return getIntent() != null && ACTION_SHOW_TAB_LIST.equals(getIntent().getAction());
    }

    private void copyLink(@NonNull String url) {
        ClipboardUtil.setPlainText(this, null, url);
    }

    private void showCopySuccessMessage() {
        FeedbackUtil.showMessage(this, R.string.address_copied);
    }

    @Nullable
    @Override
    public AppCompatActivity getActivity() {
        return this;
    }

    private boolean shouldRecreateMainActivity() {
        return getIntent().getAction() == null
                || getIntent().getAction().equals(Intent.ACTION_VIEW);
    }

    @Override
    protected void onResume() {
        super.onResume();
        app.resetWikiSite();
        app.getSessionFunnel().touchSession();
    }

    @Override
    public void onPause() {
        if (isCabOpen()) {
            // Explicitly close any current ActionMode (see T147191)
            finishActionMode();
        }
        super.onPause();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        saveState(outState);
    }

    private void saveState(@NonNull Bundle outState) {
        outState.putBoolean("isSearching", isSearching());
        outState.putString(LANGUAGE_CODE_BUNDLE_KEY, app.getAppOrSystemLanguageCode());
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, final Intent data) {
        if (settingsActivityRequested(requestCode)) {
            handleSettingsActivityResult(resultCode);
        } else if (newArticleLanguageSelected(requestCode, resultCode) || galleryPageSelected(requestCode, resultCode)) {
            handleLangLinkOrPageResult(data);
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void handleLangLinkOrPageResult(final Intent data) {
        tabsContainerView.post(new Runnable() {
            @Override
            public void run() {
                handleIntent(data);
            }
        });
    }

    @Override
    protected void onStop() {
        app.getSessionFunnel().persistSession();
        super.onStop();
    }

    @Override
    public void onDestroy() {
        if (unbinder != null) {
            unbinder.unbind();
        }
        unregisterBus();
        super.onDestroy();
    }

    /**
     * ActionMode that is invoked when the user long-presses inside the WebView.
     * @param mode ActionMode under which this context is starting.
     */
    @Override
    public void onSupportActionModeStarted(@NonNull ActionMode mode) {
        if (!isCabOpen()) {
            conditionallyInjectCustomCabMenu(mode);
        }
        super.onSupportActionModeStarted(mode);
    }

    @Override
    public void onSupportActionModeFinished(@NonNull ActionMode mode) {
        super.onSupportActionModeFinished(mode);
        nullifyActionMode();
    }

    @Override
    public void onActionModeStarted(android.view.ActionMode mode) {
        if (!isCabOpen()) {
            conditionallyInjectCustomCabMenu(mode);
        }
        super.onActionModeStarted(mode);
    }

    @Override
    public void onActionModeFinished(android.view.ActionMode mode) {
        super.onActionModeFinished(mode);
        nullifyActionMode();
    }

    private <T> void conditionallyInjectCustomCabMenu(T mode) {
        currentActionMode = new CompatActionMode(mode);
        if (currentActionMode.shouldInjectCustomMenu()) {
            currentActionMode.injectCustomMenu(pageFragment);
        }
    }

    private void registerBus() {
        bus = app.getBus();
        bus.register(busMethods);
        L.d("Registered bus.");
    }

    private void unregisterBus() {
        if (bus != null) {
            bus.unregister(busMethods);
        }
        bus = null;
        L.d("Unregistered bus.");
    }

    private void handleSettingsActivityResult(int resultCode) {
        if (languageChanged(resultCode)) {
            loadNewLanguageMainPage();
        }
    }

    private boolean settingsActivityRequested(int requestCode) {
        return requestCode == SettingsActivity.ACTIVITY_REQUEST_SHOW_SETTINGS;
    }

    private boolean newArticleLanguageSelected(int requestCode, int resultCode) {
        return requestCode == Constants.ACTIVITY_REQUEST_LANGLINKS && resultCode == LangLinksActivity.ACTIVITY_RESULT_LANGLINK_SELECT;
    }

    private boolean galleryPageSelected(int requestCode, int resultCode) {
        return requestCode == Constants.ACTIVITY_REQUEST_GALLERY && resultCode == GalleryActivity.ACTIVITY_RESULT_PAGE_SELECTED;
    }

    private boolean languageChanged(int resultCode) {
        return resultCode == SettingsActivity.ACTIVITY_RESULT_LANGUAGE_CHANGED;
    }

    /**
     * Reload the main page in the new language, after delaying for one second in order to:
     * (1) Make sure that onStart in MainActivity gets called, thus registering the activity for the bus.
     * (2) Ensure a smooth transition, which is very jarring without a delay.
     */
    private void loadNewLanguageMainPage() {
        Handler uiThread = new Handler(Looper.getMainLooper());
        uiThread.postDelayed(new Runnable() {
            @Override
            public void run() {
                loadMainPageInForegroundTab();
                updateFeaturedPageWidget();
            }
        }, DateUtils.SECOND_IN_MILLIS);
    }

    /**
     * Update any instances of our Featured Page widget, since it will change with the currently selected language.
     */
    private void updateFeaturedPageWidget() {
        Intent widgetIntent = new Intent(this, WidgetProviderFeaturedPage.class);
        widgetIntent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        int[] ids = AppWidgetManager.getInstance(getApplication()).getAppWidgetIds(
                new ComponentName(this, WidgetProviderFeaturedPage.class));
        widgetIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids);
        sendBroadcast(widgetIntent);
    }

    private void showDescriptionEditRevertDialog(@NonNull String qNumber) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.notification_reverted_title)
                .setView(new DescriptionEditRevertHelpView(this, qNumber))
                .setPositiveButton(android.R.string.ok, null)
                .create()
                .show();
    }

    @VisibleForTesting
    public void setPageLoadCallbacks(@Nullable PageLoadCallbacks pageLoadCallbacks) {
        this.pageLoadCallbacks = pageLoadCallbacks;
    }

    @SuppressLint("CommitTransaction")
    private void openSearchFragment(@NonNull SearchInvokeSource source, @Nullable String query) {
        Fragment fragment = searchFragment();
        if (fragment == null) {
            fragment = SearchFragment.newInstance(source, StringUtils.trim(query), true);
            getSupportFragmentManager()
                    .beginTransaction()
                    .add(R.id.activity_page_container, fragment)
                    .commitNowAllowingStateLoss();
        }
    }

    @SuppressLint("CommitTransaction")
    private void closeSearchFragment(@NonNull SearchFragment fragment) {
        getSupportFragmentManager().beginTransaction().remove(fragment).commitNowAllowingStateLoss();
    }

    @Nullable private SearchFragment searchFragment() {
        return (SearchFragment) getSupportFragmentManager()
                .findFragmentById(R.id.activity_page_container);
    }

    private class EventBusMethods {
        @Subscribe public void on(ChangeTextSizeEvent event) {
            if (pageFragment != null && pageFragment.getWebView() != null) {
                pageFragment.updateFontSize();
            }
        }
    }
}
