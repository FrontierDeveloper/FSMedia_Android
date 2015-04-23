package androidfrontiersci.textviews;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.TextView;

/*
    This is the CustomTextView class, used by all TextViews throughout the project. This class very
    simply sets the desired font for the TextView.
*/
public class CustomTextView extends TextView {

    public CustomTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    public CustomTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CustomTextView(Context context) {
        super(context);
        init();
    }

    private void init() {
        Typeface tf = Typefaces.get(getContext(), "fonts/EraserDust/erasdust.ttf");
        setTypeface(tf);
    }

}