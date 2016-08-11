package androidfrontiersci.articles;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import androidfrontiersci.MainActivity;
import androidfrontiersci.listviews.CustomListViewAdapter;
import androidfrontiersci.listviews.RowItem;
import frontsci.android.R;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
// ###########################################################
//    This is ArticlesListActivity, the activity started when the Articles section is selected from
//    the main menu.
//    Its layout file is activity_articles_list.xml.
//    Its menu file is articles_list.xml.
//    This class displays a ListView of the newest ten articles from frontierscientists.com as parsed
//    from http://frontierscientists.com/feed in ArticlesXmlParser.java. It creates a new
//    CustomListViewAdapter with the forArticlesList flag having been set to true in
//    ArticlesXmlParser.java. Articles already viewed, any included in the old_articles.txt file, are
//    displayed without the "new" icon. When leaving the activity, the old_articles.txt file is
//    updated with the newly viewed articles.
// ###########################################################
public class ArticlesListActivity extends Activity {
    // ###########################################################
    // These two variables are accessed in ArticleActivity.java when creating the WebView.
    // ###########################################################
    public static String article_name = "";
    public static String article_url = "";
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_articles_list);
        // A new ListView is created to display the article titles.
        ListView Articles = (ListView) findViewById(R.id.articles_listview);
        final List<RowItem> rowItems = new ArrayList<RowItem>();
        for (String title : ArticlesXmlParser.article_urls.keySet()) {
            RowItem item = new RowItem(null, title);
            rowItems.add(item);
        }
        CustomListViewAdapter adapter = new CustomListViewAdapter(getApplicationContext(), R.layout.
                articles_list_item, rowItems);
        Articles.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, final View view, final int position,
                                    long id) {
                view.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        switch (event.getAction()) {
                            case MotionEvent.ACTION_DOWN: // When pressed, darken.
                                view.setBackgroundColor(Color.argb(25, Color.BLACK, 0, 0));
                                return true;
                            case MotionEvent.ACTION_UP: // When released, go back to normal.
                                view.setBackgroundColor(Color.TRANSPARENT);
                                break;
                            }
                         return false;
                    }
                });
                selectItem(position, rowItems);
            }
        });
        Articles.setAdapter(adapter);
	}
    // When the back key is pressed, the activity is being navigated away from, the forArticlesList
    // flag is set to false and the old_articles.txt file is updated with the newly viewed articles.
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(event.getAction() == KeyEvent.ACTION_DOWN){
            if (keyCode == KeyEvent.KEYCODE_BACK) {
                MainActivity.forArticlesList = false;  // Set to false when leaving the activity
                updateOldArticlesFile();
                finish();
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }
// ###########################################################
//    Helper functions:
// ###########################################################
    // selectItem
    // This function is called when an article in the list is selected. It sets article_name and
    // article_url to that of the selected item and starts the ArticleActivity to display it.
    private void selectItem(int position, List<RowItem> rowItems) {
        article_name = rowItems.get(position).getTitle();
        article_url = ArticlesXmlParser.article_urls.get(article_name);
        finish(); // End current instance of the ArticlesListActivity in order to reload it when
                  // leaving the ArticleActivity.
        Intent intent = new Intent(getApplicationContext(), ArticleActivity.class);
        startActivity(intent);
    }
    // updateOldArticlesFile
    // This function is called when the back button is pressed and the activity is being navigated
    // away from. It cleans out any articles that are no longer being displayed, generates the
    // output string for old_articles.txt and writes that string to old_articles.txt.
    private void updateOldArticlesFile() {
        String string = "";
        for (String article : MainActivity.old_articles) {
            // If an article on the list isn't among the current articles...
            if (!ArticlesXmlParser.article_urls.keySet().contains(article)) {
                MainActivity.old_articles.remove(article); // get rid of it.
            } else {
                string += article+"\n"; // Otherwise, add it to the output string
            }
        }
        // Write the output string to the old_articles.txt file
        byte[] data = string.getBytes();
        FileOutputStream outputStream;
        try {
            outputStream = new FileOutputStream(new File(getFilesDir(), "old_articles.txt"));
            outputStream.write(data);
            outputStream.flush();
            outputStream.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}