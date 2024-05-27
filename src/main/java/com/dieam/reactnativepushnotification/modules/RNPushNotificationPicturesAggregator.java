package com.dieam.reactnativepushnotification.modules;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.facebook.common.executors.CallerThreadExecutor;
import com.facebook.common.references.CloseableReference;
import com.facebook.datasource.DataSource;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.imagepipeline.common.Priority;
import com.facebook.imagepipeline.datasource.BaseBitmapDataSubscriber;
import com.facebook.imagepipeline.image.CloseableImage;
import com.facebook.imagepipeline.request.ImageRequest;
import com.facebook.imagepipeline.request.ImageRequestBuilder;

import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import java.util.concurrent.atomic.AtomicInteger;

import static com.dieam.reactnativepushnotification.modules.RNPushNotification.LOG_TAG;

public class RNPushNotificationPicturesAggregator {
  interface Callback {
    public void call(Bitmap thumb,Bitmap linkBitmap,Bitmap largeIconImage, Bitmap bigPictureImage, Bitmap bigLargeIconImage);
  }

  private AtomicInteger count = new AtomicInteger(0);

  private Bitmap largeIconImage;
  private Bitmap thumb;
  private Bitmap linkBitmap;
  private Bitmap bigPictureImage;
  private Bitmap bigLargeIconImage;
  private Callback callback;

  public RNPushNotificationPicturesAggregator(Callback callback) {
    this.callback = callback;
  }


  public void setBigPictureUrl(Context context, String url) {
    if(null == url) {
      this.setBigPicture(null);
      return;
    }

    Uri uri = null;

    try {
      uri = Uri.parse(url);
    } catch(Exception ex) {
      Log.e(LOG_TAG, "Failed to parse bigPictureUrl", ex);
      this.setBigPicture(null);
      return;
    }

    final RNPushNotificationPicturesAggregator aggregator = this;

    this.downloadRequest(context, uri, new BaseBitmapDataSubscriber() {
      @Override
      public void onNewResultImpl(@Nullable Bitmap bitmap) {
        aggregator.setBigPicture(bitmap);
      }

      @Override
      public void onFailureImpl(DataSource dataSource) {
        aggregator.setBigPicture(null);
      }
    });
  }

  public void setBigPicture(Bitmap bitmap) {
    this.bigPictureImage = getRoundedBitmap(bitmap, commonRadius);
    this.finished();
  }

  public void setLargeIcon(Bitmap bitmap) {
    this.largeIconImage = getRoundedBitmap(bitmap, commonRadius);
    this.finished();
  }
  public void setThumb(Bitmap bitmap) {
    this.thumb = getCircularBitmap(bitmap);
    this.finished();
  }
  public void setLinkBitmap(Bitmap bitmap) {
    this.linkBitmap = getRoundedBitmap(bitmap, commonRadius);
    this.finished();
  }

  public void setBigLargeIcon(Bitmap bitmap) {
    this.bigLargeIconImage = getRoundedBitmap(bitmap, commonRadius);
    this.finished();
  }


  public void setLargeIconUrl(Context context, String url) {
    if(null == url) {
      this.setLargeIcon(null);
      return;
    }

    Uri uri = null;

    try {
      uri = Uri.parse(url);
    } catch(Exception ex) {
      Log.e(LOG_TAG, "Failed to parse largeIconUrl", ex);
      this.setLargeIcon(null);
      return;
    }

    final RNPushNotificationPicturesAggregator aggregator = this;

    this.downloadRequest(context, uri, new BaseBitmapDataSubscriber() {
      @Override
      public void onNewResultImpl(@Nullable Bitmap bitmap) {
        aggregator.setLargeIcon(bitmap);
      }

      @Override
      public void onFailureImpl(DataSource dataSource) {
        aggregator.setLargeIcon(null);
      }
    });
  }
  public void setThumbUrl(Context context, String url) {
    if(null == url) {
      this.setThumb(null);
      return;
    }

    Uri uri = null;

    try {
      uri = Uri.parse(url);
    } catch(Exception ex) {
      Log.e(LOG_TAG, "Failed to parse largeIconUrl", ex);
      this.setThumb(null);
      return;
    }

    final RNPushNotificationPicturesAggregator aggregator = this;

    this.downloadRequest(context, uri, new BaseBitmapDataSubscriber() {
      @Override
      public void onNewResultImpl(@Nullable Bitmap bitmap) {
        aggregator.setThumb(bitmap);
      }

      @Override
      public void onFailureImpl(DataSource dataSource) {
        aggregator.setThumb(null);
      }
    });
  }
  public static Bitmap getCircularBitmap(Bitmap bitmap) {
    int width = bitmap.getWidth();
    int height = bitmap.getHeight();
    int diameter = Math.min(width, height);
    int radius = diameter / 2;

    Bitmap output = Bitmap.createBitmap(diameter, diameter, Bitmap.Config.ARGB_8888);
    Canvas canvas = new Canvas(output);

    final Paint paint = new Paint();
    final RectF rectF = new RectF(0, 0, diameter, diameter);
    final Path path = new Path();

    path.addCircle((float) diameter / 2, (float) diameter / 2, (float) diameter / 2, Path.Direction.CCW);

    paint.setAntiAlias(true);
    paint.setColor(0xFF000000);

    canvas.drawARGB(0, 0, 0, 0);
    canvas.drawPath(path, paint);

    paint.setXfermode(new android.graphics.PorterDuffXfermode(android.graphics.PorterDuff.Mode.SRC_IN));
    canvas.drawBitmap(bitmap, null, rectF, paint);

    return output;
  }

