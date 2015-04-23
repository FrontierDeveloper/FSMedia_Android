package androidfrontiersci;

/*
    This is the AsyncFollowUp interface.
    The definitions of these functions are in MainActivity.java.
    They are called in the onPostExecute() functions of the various classes that extend AsyncTask.
*/
public interface AsyncFollowUp {
    void reparseXML();             // Called from XmlDownloader.java
    void downloadXML();            // Called from XmlParser.java
    void postDownloadParse();      // Called from XmlDownloader.java
    void hideLoadingScreen();      // Called from ImageProcessor.java
    void postParseImageDownload(); // Called from XmlParser.java
}
