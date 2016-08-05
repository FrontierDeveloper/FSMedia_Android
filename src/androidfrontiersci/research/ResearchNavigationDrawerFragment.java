// ResearchNavigationDrawerFragment.java

package androidfrontiersci.research;

import android.app.Activity;
import android.app.ActionBar;
import android.app.Fragment;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;

import androidfrontiersci.ImageProcessor;
import androidfrontiersci.JsonParser;
import androidfrontiersci.MainActivity;
import androidfrontiersci.listviews.CustomListViewAdapter;
import androidfrontiersci.listviews.RowItem;
import androidfrontiersci.Download.Downloader;

import frontsci.android.R;

import java.util.ArrayList;
import java.util.List;

/*
    This is the ResearchNavigationDrawerFragment fragment, used to display the research categories
    in the Research section.
    Its layout file is fragment_navigation_drawer.xml. It contains a ListView with the research
    categories and appears as an overlay off the left side of the screen. While the drawer is open,
    the rest of the screen will display the current selected category content but will be shadowed.
    Selecting an item on the drawer will take you that categories content. Selecting the shadowed
    region, selecting the left side of the action bar and swiping the navigation drawer from right
    to left will all close the drawer and un-shadow the current category. To reopen the drawer,
    simply press the left side of the action bar or swipe finger from off the left side onto the
    screen.
*/
public class ResearchNavigationDrawerFragment extends Fragment {

    // The class' private variables
	// Remember the position of the selected item.
	private static final String STATE_SELECTED_POSITION = "selected_navigation_drawer_position";
	// Per the design guidelines, you should show the drawer on launch until the user manually
	// expands it. This shared preference tracks this.
	private static final String PREF_USER_LEARNED_DRAWER = "navigation_drawer_learned";
    // A pointer to the current callbacks instance (the ResearchActivity).
	private NavigationDrawerCallbacks mCallbacks;
    // Helper component that ties the action bar to the navigation drawer.
	private ActionBarDrawerToggle mDrawerToggle;
	private DrawerLayout mDrawerLayout;
	private ListView mDrawerListView;
	private View mFragmentContainerView;
	private int mCurrentSelectedPosition = 0;
	private boolean mFromSavedInstanceState;
	private boolean mUserLearnedDrawer;

	public ResearchNavigationDrawerFragment() {}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Read in the flag indicating whether or not the user has demonstrated
		// awareness of the drawer. See PREF_USER_LEARNED_DRAWER for details.
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
		mUserLearnedDrawer = sp.getBoolean(PREF_USER_LEARNED_DRAWER, false);

		if (savedInstanceState != null) {
			mCurrentSelectedPosition = savedInstanceState
					.getInt(STATE_SELECTED_POSITION);
			mFromSavedInstanceState = true;
		}

        if (MainActivity.fromVideosOrMaps) {
			Log.d("fromVideosOrMaps","SHould run");
			mCurrentSelectedPosition = MainActivity.index;
			// needs fixed to index
		}

