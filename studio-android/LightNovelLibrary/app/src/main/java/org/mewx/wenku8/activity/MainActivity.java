package org.mewx.wenku8.activity;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.nostra13.universalimageloader.cache.disc.impl.UnlimitedDiscCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.quinny898.library.persistentsearch.SearchBox;
import com.quinny898.library.persistentsearch.SearchResult;
import com.readystatesoftware.systembartint.SystemBarTintManager;
import com.umeng.analytics.MobclickAgent;

import org.mewx.wenku8.MyApp;
import org.mewx.wenku8.R;
import org.mewx.wenku8.fragment.NavigationDrawerFragment;
import org.mewx.wenku8.global.GlobalConfig;
import org.mewx.wenku8.util.LightCache;

import java.io.File;
import java.util.List;


public class MainActivity extends ActionBarActivity {
    // This is for fragment switch
    public enum FRAGMENT_LIST {
        RKLIST, LATEST, FAV, CONFIG
    }

    private FRAGMENT_LIST status = FRAGMENT_LIST.LATEST;

    public FRAGMENT_LIST getCurrentFragment() {
        return status;
    }

    public void setCurrentFragment(FRAGMENT_LIST f) {
        status = f;
        return;
    }

    private Toolbar mToolbar;
    private NavigationDrawerFragment mNavigationDrawerFragment;
    private SearchBox searchbox;
    private String titleSaved;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_main); // have 3 styles

        // create save folder
        LightCache.saveFile(GlobalConfig.getFirstStoragePath() + "imgs",
                ".nomedia", "".getBytes(), false);
        GlobalConfig.setFirstStoragePathStatus(LightCache.testFileExist(GlobalConfig.getFirstStoragePath()
                + "imgs" + File.separator + ".nomedia")); // set status
        LightCache.saveFile(GlobalConfig.getSecondStoragePath() + "imgs",
                ".nomedia", "".getBytes(), false);

        // UIL setting
        UnlimitedDiscCache localUnlimitedDiscCache = new UnlimitedDiscCache(
                new File(GlobalConfig.getFirstStoragePath() + "cache"),
                new File(getCacheDir() + File.separator + "imgs"));
        DisplayImageOptions localDisplayImageOptions = new DisplayImageOptions.Builder()
                .resetViewBeforeLoading(true).cacheOnDisk(true)
                .cacheInMemory(true).bitmapConfig(Bitmap.Config.RGB_565)
                .resetViewBeforeLoading(true)
                .displayer(new FadeInBitmapDisplayer(250)).build();
        ImageLoaderConfiguration localImageLoaderConfiguration = new ImageLoaderConfiguration.Builder(
                this).diskCache(localUnlimitedDiscCache)
                .defaultDisplayImageOptions(localDisplayImageOptions).build();
        ImageLoader.getInstance().init(localImageLoaderConfiguration);

        // global settings
        GlobalConfig.initVolleyNetwork();

        // UMeng settings
        MobclickAgent.updateOnlineConfig( this );


        // set Toolbar
        mToolbar = (Toolbar) findViewById(R.id.toolbar_actionbar);
        setSupportActionBar(mToolbar);
        // getSupportActionBar().setDisplayShowHomeEnabled(true);

        // set Tool button
        mNavigationDrawerFragment = (NavigationDrawerFragment) getFragmentManager().findFragmentById(R.id.fragment_drawer);
        mNavigationDrawerFragment.setup(R.id.fragment_drawer, (DrawerLayout) findViewById(R.id.drawer), mToolbar);

        // find search box
        searchbox = (SearchBox) findViewById(R.id.searchbox);
        searchbox.enableVoiceRecognition(this);
        searchbox.setLogoText(getResources().getString(R.string.action_search));
        searchbox.setMaxLength(10);
        mToolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            	@Override
            	public boolean onMenuItemClick(MenuItem item) {
                    Toast.makeText(MyApp.getContext(),"called button",Toast.LENGTH_SHORT).show();
                    if(item.getItemId() == R.id.action_search) {
                        ((View)findViewById(R.id.searchbox_bg)).setVisibility(View.VISIBLE);
                        searchbox.setSearchStatus(false);
                        openSearch();
                    }
            		return true;
            	}
            });


        // change status bar color, and this require SDK16
        if (Build.VERSION.SDK_INT >= 16 ) { //&& Build.VERSION.SDK_INT <= 21) {
            // Android API 22 has more effects on status bar, so ignore

            // create our manager instance after the content view is set
            SystemBarTintManager tintManager = new SystemBarTintManager(this);
            // enable all tint
            tintManager.setStatusBarTintEnabled(true);
            tintManager.setNavigationBarTintEnabled(true);
            tintManager.setTintAlpha(0.15f);
            // set all color
            tintManager.setTintColor(getResources().getColor(android.R.color.black));
        }
    }

    // search box action
    public void openSearch() {
        // save title
        titleSaved = mToolbar.getTitle().toString();

        mToolbar.setTitle("Searching...");
        searchbox.revealFromMenuItem(R.id.action_search, this);

        // get search history
        List<String> bookshelf = GlobalConfig.getSearchHistory();
        for(String i : bookshelf) {
            SearchResult option = new SearchResult(
                    i.charAt(0)=='1' || i.charAt(0)=='2' ? i.substring(1,i.length()) : i,
                    getResources().getDrawable(R.drawable.ic_launcher));
            searchbox.addSearchable(option);
        }

        // set listener
        searchbox.setSearchListener(new SearchBox.SearchListener() {
            @Override
            public void onSearchOpened() {
                // Use this to tint the screen
                Toast.makeText(MyApp.getContext(),"search open",Toast.LENGTH_SHORT).show();

            }

            @Override
            public void onSearchClosed() {
                // Use this to un-tint the screen
                closeSearch();
            }

            @Override
            public void onSearchTermChanged() {
                // React to the search term changing
                // Called after it has updated results
            }

            @Override
            public void onSearch(String searchTerm) {
                Toast.makeText(MyApp.getContext(), searchTerm + " Searched",
                        Toast.LENGTH_LONG).show();
                mToolbar.setTitle(searchTerm);

            }

            @Override
            public void onSearchCleared() {

            }

        });

    }

    private void closeSearch() {
        mToolbar.setTitle(titleSaved);

        searchbox.hideCircularly(this);
        searchbox.setSearchStatus(false);
        ((View) findViewById(R.id.searchbox_bg)).setVisibility(View.GONE);

        InputMethodManager inputManager = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);
        inputManager.hideSoftInputFromWindow(MainActivity.this.getCurrentFocus().getWindowToken(), 0);

        return;

    }

