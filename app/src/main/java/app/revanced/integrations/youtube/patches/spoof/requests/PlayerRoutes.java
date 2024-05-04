package app.revanced.integrations.youtube.patches.spoof.requests;

import app.revanced.integrations.shared.Logger;
import app.revanced.integrations.youtube.requests.Requester;
import app.revanced.integrations.youtube.requests.Route;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.HttpURLConnection;

@Deprecated
public final class PlayerRoutes {
    private static final String YT_API_URL = "https://www.youtube.com/youtubei/v1/";
    static final Route.CompiledRoute GET_STORYBOARD_SPEC_RENDERER = new Route(
            Route.Method.POST,
            "player" +
                    "?fields=storyboards.playerStoryboardSpecRenderer," +
                    "storyboards.playerLiveStoryboardSpecRenderer," +
                    "playabilityStatus.status"
    ).compile();

    static final Route.CompiledRoute GET_FORMATS = new Route(
            Route.Method.POST,
            "player" +
                    "?fields=streamingData.formats.itag,streamingData.formats.url,streamingData.adaptiveFormats.itag,streamingData.adaptiveFormats.url"
    ).compile();

    static final String WEB_INNERTUBE_BODY;
    static final String TV_EMBED_INNER_TUBE_BODY;

    /**
     * TCP connection and HTTP read timeout
     */
    private static final int CONNECTION_TIMEOUT_MILLISECONDS = 4 * 1000; // 4 Seconds.

    static {
        JSONObject innerTubeBody = new JSONObject();

        try {
            JSONObject context = new JSONObject();

            JSONObject client = new JSONObject();
            client.put("clientName", "WEB");
            client.put("clientVersion", "2.20240111.09.00");

            context.put("client", client);

            innerTubeBody.put("context", context);
            innerTubeBody.put("videoId", "%s");
        } catch (JSONException e) {
            Logger.printException(() -> "Failed to create innerTubeBody", e);
        }

        WEB_INNERTUBE_BODY = innerTubeBody.toString();

        JSONObject tvEmbedInnerTubeBody = new JSONObject();

        try {
            JSONObject context = new JSONObject();

            JSONObject client = new JSONObject();
            client.put("clientName", "TVHTML5_SIMPLY_EMBEDDED_PLAYER");
            client.put("clientVersion", "2.0");
            client.put("platform", "TV");
            client.put("clientScreen", "EMBED");

            JSONObject thirdParty = new JSONObject();
            thirdParty.put("embedUrl", "https://www.youtube.com/watch?v=%s");

            context.put("thirdParty", thirdParty);
            context.put("client", client);

            tvEmbedInnerTubeBody.put("context", context);
            tvEmbedInnerTubeBody.put("videoId", "%s");
        } catch (JSONException e) {
            Logger.printException(() -> "Failed to create tvEmbedInnerTubeBody", e);
        }

        TV_EMBED_INNER_TUBE_BODY = tvEmbedInnerTubeBody.toString();
    }

    private PlayerRoutes() {
    }

    /** @noinspection SameParameterValue*/
    public static HttpURLConnection getPlayerResponseConnectionFromRoute(Route.CompiledRoute route) throws IOException {
        var connection = Requester.getConnectionFromCompiledRoute(YT_API_URL, route);

        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; rv:91.0) Gecko/20100101 Firefox/91.0");

        connection.setUseCaches(false);
        connection.setDoOutput(true);

        connection.setConnectTimeout(CONNECTION_TIMEOUT_MILLISECONDS);
        connection.setReadTimeout(CONNECTION_TIMEOUT_MILLISECONDS);
        return connection;
    }
}