package kr.booms.webview;

import android.content.Intent;

/**
 * Created by MARi on 2018-01-26.
 */

public interface BoomWebChromeClientInterface
{
    void onStartActivityForResult( Intent intent, int RequestCode );
}
