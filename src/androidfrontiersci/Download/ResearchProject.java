package androidfrontiersci.Download;

import android.graphics.Bitmap;
import java.util.List;

public class ResearchProject {
    public String title;
    public String description;
    public List<FSVideo> videos;
    public FSMapData mapData;
    public Bitmap image;
    public String imagePath;
    public int index = -1;

    public ResearchProject(String title_, String description_, List<FSVideo> videos_, FSMapData mapData_, Bitmap image_, String imagePath_) {
        title = title_;
        description = description_;
        videos = videos_;
        mapData = mapData_;
        image = image_;
        imagePath = imagePath_;
    }
}
