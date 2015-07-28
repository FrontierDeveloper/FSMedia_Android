// RowItem.java

package androidfrontiersci.listviews;

import android.graphics.drawable.Drawable;

/*
    This is the RowItem class, used in every instance of a CustomListViewAdapter. It simply handles
    the text and image resources for a single item in a ListView.
*/
public class RowItem {

    // The class' private variables
    private int iconId;
    private String title;
    private Drawable icon;

/*
    The constructors, setting the member variables as given
*/
    public RowItem(int iconId, String title) {
    	this.icon = null;
        this.iconId = iconId;
        this.title = title;
    }
    public RowItem(Drawable icon, String title) {
    	this.iconId = 0;
    	this.icon = icon;
    	this.title = title;
    }

/*
    Member functions:
*/
    public int getIconId() {
        return iconId;
    }
    public Drawable getIcon() {
    	return icon;
    }
    public void setIcon(Drawable icon) {
    	this.icon = icon;
    }
    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
    }
    @Override
    public String toString() {
        return title;
    }   
}