package test.yhc.ytdn;
import static test.yhc.ytdn.Utils.eAssert;
import static test.yhc.ytdn.Utils.logW;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;



//
// This is main class for HACKING Youtube protocol.
//
public class YTHacker{
    public static final int     YTQUALITY_SCORE_MAXIMUM     = 100;
    public static final int     YTQUALITY_SCORE_HIGHEST     = 100;
    public static final int     YTQUALITY_SCORE_HIGH        = 80;
    public static final int     YTQUALITY_SCORE_MIDHIGH     = 60;
    public static final int     YTQUALITY_SCORE_MIDLOW      = 40;
    public static final int     YTQUALITY_SCORE_LOW         = 20;
    public static final int     YTQUALITY_SCORE_LOWEST      = 0;
    public static final int     YTQUALITY_SCORE_MINIMUM     = 0;

    // See youtube api documentation.
    private static final int    YTVID_LENGTH = 11;

    private static final int    YTQSCORE_INVALID        = -1;

    // TODO
    // This is from experimental result and hacking script code.
    // Need to verify below tags again!
    private static final int    YTSTREAMTAG_INVALID     = -1;

    private static final int    YTSTREAMTAG_MPEG_1080p  = 37;
    private static final int    YTSTREAMTAG_MPEG_720p   = 22;
    private static final int    YTSTREAMTAG_MPEG_480p   = 35;
    private static final int    YTSTREAMTAG_MPEG_360p   = 18;

    private static final int    YTSTREAMTAG_3GPP_240p   = 36;
    private static final int    YTSTREAMTAG_3GPP_144p   = 17;

    private static final int    YTSTREAMTAG_WEBM_1080p  = 46;
    private static final int    YTSTREAMTAG_WEBM_720p   = 45;
    private static final int    YTSTREAMTAG_WEBM_480p   = 44;
    private static final int    YTSTREAMTAG_WEBM_360p   = 43;

    private static final int    YTSTREAMTAG_FLV_360p    = 34;
    private static final int    YTSTREAMTAG_FLV_240p    = 5;

    private static final Pattern    sYtUrlStreamMapPattern
        = Pattern.compile(".*\"url_encoded_fmt_stream_map\": \"([^\"]+)\".*");

    private static final Pattern    sYtUrlGenerate204Pattern
        = Pattern.compile(".*\\(\"(http\\:.+\\/generate_204\\?[^)]+)\"\\).*");

    private final NetLoader     mLoader = new NetLoader();;
    private final String        mYtvid;

    private YtVideoHtmlResult   mYtr = null;


    public static class YtVideo {
        public final String   url;
        public final String   type; // mime
        YtVideo(String aUrl, String aType) {
            url = aUrl;
            type = aType;
        }
    }

    private static class YtVideoElem {
        String  url         = "";
        String  tag         = "";
        String  type        = "";
        String  quality     = "";
        // Quality score of this video
        // This value is guesses from 'tag'
        // -1 means "invalid, so, DO NOT use this video".
        int     qscore      = YTQSCORE_INVALID;

        private YtVideoElem() {} // DO NOT CREATE DIRECTLY

        private static int
        getQuailityScore(String tag) {
            int v = -1;
            try {
                v = Integer.parseInt(tag);
            } catch (NumberFormatException e) {
                eAssert(false);
            }

            switch (v) {
            case YTSTREAMTAG_MPEG_1080p:
                return YTQUALITY_SCORE_HIGHEST;
            case YTSTREAMTAG_MPEG_720p:
                return YTQUALITY_SCORE_HIGH;
            case YTSTREAMTAG_MPEG_480p:
                return YTQUALITY_SCORE_MIDHIGH;
            case YTSTREAMTAG_MPEG_360p:
                return YTQUALITY_SCORE_MIDLOW;
            case YTSTREAMTAG_3GPP_240p:
                return YTQUALITY_SCORE_LOW;
            case YTSTREAMTAG_3GPP_144p:
                return YTQUALITY_SCORE_LOWEST;
            default:
                // TODO
                // Check it!
                // Webm and Flv should be considered here?
                // Webm and Flv format is NOT used!
                return YTQSCORE_INVALID;
            }
        }


