package androidfrontiersci.about;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Bundle;
import android.text.Layout;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.LeadingMarginSpan;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
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
    This is AboutActivity, a class that displays the content of about_page_info defined in
    JsonParser.java in a ScrollView.
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
        for (final Map<String, String> snippet : ((List<Map<String, String>>) JsonParser
                .about_page_info.get("snippets"))) {
            View snippet_view = ((LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE))
                    .inflate(R.layout.about_page_snippet, null);
            final ImageView imageView = (ImageView) snippet_view.findViewById(R.id.snippet_image);
            final TextView textView = (TextView) snippet_view.findViewById(R.id.snippet_text);
            imageView.setBackground(ImageProcessor.about_snippet_images.get(idx));
            // This creates text wrapping.
            makeSpan(imageView, textView, snippet);
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
        for (final Map<String, String> person : ((List<Map<String, String>>) JsonParser
                .about_page_info.get("people"))) {
            View person_view = ((LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE))
                    .inflate(R.layout.about_page_person, null);
            ImageView imageView = (ImageView) person_view.findViewById(R.id.person_image);
            TextView caption = (TextView) person_view.findViewById(R.id.person_caption);
            final TextView textView = (TextView) person_view.findViewById(R.id.person_text);
            imageView.setBackground(ImageProcessor.about_people_images.get(idx));
            caption.setText(person.get("caption"));
            final LinearLayout linearLayout = (LinearLayout) person_view.findViewById(
                    R.id.person_image_caption);
            // This creates text wrapping.
            makeSpan(linearLayout, textView, person);
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
                            intent.putExtra(Intent.EXTRA_EMAIL, new String[]{getString(R.string.
                                    dev_email)});
                            intent.putExtra(Intent.EXTRA_SUBJECT, email_tag + subject_text);

                            try { // If there is an email client on the device...
                                // the client is opened to allow the user to start the message.
                                startActivity(Intent.createChooser(intent, "Send mail..."));
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

/*
    Helper function:
*/
    void makeSpan(final View image_v, final TextView text_v, final Map<String, String> block) {
        final ViewTreeObserver vto = image_v.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                image_v.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                int finalHeight = image_v.getMeasuredHeight();
                int finalWidth = image_v.getMeasuredWidth();
                String text = block.get("text");

                int allTextStart = 0;
                int allTextEnd = text.length() - 1;

                // Calculate the number of lines to indent.
                int lines;
                Rect bounds = new Rect();
                text_v.getPaint().getTextBounds(text.substring(0,10), 0, 1, bounds);
                float fontSpacing = text_v.getPaint().getFontSpacing();
                lines = (int) (finalHeight/fontSpacing);

                // Build the layout with LeadingMarginSpan2.
                MyLeadingMarginSpan2 span = new MyLeadingMarginSpan2(lines, finalWidth +10 );
                SpannableString SS = new SpannableString(text);
                SS.setSpan(span, allTextStart, allTextEnd,
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                text_v.setText(SS);
            }
        });
    }
}

class MyLeadingMarginSpan2 implements LeadingMarginSpan.LeadingMarginSpan2 {
    private int margin;
    private int lines;

    MyLeadingMarginSpan2(int lines, int margin) {
        this.margin = margin;
        this.lines = lines;
    }

    // Returns the value to which indentation must be added.
    @Override
    public int getLeadingMargin(boolean first) {
        if (first) {
            return margin;
        }
        else {
            return 0;
        }
    }

    @Override
    public void drawLeadingMargin(Canvas c, Paint p, int x, int dir, int top, int baseline, int
            bottom, CharSequence text, int start, int end, boolean first, Layout layout) {}

    @Override
    public int getLeadingMarginLineCount() {
        return lines;
    }
}

