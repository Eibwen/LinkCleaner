package com.eibwen.linkcleaner.linkcleaner;

import android.os.AsyncTask;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.entity.BufferedHttpEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.regex.*;

/**
 * Created by gwalker on 5/6/2014.
 */
public class WebpageParser {
    /**
     * Copied from http://stackoverflow.com/questions/3158498/downloading-a-web-page-with-android
     */
    public static String getPage_crappier(URL url) throws IOException {
        HttpGet httpRequest = null;

        try {
            httpRequest = new HttpGet(url.toURI());
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

        HttpClient httpclient = new DefaultHttpClient();
        HttpResponse response = (HttpResponse) httpclient.execute(httpRequest);

        HttpEntity entity = response.getEntity();
        BufferedHttpEntity bufHttpEntity = new BufferedHttpEntity(entity);
        InputStream stream = bufHttpEntity.getContent();

        final BufferedReader reader;

        Header ct = response.getFirstHeader("charset");

        if (ct != null) {
            reader = new BufferedReader(new InputStreamReader(stream, ct.getValue()));
        }else {
            reader = new BufferedReader(new InputStreamReader(stream));
        }

        final StringBuilder sb = new StringBuilder();

        String line;
        while ((line = reader.readLine()) != null) {
            sb.append(line);
        }

        stream.close();
        return sb.toString();
    }

    /**
     * From http://stackoverflow.com/questions/3067827/download-html-source-android
     */
    public static String getHtml(String url) throws IOException {
        HttpClient httpClient = new DefaultHttpClient();
        HttpContext localContext = new BasicHttpContext();
        HttpGet httpGet = new HttpGet(url);
        HttpResponse response = httpClient.execute(httpGet, localContext);

        BufferedReader reader = new BufferedReader(
                new InputStreamReader(
                        response.getEntity().getContent()
                )
        );

        StringBuilder sb = new StringBuilder();
        String line = null;
        while ((line = reader.readLine()) != null) {
            sb.append(line + "\n");
        }
        return sb.toString();
    }

    public static void parseWebpage(String url, ParseWebsiteCallback callback, Pattern pattern) throws IOException {
        if (url == null || url.isEmpty()) return;

        new ParseWebsiteTask(callback, pattern).execute(url);
    }
}
interface ParseWebsiteCallback{
    void parseResult(String[] results);
}
class ParseWebsiteTask extends AsyncTask<String, Void, String> {

    private Exception exception;
    private ParseWebsiteCallback callback;
    private Pattern parsePattern;

    ParseWebsiteTask(ParseWebsiteCallback callback, Pattern parsePattern) {
        this.callback = callback;

        this.parsePattern = parsePattern;
    }

    @Override
    protected String doInBackground(String... urls) {
        try {
            for (String url : urls) {
                //TODO wtf params?
                return WebpageParser.getHtml(url);
            }
        } catch (Exception e) {
            this.exception = e;
        }
        return null;
    }

    protected void onPostExecute(String webpage) {
        Matcher m = parsePattern.matcher(webpage);
        ArrayList<String> output = new ArrayList<String>();
        while (m.find()) {
            output.add(m.group(1));
        }
        callback.parseResult(output.toArray(new String[output.size()]));
    }
}