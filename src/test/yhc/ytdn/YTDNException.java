package test.yhc.ytdn;


public class YTDNException extends Exception {
    private Err mErr;

    public YTDNException(Err err) {
        mErr = err;
    }

    public Err
    getError() {
        return mErr;
    }
}
