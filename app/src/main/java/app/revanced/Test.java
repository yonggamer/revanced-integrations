package app.revanced;

import android.net.Uri;
import app.revanced.integrations.shared.Logger;
import app.revanced.integrations.youtube.patches.spoof.requests.StoryboardRendererRequester;

import java.util.Map;

public class Test {
    private static Map<Integer, String> formats;

    public static String hook(String s) {
        if (!s.contains("googlevideo")) return s;

        if (true) {
            var c2 = new Throwable().getStackTrace()[2].toString();
            var c = new Throwable().getStackTrace()[1].toString();
            var itag = Uri.parse(s).getQueryParameter("itag");
            Logger.printInfo(() -> "Hooked " + c2 + ": itag: " + itag);
            Logger.printInfo(() -> "Hooked " + c  + ": itag: " + itag);

            return s;
        }

        if (formats == null)
            formats = StoryboardRendererRequester.getFormats("piKJAUwCYTo");

        var itag = Uri.parse(s).getQueryParameter("itag");
        Logger.printInfo(() -> "Hooked itag: " + itag);

        if (itag == null) return s;

        String m;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            m = formats.values().stream().findFirst().get();
        } else {
            m = null;
        }
        Logger.printInfo(() -> "Hooked format: " + m);
        if (m == null) return s;

        var c = new Throwable().getStackTrace()[1].toString();
        Logger.printInfo(() -> "Hooked " + c + ": From " + s + " to " + m);

        return m;
    }
}