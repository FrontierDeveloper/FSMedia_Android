package androidfrontiersci.about;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import frontsci.android.R;

import java.util.List;
import java.util.Map;

import androidfrontiersci.ImageProcessor;
import androidfrontiersci.JsonParser;

/*
    This is AboutActivity, a very simple class that displays the about_page_opening string defined in
    XmlParser.java in a ScrollView.
    Its layout file is activity_about.xml.
    Its menu file is about.xml.
    The "Contact Developers" option of the action bar opens an email client to start the message
    sending process.
*/
public class AboutActivity extends Activity {

    String email_tag = "[frontierscidevelopers] ";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_about);

        LinearLayout filler = (LinearLayout) findViewById(R.id.filler);

        // Add first the opening text.
        View top = ((LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(
                R.layout.about_page_opening, null, false);
        TextView opening = (TextView) top.findViewById(R.id.opening);
        opening.setText((String) JsonParser.about_page_info.get("opening"));
        filler.addView(top);

        // Add all three snippets, two with the image on the left and one with the image on the
        // right.
        int idx = 0;
        for (Map<String, String> snippet : ((List<Map<String, String>>) JsonParser.about_page_info
                .get("snippets"))) {
            View snippet_view;
            if ((idx % 2) == 0) {
                snippet_view = ((LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE))
                        .inflate(R.layout.about_page_snippet_left, null);
            } else {
                snippet_view = ((LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE))
                        .inflate(R.layout.about_page_snippet_right, null);
            }
            ImageView imageView = (ImageView) snippet_view.findViewById(R.id.snippet_image);
            TextView textView = (TextView) snippet_view.findViewById(R.id.snippet_text);
            imageView.setBackground(ImageProcessor.about_snippet_images.get(idx));
            textView.setText(snippet.get("text"));
            filler.addView(snippet_view);
            idx++;
        }

        // Add the goal and the people introduction.
        View middle_view = ((LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE))
                .inflate(R.layout.about_page_middle, null, false);
        TextView middle = (TextView) middle_view.findViewById(R.id.middle);
        middle.setText(JsonParser.about_page_info.get("goal") + "\n\n" + JsonParser.about_page_info
                .get("people_intro"));
        filler.addView(middle_view);

        // Add in all the people.
        idx = 0;
        for (Map<String, String> person : ((List<Map<String, String>>) JsonParser.about_page_info
                .get("people"))) {
            View person_view = ((LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE))
                    .inflate(R.layout.about_page_person, null);
            ImageView imageView = (ImageView) person_view.findViewById(R.id.person_image);
            TextView caption = (TextView) person_view.findViewById(R.id.person_caption);
            TextView textView = (TextView) person_view.findViewById(R.id.person_text);
            imageView.setBackground(ImageProcessor.about_people_images.get(idx));
            caption.setText(person.get("caption"));
            textView.setText(person.get("text"));
            filler.addView(person_view);
            idx++;
        }

	}

/*
    Action bar functions:
*/
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.about, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here.
        int id = item.getItemId();
        if (id == R.id.contact_developers) {

            final EditText subject = new EditText(this);
            subject.setHint(getString(R.string.subject_hint));

            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
            alertDialogBuilder.setTitle("Please enter your message subject.");
            alertDialogBuilder
                    .setCancelable(false)
                    .setView(subject)
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            //the user cancelled
                        }
                    })
                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            // if this button is clicked, just close
                            // the dialog box and do nothing
                            dialog.cancel();

                            String subject_text = subject.getText().toString();

                            Intent intent = new Intent(Intent.ACTION_SEND);
                            intent.setType("message/rfc822");
                            intent.putExtra(Intent.EXTRA_EMAIL, new String[]{getString(R.string.dev_email)});
                            intent.putExtra(Intent.EXTRA_SUBJECT, email_tag + subject_text);

                            try { // If there is an email client on the device...
                                startActivity(Intent.createChooser(intent, "Send mail...")); // the client is opened
                                // to allow the user to
                                // start the message.
                            } catch (android.content.ActivityNotFoundException ex) {
//                                Toast.makeText(this, "There are no email clients installed.",
//                                        Toast.LENGTH_SHORT).show();
                            }
                        }
                    });

            // create alert dialog
            AlertDialog alertDialog = alertDialogBuilder.create();
            // show it
            alertDialog.show();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }



}