        static YtVideoElem
        parse(String ytString) {
            YtVideoElem ve = new YtVideoElem();
            try {
                ytString = URLDecoder.decode(ytString, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                eAssert(false);
            }

            String   sig = null;
            String[] elems = ytString.split("\\\\u0026");
            for (String e : elems) {
                if (e.startsWith("itag="))
                    ve.tag = e.substring("itag=".length());
                else if (e.startsWith("url=")) {
                    ve.url = e.substring("url=".length());
                } else if (e.startsWith("type=")) {
                    String type = e.substring("type=".length());
                    int idx = type.indexOf(';');
                    if (idx >= 0)
                        type = type.substring(0, idx);
                    ve.type = type;
                } else if (e.startsWith("quality="))
                    ve.quality = e.substring("quality=".length());
                else if (e.startsWith("sig="))
                    sig = e.substring("sig=".length());
            }
            eAssert(!ve.url.isEmpty()
                    && !ve.tag.isEmpty()
                    && !ve.type.isEmpty()
                    && !ve.quality.isEmpty());
            if (null != sig)
                ve.url += "&signature=" + sig;
            else
                logW("NO SIGNATURE in URL STRING!!!");
            ve.qscore = getQuailityScore(ve.tag);
            return ve;
        }

        static String
        dump(YtVideoElem e) {
            return "[Video Elem]\n"
                    + "  itag=" + e.tag + "\n"
                    + "  url=" + e.url + "\n"
                    + "  type=" + e.type + "\n"
                    + "  quality=" + e.quality + "\n"
                    + "  qscore=" + e.qscore;
        }
    }

    private static class YtVideoHtmlResult {
        String         generate_204_url = ""; // url including generate 204
        YtVideoElem[]  vids = new YtVideoElem[0];
    }

    private static String
    getYtUri(String ytvid) {
        eAssert(YTVID_LENGTH == ytvid.length());
        return "watch?v=" + ytvid;
    }

    private static String
    getYtHost() {
        return "www.youtube.com";
    }

    private static YtVideoHtmlResult
    parseYtVideoHtml(BufferedReader brdr)
            throws YTDNException {
        YtVideoHtmlResult result = new YtVideoHtmlResult();
        String line = "";
        while (null != line) {
            try {
                line = brdr.readLine();
            } catch (IOException e) {
                throw new YTDNException(Err.IO_UNKNOWN);
            }

            if (null == line)
                break;

            if (line.contains("/generate_204?")) {
                Matcher m = sYtUrlGenerate204Pattern.matcher(line);
                if (!m.matches())
                    eAssert(false);

                line = m.group(1);
                line = line.replaceAll("\\\\u0026", "&");
                line = line.replaceAll("\\\\", "");
                result.generate_204_url = line;
            } else if (line.contains("\"url_encoded_fmt_stream_map\":")) {
                Matcher m = sYtUrlStreamMapPattern.matcher(line);
                if (!m.matches())
                    eAssert(false);

                line = m.group(1);
                String[] vidElemUrls = line.split(",");
                result.vids = new YtVideoElem[vidElemUrls.length];
                int i = 0;
                for (String s : vidElemUrls)
                    result.vids[i++] = YtVideoElem.parse(s);
            }
        }
        return result;
    }

    public static String
    getYtVideoPageUrl(String videoId) {
        return "http://" + getYtHost() + "/" + getYtUri(videoId);
    }

    public YTHacker(String ytvid) {
        // loader should "opened loader"
        mYtvid = ytvid;
    }

    public YtVideo
    getVideo(int quality) {
        //return new YtVideo(mYtr.generate_204_url.replace("generate_204", "videoplayback"), "");
        eAssert(null != mYtr);
        eAssert(0 <= quality && quality <= 100);

        // Select video that has closest quality score
        YtVideoElem ve = null;
        int qgap = -1;
        int curgap = -1;
        for (YtVideoElem e : mYtr.vids) {
            if (YTQSCORE_INVALID != e.qscore) {
                qgap = quality - e.qscore;
                qgap = qgap < 0? -qgap: qgap;
                if (null == ve) {
                    ve = e;
                    curgap = qgap;
                } else if (qgap < curgap) {
                    ve = e;
                    curgap = qgap;
                }
            }
        }
        eAssert(null != ve);
        return new YtVideo(ve.url, ve.type);
    }

    private NetLoader
    postRun(Err result) {
        if (Err.NO_ERR != result)
            mLoader.close();
        return mLoader;
    }


    public NetLoader start() {
        mLoader.open();
        NetLoader.HttpRespContent content;
        try {
            // Read and parse html web page of video.
            content = mLoader.getHttpContent(new URI(getYtVideoPageUrl(mYtvid)), true);
            eAssert(content.type.toLowerCase().startsWith("text/html"));
            mYtr = parseYtVideoHtml(new BufferedReader(new InputStreamReader(content.stream)));
            if (mYtr.vids.length <= 0)
                return postRun(Err.UNKNOWN);

            // NOTE
            // HACK youtube protocol!
            // Do dummy 'GET' request with generate_204 url.
            content = mLoader.getHttpContent(new URI(mYtr.generate_204_url), false);
            eAssert(HttpUtils.SC_NO_CONTENT == content.stcode);
            // Now all are ready to download!

        } catch (URISyntaxException e) {
            return postRun(Err.UNKNOWN);
        } catch (YTDNException e) {
            return postRun(e.getError());
        }
        return postRun(Err.NO_ERR);
    }

}
