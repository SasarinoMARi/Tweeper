package com.sasarinomari.webview;

import android.content.Intent;

/**
 * Created by MARi on 2018-01-26.
 */

public interface WebChromeClientInterface
{
    void onStartActivityForResult( Intent intent, int RequestCode );
}
