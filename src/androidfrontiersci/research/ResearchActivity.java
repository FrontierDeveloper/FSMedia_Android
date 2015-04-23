package androidfrontiersci.research;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActionBar;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.text.method.ScrollingMovementMethod;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;

import androidfrontiersci.ImageProcessor;
import androidfrontiersci.JsonParser;
import androidfrontiersci.listviews.CustomListViewAdapter;
import androidfrontiersci.listviews.RowItem;
import androidfrontiersci.MainActivity;
import androidfrontiersci.maps.MapsActivity;
import androidfrontiersci.textviews.CustomTextView;
import androidfrontiersci.videos.VideosListActivity;
import pl.polidea.view.ZoomView;

import com.example.androidfrontiersci.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/*
    This is ResearchActivity, the activity started when the Research section is selected from the
    main menu.
    Its layout file is activity_research.xml.
    Its menu file is research.xml.
    This class contains two fragments, ResearchNavigationDrawerFragment and CategoryContent. The
    first is a navigation drawer that contains a ListView of all of the research categories. The
    second is a simple page displaying the content of each of the research categories shown on the
    navigation drawer.
*/
public class ResearchActivity extends Activity implements
		ResearchNavigationDrawerFragment.NavigationDrawerCallbacks {

/*
    This public variable is accessed from restoreActionBar() and the CategoryContent class within
    this file. It is also accessed from VideosListActivity.java when this section is linked to the
    Videos section.
*/
    public static String mTitle;

    // The class' private variable, the fragment managing the behaviors, interactions and
    // presentation of the navigation drawer.
	private ResearchNavigationDrawerFragment mNavigationDrawerFragment;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_research);

		MainActivity.fromResearch = true; // Set the flag looked from when VideosListActivity is
                                          // started.
		mNavigationDrawerFragment = (ResearchNavigationDrawerFragment) getFragmentManager()
				.findFragmentById(R.id.navigation_drawer);
		mTitle = (String) getTitle();
		// Set up the drawer.
		DrawerLayout mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		mNavigationDrawerFragment.setUp(R.id.navigation_drawer,
				mDrawerLayout);

        if (MainActivity.fromVideosOrMaps) {
            mDrawerLayout.closeDrawer(Gravity.LEFT);
        } else {
            mDrawerLayout.openDrawer(Gravity.LEFT); // Open the navigation drawer first on opening
                                                    // this section.
        }

	}

    // When an item on the navigation drawer is selected, a new instance of the CategoryContent
    // fragment is created with the data of the selected category.
	@Override
	public void onNavigationDrawerItemSelected(int position) {
		// update the main content by replacing fragments
		FragmentManager fragmentManager = getFragmentManager();
		fragmentManager.beginTransaction().replace(R.id.container, CategoryContent.newInstance(
                position + 1)).commit();
	}

/*
    Action bar functions:
*/
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		if (!mNavigationDrawerFragment.isDrawerOpen()) {
			restoreActionBar();
			return true;
		}
		return super.onCreateOptionsMenu(menu);
	}

