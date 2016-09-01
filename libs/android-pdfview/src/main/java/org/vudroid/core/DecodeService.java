<<<<<<< HEAD
package org.vudroid.core;

import android.content.ContentResolver;
import android.graphics.Bitmap;
import android.graphics.RectF;
import android.net.Uri;
import android.view.View;

import org.vudroid.core.codec.CodecPage;

public interface DecodeService
{
    void setContentResolver(ContentResolver contentResolver);

    void setContainerView(View containerView);

    void open(Uri fileUri);

    void decodePage(Object decodeKey, int pageNum, DecodeCallback decodeCallback, float zoom, RectF pageSliceBounds);

    void stopDecoding(Object decodeKey);

    int getEffectivePagesWidth();

    int getEffectivePagesHeight();

    int getPageCount();

    int getPageWidth(int pageIndex);

    int getPageHeight(int pageIndex);
    
    CodecPage getPage(int pageIndex);
    
    void recycle();
    
    

    public interface DecodeCallback
    {
        void decodeComplete(Bitmap bitmap);
    }
}
=======
package org.vudroid.core;

import android.content.ContentResolver;
import android.graphics.Bitmap;
import android.graphics.RectF;
import android.net.Uri;
import android.view.View;

import org.vudroid.core.codec.CodecPage;

public interface DecodeService
{
    void setContentResolver(ContentResolver contentResolver);

    void setContainerView(View containerView);

    void open(Uri fileUri);

    void decodePage(Object decodeKey, int pageNum, DecodeCallback decodeCallback, float zoom, RectF pageSliceBounds);

    void stopDecoding(Object decodeKey);

    int getEffectivePagesWidth();

    int getEffectivePagesHeight();

    int getPageCount();

    int getPageWidth(int pageIndex);

    int getPageHeight(int pageIndex);
    
    CodecPage getPage(int pageIndex);
    
    void recycle();
    
    

    public interface DecodeCallback
    {
        void decodeComplete(Bitmap bitmap);
    }
}
>>>>>>> fa4ffd90cd3f6a0db797ede37e42becda41540e9
