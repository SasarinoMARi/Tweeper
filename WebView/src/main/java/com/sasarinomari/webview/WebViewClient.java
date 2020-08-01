package com.sasarinomari.webview;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.provider.Browser;
import android.util.Log;
import android.webkit.WebView;

/**
 * Created by MARi on 2018-01-26.
 */


class WebViewClient extends android.webkit.WebViewClient
{
    private final static String TAG = "BoomWebViewClient";
    WebViewClientInterface callback = null;
    private String redirectUrl;

    public WebViewClient(WebViewClientInterface callback )
    {
        this.callback = callback;
    }

    @Override
    public void onPageFinished( WebView view, String url )
    {
        super.onPageFinished( view, url );
        if ( callback != null )
            callback.onPageFinished( url );
    }

    @Override
    public boolean shouldOverrideUrlLoading( WebView view, String url )
    {
        if ( url.startsWith( "http://m.facebook.com/share.php" ) )
        {
            // 페이스북 공유 진입
            redirectUrl = view.getUrl( );
            return false;
        }
        else if ( url.startsWith( "https://www.facebook.com/dialog/return/close" ) )
        {
            view.loadUrl( redirectUrl );
            return true;
        }
        else if ( url.startsWith( "intent:kakaolink://send" ) )
        {
            try
            {
                Intent intent = new Intent( Intent.ACTION_VIEW, Uri.parse( url.substring( 7 ) ) ); // mainUrl 앞의 인탠트 부분 제거
                intent.addCategory( Intent.CATEGORY_BROWSABLE );
                intent.putExtra( Browser.EXTRA_APPLICATION_ID, view.getContext().getPackageName( ) );
                view.getContext().startActivity( intent );
            } catch ( ActivityNotFoundException e )
            {
                Intent intent = new Intent( Intent.ACTION_VIEW );
                intent.setData( Uri.parse( "market://details?id=" + "com.kakao.talk" ) );
                view.getContext().startActivity( intent );
                Log.d( TAG, "카카오톡이 설치되어있지 않습니다 : " + e.toString( ) );
            } catch ( Exception e )
            {
                Log.d( TAG, e.toString( ) );
            }
            return true;
        }
        else if ( url.startsWith( "tel:" ) )
        {
            Intent tel = new Intent( Intent.ACTION_DIAL, Uri.parse( url ) );
            view.getContext( ).startActivity( tel );
            return true;
        }
        else if ( url.startsWith( "mailto:" ) )
        {
            String body = "Enter your Question, Enquiry or Feedback below:\n\n";
            Intent mail = new Intent( Intent.ACTION_SEND );
            mail.setType( "application/octet-stream" );
            mail.putExtra( Intent.EXTRA_EMAIL, new String[]{ "email address" } );
            mail.putExtra( Intent.EXTRA_SUBJECT, "Subject" );
            mail.putExtra( Intent.EXTRA_TEXT, body );
            view.getContext( ).startActivity( mail );
            return true;
        }
        else return callback != null && callback.shouldOverrideUrlLoading( url );
    }

    @Override
    public void onReceivedError( WebView view, int errorCode, String description, String failingUrl )
    {
        super.onReceivedError( view, errorCode, description, failingUrl );

        switch ( errorCode )
        {
            case ERROR_AUTHENTICATION: // 서버에서 사용자 인증 실패
            case ERROR_BAD_URL: // 잘못된 URL
            case ERROR_CONNECT: // 서버로 연결 실패
            case ERROR_FAILED_SSL_HANDSHAKE: // SSL handshake 수행 실패
            case ERROR_FILE: // 일반 파일 오류
            case ERROR_FILE_NOT_FOUND: // 파일을 찾을 수 없습니다
            case ERROR_HOST_LOOKUP: // 서버 또는 프록시 호스트 이름 조회 실패
            case ERROR_IO: // 서버에서 읽거나 서버로 쓰기 실패
            case ERROR_PROXY_AUTHENTICATION: // 프록시에서 사용자 인증 실패
            case ERROR_REDIRECT_LOOP: // 너무 많은 리디렉션
            case ERROR_TIMEOUT: // 연결 시간 초과
            case ERROR_TOO_MANY_REQUESTS: // 페이지 로드중 너무 많은 요청 발생
            case ERROR_UNKNOWN: // 일반 오류
            case ERROR_UNSUPPORTED_AUTH_SCHEME: // 지원되지 않는 인증 체계
            case ERROR_UNSUPPORTED_SCHEME:
                //view.loadUrl( "about:blank" );
                break;
        }
    }

}