		// Select either the default item (0) or the last selected item.
		selectItem(mCurrentSelectedPosition);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		// Indicate that this fragment would like to influence the set of
		// actions in the action bar.
		setHasOptionsMenu(true);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
		mDrawerListView = (ListView) inflater.inflate(R.layout
                        .fragment_navigation_drawer, container, false);
		mDrawerListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
		    @Override
			public void onItemClick(AdapterView<?> parent, View view,
				int position, long id) {
				selectItem(position);
			}
        	    });

        BitmapDrawable verticalRepeatBg = (BitmapDrawable) getResources().getDrawable(
                R.drawable.navigation_bg);
        verticalRepeatBg.setTileModeXY(Shader.TileMode.MIRROR, Shader.TileMode.REPEAT);
        mDrawerListView.setBackground(verticalRepeatBg);
        
        List<RowItem> rowItems;

		rowItems = new ArrayList<RowItem>();

		for (int i = 0; i < Downloader.RPMap.size(); i++) {
			String title = Downloader.RPMap.get(i).title;
			ImageView imageView = new ImageView(getActivity());
			imageView.setImageBitmap(Downloader.RPMap.get(i).image);
			RowItem item = new RowItem(imageView.getDrawable(), Downloader.RPMap.get(i).title);
			rowItems.add(item);
		}

		CustomListViewAdapter adapter = new CustomListViewAdapter(getActivity(),
				R.layout.list_item, rowItems);
		
		mDrawerListView.setAdapter(adapter);
		mDrawerListView.setItemChecked(mCurrentSelectedPosition, true);
		return mDrawerListView;
	}

	public boolean isDrawerOpen() {
		return mDrawerLayout != null
				&& mDrawerLayout.isDrawerOpen(mFragmentContainerView);
	}

	// Users of this fragment must call this method to set up the navigation drawer interactions.
	public void setUp(int fragmentId, DrawerLayout drawerLayout) {
		mFragmentContainerView = getActivity().findViewById(fragmentId);
		mDrawerLayout = drawerLayout;
		// Set a custom shadow that overlays the main content when the drawer opens.
		mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);

		ActionBar actionBar = getActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.setHomeButtonEnabled(true);

		// ActionBarDrawerToggle ties together the the proper interactions between the navigation
		// drawer and the action bar app icon.
		mDrawerToggle = new ActionBarDrawerToggle(getActivity(), mDrawerLayout,
                R.drawable.ic_drawer, R.string.navigation_drawer_open,
                R.string.navigation_drawer_close) {
			@Override
			public void onDrawerClosed(View drawerView) {
				super.onDrawerClosed(drawerView);
				if (!isAdded()) {
					return;
				}
				getActivity().invalidateOptionsMenu(); // Calls onPrepareOptionsMenu()
			}

			@Override
			public void onDrawerOpened(View drawerView) {
				super.onDrawerOpened(drawerView);
				if (!isAdded()) {
					return;
				}
				if (!mUserLearnedDrawer) {
					// The user manually opened the drawer; store this flag to prevent auto-showing
					// the navigation drawer automatically in the future.
					mUserLearnedDrawer = true;
					SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(
                            getActivity());
					sp.edit().putBoolean(PREF_USER_LEARNED_DRAWER, true).apply();
				}
				getActivity().invalidateOptionsMenu(); // Calls onPrepareOptionsMenu()
			}
		};
		// If the user hasn't 'learned' about the drawer, open it to introduce them to the drawer,
		// per the navigation drawer design guidelines.
		if (!mUserLearnedDrawer && !mFromSavedInstanceState) {
			mDrawerLayout.openDrawer(mFragmentContainerView);
		}
		// Defer code dependent on restoration of previous instance state.
		mDrawerLayout.post(new Runnable() {
			@Override
			public void run() {
				mDrawerToggle.syncState();
			}
		});
		mDrawerLayout.setDrawerListener(mDrawerToggle);
	}

	private void selectItem(int position) {
		mCurrentSelectedPosition = position;
		if (mDrawerListView != null) {
			mDrawerListView.setItemChecked(position, true);
		}
		if (mDrawerLayout != null) {
			mDrawerLayout.closeDrawer(mFragmentContainerView);
		}
		if (mCallbacks != null) {
			mCallbacks.onNavigationDrawerItemSelected(position);
		}
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		try {
			mCallbacks = (NavigationDrawerCallbacks) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(
					"Activity must implement NavigationDrawerCallbacks.");
		}
	}

	@Override
	public void onDetach() {
		super.onDetach();
		mCallbacks = null;
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putInt(STATE_SELECTED_POSITION, mCurrentSelectedPosition);
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		// Forward the new configuration the drawer toggle component.
		mDrawerToggle.onConfigurationChanged(newConfig);
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		// If the drawer is open, show the global app actions in the action bar. See
		// showGlobalContextActionBar, which controls the top-left area of the action bar.
		if (mDrawerLayout != null && isDrawerOpen()) {
			showGlobalContextActionBar();
		}
		super.onCreateOptionsMenu(menu, inflater);
	}

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Pass the event to ActionBarDrawerToggle. If it returns true, then it has handled the app
        // icon touch event.
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        // Handle your other action bar items...
        return super.onOptionsItemSelected(item);
    }

	// Per the navigation drawer design guidelines, updates the action bar to show the app section
	// 'context', rather than just what's in the current screen.
	private void showGlobalContextActionBar() {
		ActionBar actionBar = getActionBar();
		actionBar.setDisplayShowTitleEnabled(true);
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
		actionBar.setTitle(R.string.title_activity_research);
		actionBar.setIcon(R.drawable.research_icon);
	}

	private ActionBar getActionBar() {
		return getActivity().getActionBar();
	}

	// Callbacks interface that all activities using this fragment must implement.
	public static interface NavigationDrawerCallbacks {
		void onNavigationDrawerItemSelected(int position); // Called when an item in the navigation
		                                                   // drawer is selected.
	}
}
