package kr.booms.webview;

public interface BoomWebViewClientInterface
{
    void onPageFinished( String url );
    boolean shouldOverrideUrlLoading( String url );
}
