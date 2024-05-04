package app.revanced;

import android.net.Uri;
import androidx.annotation.Nullable;
import app.revanced.integrations.shared.Logger;
import app.revanced.integrations.shared.Utils;
import app.revanced.integrations.youtube.requests.Requester;
import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.downloader.Downloader;
import org.schabi.newpipe.extractor.downloader.Request;
import org.schabi.newpipe.extractor.downloader.Response;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.services.youtube.YoutubeService;
import org.schabi.newpipe.extractor.services.youtube.linkHandler.YoutubeStreamLinkHandlerFactory;
import org.schabi.newpipe.extractor.stream.AudioStream;
import org.schabi.newpipe.extractor.stream.VideoStream;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;

public class Test {
    private static Map<Integer, String> formats = new HashMap<>();


    @Nullable
    public static HttpURLConnection makeRequest(final Request request) {
        try {
            Utils.verifyOffMainThread();
            //Objects.requireNonNull(request.dataToSend());


            HttpURLConnection connection = (HttpURLConnection) new URL(request.url()).openConnection();
            connection.setRequestMethod(request.httpMethod());


            String agentString = "Mozilla/5.0 (Windows NT 10.0; rv:91.0) Gecko/20100101 Firefox/91.0";
            connection.setRequestProperty("User-Agent", agentString);

            for (final Map.Entry<String, List<String>> pair : request.headers().entrySet()) {
                final String headerName = pair.getKey();
                final List<String> headerValueList = pair.getValue();

                if (headerValueList.size() > 1) {
                    for (final String headerValue : headerValueList) {
                        connection.addRequestProperty(headerName, headerValue);
                    }
                } else if (headerValueList.size() == 1) {
                    connection.addRequestProperty(headerName, headerValueList.get(0));
                }
            }

            final byte[] innerTubeBody = request.dataToSend();
            if (innerTubeBody != null) {
                connection.getOutputStream().write(innerTubeBody, 0, innerTubeBody.length);
            }

            final int responseCode = connection.getResponseCode();
            if (responseCode == 200) return connection;
            else if (responseCode == 429) {
                throw new Exception("reCaptcha Challenge requested");
            } else {
                throw new Exception("Error making request: " + responseCode);
            }

        } catch (Exception ignored) {
            Logger.printInfo(() -> "Hooked Error making request: " + ignored.getMessage(), ignored);
        }

        return null;
    }

    public static String hook(String s) {

        if (!s.contains("googlevideo")) return s;
        if (formats.isEmpty()) {
            try {
                NewPipe.init(new Downloader() {
                    @Override
                    public Response execute(Request request) throws IOException {
                        var c = makeRequest(request);
                        var r = new Response(
                                c.getResponseCode(),
                                c.getResponseMessage(),
                                c.getHeaderFields(),
                                Requester.parseString(c),
                                c.getURL().toString()
                        );
                        c.disconnect();;
                        return r;
                    }
                });
                var extractor = new YoutubeService(1).getStreamExtractor(YoutubeStreamLinkHandlerFactory.getInstance().fromId("piKJAUwCYTo"));
                extractor.fetchPage();

                for (AudioStream audioStream : extractor.getAudioStreams()) {
                    formats.put(audioStream.getItag(), audioStream.getContent());
                }

                for (VideoStream videoOnlyStream : extractor.getVideoOnlyStreams()) {
                    formats.put(videoOnlyStream.getItag(), videoOnlyStream.getContent());
                }

                for (VideoStream videoStream : extractor.getVideoStreams()) {
                    formats.put(videoStream.getItag(), videoStream.getContent());
                }
            } catch (ExtractionException | IOException ignored) {
                Logger.printInfo(() -> "Hooked Error making request: " + ignored.getMessage(), ignored);
            }

            //formats = StoryboardRendererRequester.getFormats("piKJAUwCYTo");
        }
        var itag = Uri.parse(s).getQueryParameter("itag");
        Logger.printInfo(() -> "Hooked itag: " + itag);

        if (itag == null) return s;

        // find nearest key to itag
        var availableTags = formats.keySet();
        Integer nearest;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            nearest = availableTags.stream().min(Comparator.comparingInt(a -> Math.abs(Integer.parseInt(itag) - a))).orElse(null);
        } else {
            nearest = null;
        }
        Logger.printInfo(() -> "Hooked count: " + formats.size());
        Logger.printInfo(() -> "Hooked nearest " + nearest);

        String m = formats.get(nearest);
        Logger.printInfo(() -> String.valueOf(("Hooked format " + m == null)));
        if (m == null) {
            Logger.printInfo(() -> "Hooked format null");
            return s;
        }
        Logger.printInfo(() -> "Hooked");
        return m;
    }
}