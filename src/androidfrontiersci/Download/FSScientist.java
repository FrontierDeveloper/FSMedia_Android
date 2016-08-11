package androidfrontiersci.Download;
// ###########################################################
// This object defines a scientist for sciOnCall
// ###########################################################
import android.graphics.Bitmap;
public class FSScientist {
    public String name;
    public String bio;
    public Bitmap image;
    // ###########################################################
    public FSScientist(String name_, String bio_, Bitmap image_) {
        name = name_;
        bio = bio_;
        image = image_;
    }
}