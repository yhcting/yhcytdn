package test.yhc.ytdn;

import static test.yhc.ytdn.Utils.eAssert;
import static test.yhc.ytdn.Utils.logW;

import java.io.File;
import java.io.FileOutputStream;
import java.net.URI;

public class Main {
    public static void main(String[] args) {
        Utils.init();
        NetLoader loader = null;
        try {
            YTHacker ythack = new YTHacker("VkW2N-blZcc");

            loader = ythack.start();
            eAssert(null != loader);
            YTHacker.YtVideo ytv = ythack.getVideo(YTHacker.YTQUALITY_SCORE_MIDLOW);

            // why - 1st works
            // but second doesn't work???
            //content = mLoader.getHttpContent(new URI(mYtr.generate_204_url.replace("generate_204", "videoplayback")), false);
            NetLoader.HttpRespContent content = loader.getHttpContent(new URI(ytv.url), false);
            File f = new File("/home/hbg683/ytdntest.mp4");
    //        Long iBytesMax = Long.parseLong(mResponse.getFirstHeader("Content-Length").getValue());
            FileOutputStream fos = new FileOutputStream(f);
            byte[] bytes = new byte[4*4096];
            Integer iBytesRead = 1;

            while (iBytesRead>0) {
                iBytesRead = content.stream.read(bytes);
                try {fos.write(bytes,0,iBytesRead);} catch (IndexOutOfBoundsException ioob) {}
            } // while
            fos.close();
        } catch (Exception e) {
            logW("Unexpected Exception " + e.getMessage());
        } finally {
            if (null != loader)
                loader.close();
        }
    }
}