/*
    Helper functions:
*/
    // restoreActionBar
    // This function is called from onCreateOptionsMenu(). It restores the action bar with the new
    // category title every time the CategoryContent fragment is in the foreground.
    public void restoreActionBar() {
        ActionBar actionBar = getActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(mTitle);
    }
    // onSectionAttached
    // This function is called from onAttach() of the CategoryContent fragment. It sets the mTitle
    // variable to the section selected.
    public void onSectionAttached(int number) {
        mTitle = JsonParser.displayable_categories.get(number-1);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(event.getAction() == KeyEvent.ACTION_DOWN){
            if (keyCode == KeyEvent.KEYCODE_BACK) {
                finish();
                MainActivity.fromResearch = false; // Set the fromResearch flag to false when
                                                   // leaving, when no longer from Research section.
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

/*
    This is the CategoryContent fragment.
    Its layout file is fragment_research.xml.
    It contains the image, post content and links to videos and maps for each of the research
    categories.
*/
	public static class CategoryContent extends Fragment {

		private static final String ARG_SECTION_NUMBER = "section_number";

        // newInstance
		// This function returns a new instance of this fragment for the given section number.
		public static CategoryContent newInstance(int sectionNum) {
			CategoryContent fragment = new CategoryContent();
			Bundle args = new Bundle();
			args.putInt(ARG_SECTION_NUMBER, sectionNum);
			fragment.setArguments(args);
			return fragment;
		}

		public CategoryContent() {}

        // This private class is simply a holder for the elements page.
		private class ViewHolder {
			ImageView project_image = new ImageView(getActivity());
			TextView post_content = new CustomTextView(getActivity());
			ListView videos_link = new ListView(getActivity());
			ListView maps_link = new ListView(getActivity());
		}

		@SuppressLint("InflateParams") @Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_research, null);
	        ViewHolder holder = new ViewHolder();
	        holder.project_image = (ImageView) rootView.findViewById(R.id.project_image);
	        holder.post_content = (TextView) rootView.findViewById(R.id.post_content);
	        holder.videos_link = (ListView) rootView.findViewById(R.id.videos_link);
	        holder.maps_link = (ListView) rootView.findViewById(R.id.maps_link);
	        rootView.setTag(holder);
	        
	        holder.project_image.setImageDrawable(ImageProcessor.project_images.get(mTitle));

            String current_research_category = MainActivity.getResearchCategory(mTitle);

            holder.post_content.setText((String) ((Map<String, Object>) JsonParser.ProjectData
                    .get(mTitle)).get("project_description"));
	        	
	        holder.post_content.setMovementMethod(new ScrollingMovementMethod());
	        makeMyScrollSmart(holder.post_content);
	        
	        // To avoid redundancy and to make the creation of the links much easier, two separate
	        // ListViews are created, using the same format as seen in the main menu and the
	        // navigation drawers. This removes the need of a LinearLayout that would require the
	        // piece by piece recreation of the format previously achieved. Two ListViews are
	        // necessary because if both items were placed in one ListView the view would be
	        // required to be scrollable, meaning only one item would display at a time. The end
	        // result of this implementation of two ListViews is the appearance of one ListView that
	        // displays both items at the same time.
	        // Create link to videos
	        List<RowItem> videosLinkContainer = new ArrayList<RowItem>();
	        RowItem videos_link = new RowItem(R.drawable.video_icon, getString(
                    R.string.title_activity_videos));
	        videosLinkContainer.add(videos_link);
	        CustomListViewAdapter videosLinkAdapter = new CustomListViewAdapter(getActivity(),
                    R.layout.list_item, videosLinkContainer);
	        holder.videos_link.setAdapter(videosLinkAdapter);
	        holder.videos_link.setOnItemClickListener(new AdapterView.OnItemClickListener() {
	        	@Override
	            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
	                linkToVideos();
	            }
	        });
	        
	        // Create link to maps
	        List<RowItem> mapsLinkContainer = new ArrayList<RowItem>();
	        RowItem maps_link = new RowItem(R.drawable.map_icon, getString(
                    R.string.title_activity_maps));
	        mapsLinkContainer.add(maps_link);
	        CustomListViewAdapter mapsLinkAdapter = new CustomListViewAdapter(getActivity(),
                    R.layout.list_item, mapsLinkContainer);
	        holder.maps_link.setAdapter(mapsLinkAdapter);
	        holder.maps_link.setOnItemClickListener(new AdapterView.OnItemClickListener() {
	        	@Override
	            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
	                linkToMaps();
	            }
	        });

            // If there are no videos that correspond to the research category,
            // the corresponding link is not shown.
            if (((Map<String, Map<String, Object>>) JsonParser.ProjectData.get(mTitle)).get(
                    "videos").isEmpty()) {
                holder.videos_link.setVisibility(ListView.INVISIBLE);
            }

            ZoomView zoomView = new ZoomView(getActivity());
            zoomView.addView(rootView);

            return zoomView; // Return zoom-able view with rootView in it.
		}

    /*
        Helper functions:
    */
        // linkToVideos
        // This function is called when the videos link is selected. It simply starts the
        // VideosListActivity, the opening activity of the Videos section.
		private void linkToVideos() {
		    Intent intent;
        	intent = new Intent(getActivity(), VideosListActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); // Go to old instance of class if there
                                                             // is one. This disallows endless loops
                                                             // of new activities.
        	startActivity(intent);
		}
		// linkToMaps
        // This function is called when the maps link is selected. It simply starts the
        // MapsActivity.
		private void linkToMaps() {
		    Intent intent;
        	intent = new Intent(getActivity(), MapsActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); // Go to old instance of class if there
                                                             // is one. This disallows endless loops
                                                             // of new activities.
        	startActivity(intent);
		}
		// makeMyScrollSmart
		// This function is called from onCreateView(). It allows the existence of a scrolling view
		// inside of a ScrollView. When the child view is touched, the parent ScrollView is
		// disabled.
		@SuppressLint("ClickableViewAccessibility") private void makeMyScrollSmart(View view) {
		    view.setOnTouchListener(new View.OnTouchListener() {
		        @Override
		        public boolean onTouch(View __v, MotionEvent __event) {
		            if (__event.getAction() == MotionEvent.ACTION_DOWN) {
		                // Disallow the touch request for parent scroll on touch of child view.
		                requestDisallowParentInterceptTouchEvent(__v, true);
		            } else if (__event.getAction() == MotionEvent.ACTION_UP || __event.getAction()
                            == MotionEvent.ACTION_CANCEL) {
		                // Re-allow parent events on removed touch of child view.
		                requestDisallowParentInterceptTouchEvent(__v, false);
		            }
		            return false;
		        }
		    });
		}
        // requestDisallowParentInterceptTouchEvent
        // This function is called from makeMyScrollSmart().
		private void requestDisallowParentInterceptTouchEvent(View __v,
                                                              Boolean __disallowIntercept) {
		    while (__v.getParent() != null && __v.getParent() instanceof View) {
		        if (__v.getParent() instanceof ScrollView) {
		            __v.getParent().requestDisallowInterceptTouchEvent(__disallowIntercept);
		        }
		        __v = (View) __v.getParent();
		    }
		}

		@Override
		public void onAttach(Activity activity) {
			super.onAttach(activity);
			((ResearchActivity) activity).onSectionAttached(getArguments()
					.getInt(ARG_SECTION_NUMBER));
		}
	}
}
