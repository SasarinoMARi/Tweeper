package kr.booms.webview;

import android.util.Log;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;

/**
 * Created by MARi on 2018-01-26.
 */

public class BoomWebViewInterface
{
    WebView webView;

    public BoomWebViewInterface( WebView webView) {this.webView = webView;}

    @JavascriptInterface
    public void applyLoginInfo(String id, String pw) {
        Log.i(  "BoomWebViewInterface", "applyLoginInfo" );
        WebViewLoginAssistant.setAutoLoginParameters(webView.getContext(), id, pw);
    }

    @JavascriptInterface
    public void removeLoginInfo() {
        Log.i(  "BoomWebViewInterface", "removeLoginInfo" );
        WebViewLoginAssistant.removeAutoLoginParameters(webView.getContext());
    }
}
