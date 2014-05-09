package com.eibwen.linkcleaner.linkcleaner;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.regex.Pattern;


public class MainActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment())
                    .commit();
        }


//        TextView original = (TextView)findViewById(R.id.link_original);
//        original.setText("Hello jerks");


//        final Intent intent = getIntent();
//        if (intent != null){
//            //TODO return here is not ideal apparently
//            if (processIntent(intent)) {
//                //return;
//            }
//        }
    }

//    public void onViewCreated (View view, Bundle savedInstanceState) {
//        TextView original = (TextView) findViewById(R.id.link_original);
//        original.setText("Hello jerks");
//
//
//        final Intent intent = getIntent();
//        if (intent != null) {
//            //TODO return here is not ideal apparently
//            if (processIntent(intent)) {
//                //return;
//            }
//        }
//    }

    /**
     *
     * @param intent
     * @return true if processing is complete
     */
    private boolean processIntent(Intent intent) {
        //TODO what is FLAG_GRANT_READ_URI_PERMISSION  on type image/jpeg, Class: android.net.Uri$HierarchicalUri
        String url = intent.getDataString();

        if (url == null) return false;

        TextView original = (TextView)findViewById(R.id.link_original);
        original.setText(url);

        String outputUrl = processUrl(url);

        setCleanedLink(outputUrl);

        if (furtherActions(outputUrl))
            return false;
        else{
            openLink();
            return true;
        }
    }

    private boolean furtherActions(String url) {
        //TODO ability to Further Process somehow (i.e. youtube find the media urls, parse .mp4 or .flv links, download from other sites)
        //TODO for unknown urls, prompt for options?

        return false;
    }

    final String YoutubeUrl = "https://www.youtube.com/watch?v={0}";
    private String processUrl(String url) {

        if (url.contains("facebook.com")) {
            Uri uri = Uri.parse(url);
            return processUrl(uri.getQueryParameter("u"));
        } else if (url.contains("youtube.com")) {
            Uri uri = Uri.parse(url);
            String videoId = uri.getQueryParameter("v");
            String queryData = videoId + getGoodYoutubeParams(uri);
            return YoutubeUrl + queryData;
        } else if (url.contains("youtu.be")) {
            Uri uri = Uri.parse(url);
            //TODO support http://youtu.be/n3ioR9cJEIY?t=19s
            String queryData = uri.getLastPathSegment() + getGoodYoutubeParams(uri);
            return YoutubeUrl + queryData;
        }

        //Check for 300?
        //http://bzfd.it/1fNKYs5 .. This does not send one...

        return url;
    }

    private String getGoodYoutubeParams(Uri uri) {
        //TODO find what a playlist link looks like...
        String[] goodParams = {"u"};
        StringBuilder sb = new StringBuilder();
        for (String goodParam : goodParams) {
            String param = uri.getQueryParameter(goodParam);
            if (param != null) {
                sb.append("&").append(goodParam).append("=").append(param);
            }
        }
        return sb.toString();
    }

    String cleanedLink = "";
    private void setCleanedLink(String link){
        TextView cleaned = (TextView)findViewById(R.id.link_cleaned);
        cleaned.setText(link);
        cleanedLink = link;
    }
    private String getCleanedLink(){
        return cleanedLink;
    }

    private void setLinkMessage(String message){
        TextView cleaned = (TextView)findViewById(R.id.cleanedLink);
        cleaned.setText(message);
    }

    public void openLink(){
        if (getCleanedLink() == null || getCleanedLink().isEmpty()) return;

        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(getCleanedLink()));
        startActivity(i);
    }



    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public void copyToClipboardClick(View v){
        ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newRawUri(getCleanedLink(), Uri.parse(getCleanedLink()));
        clipboard.setPrimaryClip(clip);
    }

    public void openLink(View v) {
        openLink();
    }

    public void findMp4Links(View v){
        try {
            WebpageParser.parseWebpage(getCleanedLink(), new fuckAndroid(), Pattern.compile("\"(.+\\.mp4)\""));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    class fuckAndroid implements ParseWebsiteCallback {

        @Override
        public void parseResult(String[] results) {
            setLinkMessage("Found " + results.length + " mp4s!");
        }
    }


    public void sendAnIntentLink(View v){
        String[] urls = new String[]{
                "http://m.facebook.com/l.php?u=http%3A%2F%2Fblog.petflow.com%2Fthis-is-a-video-everyone-needs-to-see-for-the-first-time-in-my-life-im-speechless%2F&h=WAQFk8n7j&s=1&enc=AZNpIN5aMlSUJ7awC1uTVxIhncnHmFBH59BcUqMisD9rKxnllf5Zgt5p5TrWlg9JEnlt2YwniK8mYqVWlbT9VQyU",
                "http://m.facebook.com/l.php?u=http%3A%2F%2Fyoutu.be%2Fn3ioR9cJEIY&h=gAQHz7f60&s=1&enc=AZPJL2UAzLpIS5B1oULtkkcGlJUhv50ilZvgKDlFAiKFzlwnx2fhI6YnANiLy3uiPYz6yV7irW2q2zUoa6QR23sl"
        };

        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(urls[0]));
        startActivity(i);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);

            return rootView;
        }

        @Override
        public void onViewCreated (View view, Bundle savedInstanceState) {
            Activity a = getActivity();
            TextView original = (TextView)a.findViewById(R.id.link_original);
            original.setText("Hello jerks");


            final Intent intent = a.getIntent();
            if (intent != null) {
                //TODO return here is not ideal apparently
                if (((MainActivity)a).processIntent(intent)) {
                    //return;
                }
            }
        }
    }
}
