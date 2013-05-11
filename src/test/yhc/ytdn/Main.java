/*****************************************************************************
 *    Copyright (C) 2012, 2013 Younghyung Cho. <yhcting77@gmail.com>
 *
 *    This file is part of yhcytdn.
 *
 *    This program is free software: you can redistribute it and/or modify
 *    it under the terms of the GNU Lesser General Public License as
 *    published by the Free Software Foundation either version 3 of the
 *    License, or (at your option) any later version.
 *
 *    This program is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU Lesser General Public License
 *    (<http://www.gnu.org/licenses/lgpl.html>) for more details.
 *
 *    You should have received a copy of the GNU General Public License
 *    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *****************************************************************************/

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
