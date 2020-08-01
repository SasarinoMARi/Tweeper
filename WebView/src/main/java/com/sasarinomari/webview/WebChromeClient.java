package com.sasarinomari.webview;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Message;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.ValueCallback;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static android.app.Activity.RESULT_OK;

class WebChromeClient extends android.webkit.WebChromeClient
{
    static final String TYPE_IMAGE = "image/*";
    static final int INPUT_FILE_REQUEST_CODE = 1;

    static String mCameraPhotoPath;
    static ValueCallback< Uri[] > mFilePathCallback;
    static ValueCallback< Uri > mUploadMessage;

    android.webkit.WebView webview;
    String customAgent;
    WebChromeClientInterface callback;

    public WebChromeClient(String customAgent, WebChromeClientInterface callback )
    {
        this.customAgent = customAgent;
        this.callback = callback;
    }

    @Override
    public void onCloseWindow(android.webkit.WebView w )
    {
        super.onCloseWindow( w );
    }

    @Override
    public boolean onCreateWindow(android.webkit.WebView view, boolean dialog, boolean userGesture, Message resultMsg )
    {
        this.webview = view;

        WebView.applyWebSettings( view, customAgent );

        view.setWebChromeClient( this );
        android.webkit.WebView.WebViewTransport transport = ( android.webkit.WebView.WebViewTransport ) resultMsg.obj;
        transport.setWebView( view );
        resultMsg.sendToTarget( );
        return false;
    }

    // region File chooser

    // For Android Version < 3.0
    public void openFileChooser( ValueCallback< Uri > uploadMsg )
    {
        //System.out.println("WebViewActivity OS Version : " + Build.VERSION.SDK_INT + "\t openFC(VCU), n=1");
        mUploadMessage = uploadMsg;
        Intent intent = new Intent( Intent.ACTION_GET_CONTENT );
        intent.addCategory( Intent.CATEGORY_OPENABLE );
        intent.setType( TYPE_IMAGE );
        callback.onStartActivityForResult( intent, INPUT_FILE_REQUEST_CODE);
    }

    // For 3.0 <= Android Version < 4.1
    public void openFileChooser( ValueCallback< Uri > uploadMsg, String acceptType )
    {
        //System.out.println("WebViewActivity 3<A<4.1, OS Version : " + Build.VERSION.SDK_INT + "\t openFC(VCU,aT), n=2");
        openFileChooser( uploadMsg, acceptType, "" );
    }

    // For 4.1 <= Android Version < 5.0
    public void openFileChooser( ValueCallback< Uri > uploadFile, String acceptType, String capture )
    {
        Log.d( getClass( ).getName( ), "openFileChooser : " + acceptType + "/" + capture );
        mUploadMessage = uploadFile;
        imageChooser( );
    }

    // For Android Version 5.0+
    // Ref: https://github.com/GoogleChrome/chromium-webview-samples/blob/master/input-file-example/app/src/main/java/inputfilesample/android/chrome/google/com/inputfilesample/MainFragment.java
    public boolean onShowFileChooser(android.webkit.WebView webView,
                                     ValueCallback< Uri[] > filePathCallback, android.webkit.WebChromeClient.FileChooserParams fileChooserParams )
    {
        System.out.println( "WebViewActivity A>5, OS Version : " + Build.VERSION.SDK_INT + "\t onSFC(WV,VCUB,FCP), n=3" );
        if ( mFilePathCallback != null )
        {
            mFilePathCallback.onReceiveValue( null );
        }
        mFilePathCallback = filePathCallback;
        imageChooser( );
        return true;
    }

    private void imageChooser( )
    {
        Intent takePictureIntent = new Intent( MediaStore.ACTION_IMAGE_CAPTURE );
        if ( takePictureIntent.resolveActivity( webview.getContext( ).getPackageManager( ) ) != null )
        {
            // Create the File where the photo should go
            File photoFile = null;
            try
            {
                photoFile = createImageFile( );
                takePictureIntent.putExtra( "PhotoPath", mCameraPhotoPath );
            } catch ( IOException ex )
            {
                // Error occurred while creating the File
                Log.e( getClass( ).getName( ), "Unable to create Image File", ex );
            }

            // Continue only if the File was successfully created
            if ( photoFile != null )
            {
                mCameraPhotoPath = "file:" + photoFile.getAbsolutePath( );
                takePictureIntent.putExtra( MediaStore.EXTRA_OUTPUT,
                        Uri.fromFile( photoFile ) );
            }
            else
            {
                takePictureIntent = null;
            }
        }

        Intent contentSelectionIntent = new Intent( Intent.ACTION_GET_CONTENT );
        contentSelectionIntent.addCategory( Intent.CATEGORY_OPENABLE );
        contentSelectionIntent.setType( TYPE_IMAGE );

        Intent[] intentArray;
        if ( takePictureIntent != null )
        {
            intentArray = new Intent[]{ takePictureIntent };
        }
        else
        {
            intentArray = new Intent[ 0 ];
        }

        Intent chooserIntent = new Intent( Intent.ACTION_CHOOSER );
        chooserIntent.putExtra( Intent.EXTRA_INTENT, contentSelectionIntent );
        chooserIntent.putExtra( Intent.EXTRA_TITLE, "Image Chooser" );
        chooserIntent.putExtra( Intent.EXTRA_INITIAL_INTENTS, intentArray );

        callback.onStartActivityForResult( chooserIntent, INPUT_FILE_REQUEST_CODE );
    }

    private File createImageFile( ) throws IOException
    {
        // Create an image file name
        String timeStamp = new SimpleDateFormat( "yyyyMMdd_HHmmss" ).format( new Date( ) );
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES );
        File imageFile = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );
        return imageFile;
    }

    private static Uri getResultUri( Context context, Intent data )
    {
        Uri result = null;
        if ( data == null || TextUtils.isEmpty( data.getDataString( ) ) )
        {
            // If there is not data, then we may have taken a photo
            if ( mCameraPhotoPath != null )
            {
                result = Uri.parse( mCameraPhotoPath );
            }
        }
        else
        {
            String filePath = "";
            if ( Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP )
            {
                filePath = data.getDataString( );
            }
            else
            {
                filePath = "file:" + RealPathUtil.getRealPath( context, data.getData( ) );
            }
            result = Uri.parse( filePath );
        }

        return result;
    }

    //endregion


    public static void onActivityResult( Context context, int requestCode, int resultCode, Intent data )
    {
        if ( requestCode == INPUT_FILE_REQUEST_CODE )
            if ( resultCode == RESULT_OK )
            {
                if ( Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP )
                {
                    if ( mFilePathCallback == null )
                    {
                        return;
                    }
                    Uri[] results = new Uri[]{ getResultUri( context, data ) };
                    mFilePathCallback.onReceiveValue( results );
                    mFilePathCallback = null;
                }
                else
                {
                    if ( mUploadMessage == null )
                    {
                        return;
                    }
                    Uri result = getResultUri( context, data );
                    mUploadMessage.onReceiveValue( result );
                    mUploadMessage = null;
                }
            }
            else
            {
                if ( mFilePathCallback != null )
                    mFilePathCallback.onReceiveValue( null );
                if ( mUploadMessage != null )
                    mUploadMessage.onReceiveValue( null );
                mFilePathCallback = null;
                mUploadMessage = null;
            }
    }
}