  public static Bitmap getRoundedBitmap(Bitmap bitmap, int cornerRadius) {
    if(bitmap == null){
      return null;
    }
    // Create a new bitmap with the same dimensions
    Bitmap roundedBitmap = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);

    // Create a new canvas to draw the rounded bitmap
    Canvas canvas = new Canvas(roundedBitmap);

    // Create a paint object
    Paint paint = new Paint();

    // Create a bitmap shader to draw the original bitmap
    BitmapShader shader = new BitmapShader(bitmap, BitmapShader.TileMode.CLAMP, BitmapShader.TileMode.CLAMP);
    paint.setShader(shader);

    // Create a rect for the rounded bitmap
    RectF rect = new RectF(0, 0, bitmap.getWidth(), bitmap.getHeight());

    // Draw the rounded bitmap
    canvas.drawRoundRect(rect, cornerRadius, cornerRadius, paint);

    return roundedBitmap;
  }
  static int commonRadius = 300;
  static int iconRadius = 300;
  public static Bitmap getMagicalBitmap(Context context,int resId){
    Drawable drawable = ContextCompat.getDrawable(context, resId);
    if(drawable!=null){
      Bitmap bitmap = drawableToBitmap(drawable);
      return getRoundedBitmap(bitmap,iconRadius);
    }
      return null;
  }

  public static Bitmap drawableToBitmap(Drawable drawable) {
    Bitmap bitmap = Bitmap.createBitmap(
            drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888
    );
    Canvas canvas = new Canvas(bitmap);
    drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
    drawable.draw(canvas);
    return bitmap;
  }
  public void setLinkUrl(Context context, String url) {
    if(null == url) {
      this.setLinkBitmap(null);
      return;
    }

    Uri uri = null;

    try {
      uri = Uri.parse(url);
    } catch(Exception ex) {
      Log.e(LOG_TAG, "Failed to parse largeIconUrl", ex);
      this.setLinkBitmap(null);
      return;
    }

    final RNPushNotificationPicturesAggregator aggregator = this;

    this.downloadRequest(context, uri, new BaseBitmapDataSubscriber() {
      @Override
      public void onNewResultImpl(@Nullable Bitmap bitmap) {
        aggregator.setLinkBitmap(bitmap);
      }

      @Override
      public void onFailureImpl(DataSource dataSource) {
        aggregator.setLinkBitmap(null);
      }
    });
  }


  public void setBigLargeIconUrl(Context context, String url) {
    if(null == url) {
      this.setBigLargeIcon(null);
      return;
    }

    Uri uri = null;

    try {
      uri = Uri.parse(url);
    } catch(Exception ex) {
      Log.e(LOG_TAG, "Failed to parse bigLargeIconUrl", ex);
      this.setBigLargeIcon(null);
      return;
    }

    final RNPushNotificationPicturesAggregator aggregator = this;

    this.downloadRequest(context, uri, new BaseBitmapDataSubscriber() {
      @Override
      public void onNewResultImpl(@Nullable Bitmap bitmap) {
        aggregator.setBigLargeIcon(bitmap);
      }

      @Override
      public void onFailureImpl(DataSource dataSource) {
        aggregator.setBigLargeIcon(null);
      }
    });
  }

  private void downloadRequest(Context context, Uri uri, BaseBitmapDataSubscriber subscriber) {
    ImageRequest imageRequest = ImageRequestBuilder
            .newBuilderWithSource(uri)
            .setRequestPriority(Priority.HIGH)
            .setLowestPermittedRequestLevel(ImageRequest.RequestLevel.FULL_FETCH)
            .build();

    if(Fresco.getDraweeControllerBuilderSupplier() == null) {
//    if(!Fresco.hasBeenInitialized()) {
      Fresco.initialize(context);
    }

    DataSource<CloseableReference<CloseableImage>> dataSource = Fresco.getImagePipeline().fetchDecodedImage(imageRequest, context);

    dataSource.subscribe(subscriber, CallerThreadExecutor.getInstance());
  }

  private void finished() {
    synchronized(this.count) {
      int val = this.count.incrementAndGet();

      if(val >= 5 && this.callback != null) {
        this.callback.call(this.thumb,this.linkBitmap,this.largeIconImage, this.bigPictureImage, this.bigLargeIconImage);
      }
    }
  }
}