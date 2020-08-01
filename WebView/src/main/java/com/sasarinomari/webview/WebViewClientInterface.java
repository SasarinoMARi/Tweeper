package com.sasarinomari.webview;

public interface WebViewClientInterface
{
    void onPageFinished( String url );
    boolean shouldOverrideUrlLoading( String url );
}
