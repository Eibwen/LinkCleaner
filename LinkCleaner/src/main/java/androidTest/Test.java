package androidTest;

import android.net.Uri;
import android.test.InstrumentationTestCase;

/**
 * Created by gwalker on 5/6/2014.
 */
public class Test extends InstrumentationTestCase {
    public void testUriParsing() {
        Uri uri = Uri.parse("http://youtu.be/n3ioR9cJEIY?t=19s");
        assertEquals(uri.getQueryParameter("ffff"), null);
    }
}
