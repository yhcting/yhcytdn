package test.yhc.ytdn;

import static test.yhc.ytdn.Utils.eAssert;
import static test.yhc.ytdn.Utils.logW;

import java.io.File;
import java.io.FileOutputStream;
import java.net.URI;
import java.util.Map;

public class Main {
    public static String
    getHomeDirectory() {
        Map<String, String> env = System.getenv();
        return env.get("HOME");
    }

    public static void
    main(String[] args) {
        Utils.init();
        NetLoader loader = null;
        try {
            YTHacker ythack = new YTHacker("VkW2N-blZcc");

            loader = ythack.start();
            eAssert(null != loader);
            YTHacker.YtVideo ytv = ythack.getVideo(YTHacker.YTQUALITY_SCORE_MIDLOW);

            NetLoader.HttpRespContent content = loader.getHttpContent(new URI(ytv.url), false);
            File f = new File(getHomeDirectory() + "/yhcytdn_test.mp4");
            FileOutputStream fos = new FileOutputStream(f);
            byte[] bytes = new byte[4 * 4096];
            Integer iBytesRead = 1;

            while (iBytesRead>0) {
                iBytesRead = content.stream.read(bytes);
                try {fos.write(bytes,0,iBytesRead);} catch (IndexOutOfBoundsException ioob) {}
            }
            fos.close();
        } catch (Exception e) {
            logW("Unexpected Exception " + e.getMessage());
        } finally {
            if (null != loader)
                loader.close();
        }
    }
}
