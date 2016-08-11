package androidfrontiersci.about;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import frontsci.android.R;
// ###########################################################
//    This is AboutActivity, a class that displays the About page in a WebView.
//    Its layout file is activity_about.xml.
//    Its menu file is about.xml.
// ###########################################################
public class AboutActivity extends Activity {
    // ###########################################################
    // The class' private variable. its WebView
    // ###########################################################
    private String link = "http://frontierscientists.com/about/";
    private WebView webView;
    private static AboutProgressDialog progress;
    private String email_tag = "[frontscidevelopers]";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_article);

        progress = new AboutProgressDialog(AboutActivity.this);
        progress.show();

        webView = (WebView) findViewById(R.id.web_view);
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);

        // This instantiates a new WebViewClient, so as not to require the use of a specific client
        // such as Chrome.
        webView.setWebViewClient(new WebViewClient() {
            public void onReceivedError(WebView view, int errorCode, String description, String
                    failingUrl) {
                Toast.makeText(getApplicationContext(), description, Toast.LENGTH_SHORT).show();
            }
            public void onPageFinished(WebView view, String url) {
                progress.dismiss();
            }
        });
        webView.getSettings().setBuiltInZoomControls(true); // Enable zoom.
        webView.loadUrl(link);
    }
    // While in the WebView, the back button serves the WebView, unless pressed when at origin page,
    // then it ends the activity and goes back to the main menu.
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(event.getAction() == KeyEvent.ACTION_DOWN){
            if (keyCode == KeyEvent.KEYCODE_BACK) {
                if (webView.canGoBack()) {
                    webView.goBack();
                } else {
                    // Go back to MainActivity.
                    finish();
                }
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }
    @Override public void finish() {
        ViewGroup view = (ViewGroup) getWindow().getDecorView();
        view.removeAllViews(); // Avoids window leaking exception.
        super.finish();
    }
// ###########################################################
//    Action bar functions:
// ###########################################################
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
// ###########################################################
//        This is the AboutProgressDialog dialog.
//        Its layout file is dialog_progress.xml.
// ###########################################################
    public class AboutProgressDialog extends AlertDialog {
        public AboutProgressDialog(Context context) {
            super(context);
        }
        @Override
        public void show() {
            super.show();
            setContentView(R.layout.dialog_progress);
            ((TextView) findViewById(R.id.text)).setText("Loading About page...");
        }
    }
}