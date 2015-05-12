package androidfrontiersci.askascientist;

import frontsci.android.R;
import frontsci.android.R.id;
import com.google.android.youtube.player.YouTubeStandalonePlayer;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.content.DialogInterface;
import android.widget.Toast;

import androidfrontiersci.ImageProcessor;
import androidfrontiersci.JsonParser;
import androidfrontiersci.videos.DeveloperKey;
import androidfrontiersci.videos.VideoActivity;

public class AskAScientistActivity extends Activity {

    public static String your_name;
    public static String question;
    public static String subject;
//    public static String all_fields;
    public static String front_sci = "[frontiersci] ";

    public static String scientist_name;
    public static String scientist_bio;
    public static String scientist_video_url;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_ask_a_scientist);
		if (savedInstanceState == null) {
			getFragmentManager().beginTransaction()
					.add(R.id.container, new PlaceholderFragment()).commit();
		}

    }

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.ask_a_scientist, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.


        int id = item.getItemId();
        switch (id){
            case R.id.email_send:
                EditText name_et = (EditText) findViewById(R.id.your_name);
                EditText subject_et = (EditText) findViewById(R.id.the_subject);
                EditText question_et = (EditText) findViewById(R.id.your_question);

                your_name = name_et.getText().toString();
                subject = subject_et.getText().toString();
                question = question_et.getText().toString();

//                all_fields = your_name + subject + question;
                if (your_name.equals("") || subject.equals("") || question.equals("")) {
                    // TODO Change this dialog to custom
                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
                    alertDialogBuilder.setTitle("Error");
                    alertDialogBuilder
                            .setMessage("Fill out all of your information!")
                            .setCancelable(false)
                            .setNegativeButton("Ok", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    // if this button is clicked, just close
                                    // the dialog box and do nothing
                                    dialog.cancel();
                                }
                            });

                    // create alert dialog
                    AlertDialog alertDialog = alertDialogBuilder.create();

                    // show it
                    alertDialog.show();
                    return true;
                } else {
                    Intent i = new Intent(Intent.ACTION_SEND);
                    i.setType("message/rfc822");
                    i.putExtra(Intent.EXTRA_EMAIL, new String[]{getString(R.string.ask_email)});
                    i.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.ask_email_tag) + subject);
                    i.putExtra(Intent.EXTRA_TEXT   , question);
                    try {
                        startActivity(Intent.createChooser(i, "Send mail..."));
                    } catch (android.content.ActivityNotFoundException ex) {
                        Toast.makeText(this, "There are no email clients installed.", Toast.LENGTH_SHORT).show();
                    }
                    return true;
                }
        }
		return super.onOptionsItemSelected(item);
	}

	/**
	 * A placeholder fragment containing a simple view.
	 */
	public static class PlaceholderFragment extends Fragment {

		public PlaceholderFragment() {
		}

        private class ViewHolder {
            ImageView scientist_image = new ImageView(getActivity());
            TextView scientist_name_TV = new TextView(getActivity());
            TextView scientist_bio_TV = new TextView(getActivity());
            Button scientist_video_button = new Button(getActivity());
        }

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_ask_a_scientist, container, false);
            ViewHolder holder = new ViewHolder();

            holder.scientist_image = (ImageView) rootView.findViewById(id.scientist_image);

            holder.scientist_image.setImageDrawable(ImageProcessor.ask_a_scientist_photo);

            scientist_name = JsonParser.ask_a_scientist_info.get("name");
            scientist_bio = JsonParser.ask_a_scientist_info.get("bio");
            scientist_video_url = JsonParser.ask_a_scientist_info.get("video");

            Log.e("TAG", "name: "+scientist_name);
            Log.e("TAG", "bio: "+scientist_bio);
            Log.e("TAG","video: "+scientist_video_url);

            holder.scientist_name_TV = (TextView) rootView.findViewById(id.scientist_name);
            holder.scientist_name_TV.setText(scientist_name);
            holder.scientist_bio_TV = (TextView) rootView.findViewById(id.scientist_bio);
            holder.scientist_bio_TV.setText(scientist_bio);
            holder.scientist_video_button = (Button) rootView.findViewById(id.video_bio);


            holder.scientist_video_button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String video_id = VideoActivity.getYouTubeVideoId(scientist_video_url);
                    Intent intent = YouTubeStandalonePlayer.createVideoIntent(getActivity(),
                            DeveloperKey.DEVELOPER_KEY, video_id);
                    startActivity(intent);
                }
            });

            holder.scientist_image.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String video_id = VideoActivity.getYouTubeVideoId(scientist_video_url);
                    Intent intent = YouTubeStandalonePlayer.createVideoIntent(getActivity(),
                            DeveloperKey.DEVELOPER_KEY, video_id);
                    startActivity(intent);
                }
            });
			return rootView;
		}

	}
}