//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        if (requestCode == 1234 && resultCode == RESULT_OK) {
//            ArrayList<String> matches = data
//                    .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
//            searchbox.populateEditText(matches);
//        }
//        super.onActivityResult(requestCode, resultCode, data);
//    }





    /**
     * Hard menu button works like the soft menu button.
     * And this will control all the menu appearance,
     * I can handle the button list by edit this function.
     *
     * @param menu The options menu in which you place your items, but I ignore this.
     * @return True if shown successfully, False if failed.
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // to the open status //
        if(mNavigationDrawerFragment.isDrawerOpen()) {
            // close search box and IME
            if (searchbox != null)
                closeSearch();
        }

        // to the close status //
        // only when the navigation draw closed, I draw the menu bar.
        // the menu items will be drawn automatically
        if(!mNavigationDrawerFragment.isDrawerOpen()) {


            // change title of toolbar
            switch(status){

                case LATEST:
                    if(getSupportActionBar()!=null)
                        getSupportActionBar().setTitle(getResources().getString(R.string.main_menu_latest));
                    getMenuInflater().inflate(R.menu.menu_latest, menu);

                    // Associate searchable configuration with the SearchView
//                    SearchManager searchManager =
//                            (SearchManager) getSystemService(Context.SEARCH_SERVICE);
//                    SearchView searchView =
//                            (SearchView) menu.findItem(R.id.action_search).getActionView();
//                    searchView.setSearchableInfo(
//                            searchManager.getSearchableInfo(getComponentName()));

                    break;

                case RKLIST:
                    if(getSupportActionBar()!=null)
                        getSupportActionBar().setTitle(getResources().getString(R.string.main_menu_rklist));

                    break;

                case FAV:
                    if(getSupportActionBar()!=null)
                        getSupportActionBar().setTitle(getResources().getString(R.string.main_menu_fav));

                    break;

                case CONFIG:
                    if(getSupportActionBar()!=null)
                        getSupportActionBar().setTitle(getResources().getString(R.string.main_menu_config));

                    break;
            }
        }
        else {
            if(getSupportActionBar()!=null)
                getSupportActionBar().setTitle(getResources().getString(R.string.app_name));
        }

        return true;
    }

    /**
     * This function will be called by NavigationDrawerFragment,
     * once called, change fragment.
     *
     * @param targetFragment target fragment.
     */
    public void changeFragment(Fragment targetFragment) {
        // temporarily set elevation to remove rank list toolbar shadow
        if(status == FRAGMENT_LIST.RKLIST)
            getSupportActionBar().setElevation(0);
        else
            getSupportActionBar().setElevation(getResources().getDimension(R.dimen.toolbar_elevation));

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, targetFragment, "fragment")
                .setTransitionStyle(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                .commit();

    }

    @Override
    protected void onPause() {
        super.onPause();
        MobclickAgent.onPause(this);

        return;
    }

    @Override
    protected void onResume() {
        super.onResume();
        MobclickAgent.onResume(this);

        return;
    }

    @Override
    public void onBackPressed() {
        if (mNavigationDrawerFragment.isDrawerOpen())
            mNavigationDrawerFragment.closeDrawer();
        else if(searchbox.getSearchStatus())
            closeSearch();
        else
            super.onBackPressed();
    }
}
