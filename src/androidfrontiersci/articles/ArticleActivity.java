package androidfrontiersci.articles;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;
import android.widget.Toast;

import androidfrontiersci.MainActivity;

import com.example.androidfrontiersci.R;

/*
    This is ArticleActivity, started from ArticlesListActivity to display a WebView with the
    selected article.
    Its layout file is activity_article.xml.
    Its menu file is article.xml.
*/
public class ArticleActivity extends ArticlesListActivity {

    // The class' private variable. its WebView
    private WebView webView;
    private static ArticleProgressDialog progress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_article);
        // Add selected article to articles already seen
        MainActivity.old_articles.add(article_name);

        progress = new ArticleProgressDialog(ArticleActivity.this);
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

        webView.loadUrl(ArticlesListActivity.article_url);
    }

    // While in the WebView, the back button serves the WebView, unless pressed when at origin page,
    // then it ends the activity and goes back to the list of articles.
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(event.getAction() == KeyEvent.ACTION_DOWN){
            if (keyCode == KeyEvent.KEYCODE_BACK) {
                if (webView.canGoBack()) {
                    webView.goBack();
                } else {
                    finish();
                    // Reload the ArticlesListActivity with the article just viewed no longer marked
                    // as new.
                    Intent intent = new Intent(getApplicationContext(), ArticlesListActivity.class);
                    startActivity(intent);
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

/*
    This is the CustomProgressDialog dialog.
    Its layout file is dialog_progress.xml.
*/
    public class ArticleProgressDialog extends AlertDialog {

        public ArticleProgressDialog(Context context) {
            super(context);
        }

        @Override
        public void show() {
            super.show();
            setContentView(R.layout.dialog_progress);
            ((TextView) findViewById(R.id.text)).setText("Loading article...");
        }

    }
}
