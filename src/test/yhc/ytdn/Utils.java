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


import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class Utils {
    private static final boolean DBG    = true;

    // This is only for debugging.
    private static boolean  sInitialized = false;

    private static final String[] sDateFormats = new String[] {
            // To support W3CDTF
        "yyyy-MM-d'T'HH:mm:ss.SSSZ",
        "yyyy-MM-d'T'HH:mm:ss.SSS'Z'",
        "yyyy-MM-d'T'HH:mm:ssZ",
        "yyyy-MM-d'T'HH:mm:ss'Z'",
    };

    // ========================================================================
    //
    // Initialization
    //
    // ========================================================================

    public static void
    init() {
        // This is called first for module initialization.
        // So, ANY DEPENDENCY to other module is NOT allowed
        eAssert(!sInitialized);
        if (!sInitialized)
            sInitialized = true;

    }

    // ========================================================================
    //
    // Fundamentals
    //
    // ========================================================================
    private static enum LogLV{
        V ("[V]", 6),
        D ("[D]", 5),
        I ("[I]", 4),
        W ("[W]", 3),
        E ("[E]", 2),
        F ("[F]", 1);

        private String pref; // prefix string
        private int    pri;  // priority
        LogLV(String pref, int pri) {
            this.pref = pref;
            this.pri = pri;
        }

        String pref() {
            return pref;
        }

        int pri() {
            return pri;
        }
    }

    private static void
    log(LogLV lv, String msg) {
        if (!DBG || null == msg)
            return;

        switch(lv) {
        case V: System.out.println("[V] " +  msg); break;
        case D: System.out.println("[D] " + msg); break;
        case I: System.out.println("[I] " + msg); break;
        case W: System.out.println("[W] " + msg); break;
        case E: System.out.println("[E] " + msg); break;
        case F: System.out.println("[F] " + msg); break;
        }
    }

    // Assert
    public static void
    eAssert(boolean cond) {
        if (!cond)
            throw new AssertionError();
    }

    // For logging
    public static void logV(String msg) { log(LogLV.V, msg); }
    public static void logD(String msg) { log(LogLV.D, msg); }
    public static void logI(String msg) { log(LogLV.I, msg); }
    public static void logW(String msg) { log(LogLV.W, msg); }
    public static void logE(String msg) { log(LogLV.E, msg); }
    public static void logF(String msg) { log(LogLV.F, msg); }

    public static void
    copy(OutputStream os, InputStream is) throws IOException {
        byte buf[]=new byte[1024 * 16];
        int len;
        while((len = is.read(buf)) > 0)
            os.write(buf, 0, len);
    }
}
