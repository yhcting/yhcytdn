package test.yhc.ytdn;

public class Policy {
    // --------------------------------------------------------------------
    // Network access
    // --------------------------------------------------------------------
    public static final int     PROXY_PORT              = 59923;
    public static final String  HTTP_UASTRING
        = "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/535.19 (KHTML, like Gecko) Ubuntu/12.04 Chromium/18.0.1025.168 Chrome/18.0.1025.168 Safari/535.19";
    // Too long : user waits too long time to get feedback.
    // Too short : fails on bad network condition.
    public static final int     NETWORK_CONN_TIMEOUT    = 5000;
    public static final int     NETOWRK_CONN_RETRY      = 3;
}
