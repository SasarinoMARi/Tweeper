package com.sasarinomari.webview;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.DownloadListener;
import android.webkit.WebSettings;
import android.widget.RelativeLayout;

/**
 * Created by MARi on 2018-01-26.
 */

public class WebView
{
    // region Instance

    private android.webkit.WebView mainView;
    private android.webkit.WebView popupView;

    private WebView( )
    {

    }

    // endregion


    // region Main WebView Interface

    public void loadUrl( final String dset )
    {
        mainView.post( new Runnable( )
        {
            @Override
            public void run( )
            {
                mainView.loadUrl( dset );
            }
        } );
    }

    public void reload( )
    {
        mainView.post( new Runnable( )
        {
            @Override
            public void run( )
            {
                mainView.reload( );
            }
        } );
    }

    public void goBack( )
    {
        mainView.post( new Runnable( )
        {
            @Override
            public void run( )
            {
                if ( mainView.canGoBack( ) )
                    mainView.goBack( );
            }
        } );
    }

    public boolean canGoBack( )
    {
        return mainView.canGoBack( );
    }

    public void addJavascriptInterface( Object javascriptInterface, String name )
    {
        mainView.addJavascriptInterface( javascriptInterface, name );
        popupView.addJavascriptInterface( javascriptInterface, name );
    }

    public String getUrl( )
    {
        return mainView.getUrl( );
    }

    // endregion

    // region Popup WebView Interface

    public void openPopup( final String url )
    {
        popupView.post( new Runnable( )
        {
            @Override
            public void run( )
            {
                popupView.loadUrl( url );
                popupView.setVisibility( View.VISIBLE );
            }
        } );
    }

    public void closePopup( final String redirectUrl )
    {
        popupView.post( new Runnable( )
        {
            @Override
            public void run( )
            {
                mainView.loadUrl( redirectUrl );
                popupView.setVisibility( View.GONE );
            }
        } );
    }

    // endregion


    // region Instance Factory

    public static WebView createWithContext(RelativeLayout holder, String customAgent, WebChromeClientInterface webChromeClientInterface, WebViewClientInterface webViewClientInterface )
    {
        WebView instance = new WebView( );

        android.webkit.WebView main = new android.webkit.WebView( holder.getContext( ) );
        main.setLayoutParams( new RelativeLayout.LayoutParams( RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT ) );
        holder.addView( main );
        instance.mainView = main;
        applyWebviewConfigurations( instance.mainView, customAgent, webChromeClientInterface, webViewClientInterface );

        android.webkit.WebView popup = new android.webkit.WebView( holder.getContext( ) );
        popup.setLayoutParams( new RelativeLayout.LayoutParams( RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT ) );
        holder.addView( popup );
        popup.setVisibility( View.GONE );
        instance.popupView = popup;
        applyWebviewConfigurations( instance.popupView, customAgent, webChromeClientInterface, webViewClientInterface );

        return instance;
    }

    /*
    public static BoomWebView createWithWebView( WebView view, String customAgent, BoomWebChromeClientInterface webChromeClientInterface, BoomWebViewClientInterface webViewClientInterface )
    {
        BoomWebView instance = new BoomWebView( );
        instance.mainView = view;
        applyWebviewConfigurations( instance.mainView, customAgent, webChromeClientInterface, webViewClientInterface );
        return instance;
    }
    */

    // endregion

    // region WebView Configurations Applier

    static void applyWebviewConfigurations(android.webkit.WebView webview, String customAgent, WebChromeClientInterface webChromeClientInterface, WebViewClientInterface webViewClientInterface )
    {
        if ( webview == null )
            return;

        applyWebSettings( webview, customAgent );
        applyWebChromeClient( webview, customAgent, webChromeClientInterface );
        applyWebViewClient( webview, webViewClientInterface );
        applyDownloadListener( webview );
        applyDefaultJavascriptInterface( webview );
    }

    public static void applyDefaultJavascriptInterface(android.webkit.WebView webview )
    {
        webview.addJavascriptInterface( new WebViewInterface( webview ), "BoomWebViewInterface" );
    }

    public static void applyWebSettings(android.webkit.WebView webview, String customAgent )
    {
        WebSettings set = webview.getSettings( );
        set.setJavaScriptEnabled( true );
        set.setDomStorageEnabled( true );
        set.setAllowFileAccess( true );
        set.setAllowContentAccess( true );
        set.setBuiltInZoomControls( false );
        set.setDefaultZoom( WebSettings.ZoomDensity.FAR );
        set.setSupportZoom( false );
        set.setJavaScriptCanOpenWindowsAutomatically( true );
        set.setDefaultTextEncodingName( "euc-kr" );
        set.setAppCacheEnabled( true );
        if(customAgent!=null) set.setUserAgentString( set.getUserAgentString( ) + customAgent );

        if ( Build.VERSION.SDK_INT >= 21 )
        {
            set.setMixedContentMode( WebSettings.MIXED_CONTENT_ALWAYS_ALLOW );
            CookieManager cookieManager = CookieManager.getInstance( );
            cookieManager.setAcceptCookie( true );
            cookieManager.setAcceptThirdPartyCookies( webview, true );

        }
    }

    public static void applyWebChromeClient(android.webkit.WebView webview, String customAgent, WebChromeClientInterface webChromeClientInterface )
    {
        webview.setWebChromeClient( new WebChromeClient( customAgent, webChromeClientInterface ) );
    }

    public static void applyWebViewClient(android.webkit.WebView webview, WebViewClientInterface callback )
    {
        webview.setWebViewClient( new WebViewClient( callback ) );
    }

    public static void applyDownloadListener( final android.webkit.WebView webview )
    {
        webview.setDownloadListener( new DownloadListener( )
        {
            public void onDownloadStart( String url, String userAgent, String contentDisposition, String mimetype, long contentLength )
            {
                Intent i = new Intent( Intent.ACTION_VIEW );
                i.setData( Uri.parse( url ) );
                webview.getContext( ).startActivity( i );
            }
        } );
    }

    // endregion

    //region Should Override in Activity

    public static void onActivityResult( Context context, int requestCode, int resultCode, Intent data )
    {
        WebChromeClient.onActivityResult( context, requestCode, resultCode, data );
    }

    public void saveState( Bundle outState )
    {
        mainView.saveState( outState );
    }

    public void restoreState( Bundle savedInstanceState )
    {
        mainView.restoreState( savedInstanceState );
    }

    // endregion

    @SuppressLint("ClickableViewAccessibility")
    public void setOnTouchListener (View.OnTouchListener l) {
        mainView.setOnTouchListener(l);
    }

}
