package com.sasarinomari.webview;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by MARi on 2018-01-26.
 */

public class WebViewLoginAssistant
{
    private static String preferenceName = ".WebViewLoginInfo";

    public static String getAutoLoginParameters( Context context )
    {
        SharedPreferences pref = context.getSharedPreferences( context.getPackageName() + preferenceName, Activity.MODE_PRIVATE );
        String id = pref.getString( "id", "" );
        String pw = pref.getString( "pw", "" );
        if ( "".equals( id ) || "".equals( pw ) )
            return null;
        return "id=" + id + "&pw=" + pw;
    }

    public static void setAutoLoginParameters( Context context, String id, String pw )
    {
        SharedPreferences pref = context.getSharedPreferences( context.getPackageName() + preferenceName, Activity.MODE_PRIVATE );
        SharedPreferences.Editor editor = pref.edit( );
        editor.putString( "id", id );
        editor.putString( "pw", pw );
        editor.apply( );
    }

    public static void removeAutoLoginParameters( Context context )
    {
        SharedPreferences pref = context.getSharedPreferences( context.getPackageName() + preferenceName, Activity.MODE_PRIVATE );
        SharedPreferences.Editor editor = pref.edit( );
        editor.remove( "id" );
        editor.remove( "pw" );
        editor.apply( );
    }
}
