package com.android.drawingimage;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.AttributeSet;
import android.util.Pair;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.FrameLayout;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

@SuppressLint("ClickableViewAccessibility")
public class DrawingView extends View implements OnTouchListener {
    private static final float TOUCH_TOLERANCE = 4;
    private Canvas m_Canvas;
    private Path m_Path;
    private Paint m_Paint;
    private ArrayList<Pair<Path, Paint>> paths = new ArrayList<Pair<Path, Paint>>();
    private float mX = 50, mY = 50;
    private boolean isDrawingButtonClicked = false;
    private boolean picDiscription = false;
    private boolean isImageSave = false;

    public DrawingView(Context context, AttributeSet attr) {
        super(context, attr);
        setFocusable(true);
        setFocusableInTouchMode(true);
        this.setOnTouchListener(this);
        onCanvasInitialization();
    }

    public static Uri getImageContentUri(Context context, File imageFile) {
        String filePath = imageFile.getAbsolutePath();
        Cursor cursor = context.getContentResolver().query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                new String[]{MediaStore.Images.Media._ID},
                MediaStore.Images.Media.DATA + "=? ",
                new String[]{filePath}, null);
        if (cursor != null && cursor.moveToFirst()) {
            int id = cursor.getInt(cursor.getColumnIndex(MediaStore.MediaColumns._ID));
            cursor.close();
            return Uri.withAppendedPath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "" + id);
        } else {
            if (imageFile.exists()) {
                ContentValues values = new ContentValues();
                values.put(MediaStore.Images.Media.DATA, filePath);
                return context.getContentResolver().insert(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
            } else {
                return null;
            }
        }
    }

    public static Bitmap getResizedBitmap(Bitmap bitmap, int maxSize) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        float bitmapRatio = (float) width / (float) height;
        if (bitmapRatio > 1) {
            width = maxSize;
            height = (int) (width / bitmapRatio);
        } else {
            height = maxSize;
            width = (int) (height * bitmapRatio);
        }
        bitmap = Bitmap.createScaledBitmap(bitmap, width, height, true);
        return bitmap;
    }

    public void onCanvasInitialization() {

        m_Paint = new Paint();
        m_Paint.setAntiAlias(true);
        m_Paint.setDither(true);
        m_Paint.setColor(Color.parseColor("#000000"));
        m_Paint.setStyle(Paint.Style.STROKE);
        m_Paint.setStrokeJoin(Paint.Join.ROUND);
        m_Paint.setStrokeCap(Paint.Cap.ROUND);
        m_Paint.setStrokeWidth(2);

        m_Canvas = new Canvas();

        m_Path = new Path();
        Paint newPaint = new Paint(m_Paint);
        paths.add(new Pair<Path, Paint>(m_Path, newPaint));

    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
    }

    public boolean onTouch(View arg0, MotionEvent event) {
        float x = event.getX();
        float y = event.getY();

        if (isDrawingButtonClicked) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    touch_start(x, y);
                    invalidate();
                    break;
                case MotionEvent.ACTION_MOVE:
                    touch_move(x, y);
                    invalidate();
                    break;
                case MotionEvent.ACTION_UP:
                    touch_up();
                    invalidate();
                    break;
            }
        }
        return true;
    }

    @Override
    public void onDraw(Canvas canvas) {
        for (Pair<Path, Paint> p : paths) {
            canvas.drawPath(p.first, p.second);
        }
    }

    private void touch_start(float x, float y) {
        if (isDrawingButtonClicked) {
            int color = Color.RED;
            m_Paint.setColor(Color.rgb(Color.red(color), Color.green(color), Color.blue(color)));
            m_Paint.setStrokeWidth(5);
            Paint newPaint = new Paint(m_Paint); // Clones the mPaint object
            paths.add(new Pair<Path, Paint>(m_Path, newPaint));

            m_Path.reset();
            m_Path.moveTo(x, y);
            mX = x;
            mY = y;
        } else {
        }    //reset();	}
    }

    private void touch_move(float x, float y) {
        float dx = Math.abs(x - mX);
        float dy = Math.abs(y - mY);
        if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
            m_Path.quadTo(mX, mY, (x + mX) / 2, (y + mY) / 2);
            mX = x;
            mY = y;
        }
    }

    private void touch_up() {
        m_Path.lineTo(mX, mY);

        // commit the path to our offscreen
        m_Canvas.drawPath(m_Path, m_Paint);

        // kill this so we don't double draw
        m_Path = new Path();
    }

    public Bitmap drawTextOnBitmap(Bitmap bm, String text) {
        float scale = getResources().getDisplayMetrics().density;
        Config bitmapconfig = bm.getConfig();

        if (bitmapconfig == null) {
            bitmapconfig = Config.ARGB_8888;
        }
        bm = bm.copy(bitmapconfig, true);

        Canvas canvas = new Canvas(bm);
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(Color.CYAN);
        paint.setTextSize((int) (14 * scale));
        paint.setShadowLayer(1f, 0f, 1f, Color.WHITE);

        Rect bounds = new Rect();
        paint.getTextBounds(text, 0, text.length(), bounds);

        int x = (bm.getWidth() - bounds.width()) / 2;
        int y = (bm.getHeight() + bounds.height()) / 2;

        canvas.drawText(text, x, y, paint);

        return bm;
    }

    public void reset() {
        paths.clear();
        invalidate();
    }

    public void undoLastPath() {
        if (paths.size() > 0) {
            paths.remove(paths.size() - 1);
            invalidate();
        } else {
            Toast.makeText(getContext(), "Don't have much Drawing to remove", Toast.LENGTH_SHORT).show();
        }
    }

    public void refreshGallery(File file) {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        mediaScanIntent.setData(Uri.fromFile(file));
        getContext().sendBroadcast(mediaScanIntent);
    }

    public String getSystemTimeDate() {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date d = new Date();
        return dateFormat.format(d);
    }

    public boolean isDrawingButtonClicked() {
        return isDrawingButtonClicked;
    }

    public void setDrawingButtonClicked(boolean drawingButtonClicked) {
        isDrawingButtonClicked = drawingButtonClicked;
    }

    public boolean isPicDiscription() {
        return picDiscription;
    }

    public void setPicDiscription(boolean picDiscription) {
        this.picDiscription = picDiscription;
    }

    public Bundle saveImage(Bitmap bm) {
        try {
            File dir = new File(Environment.getExternalStorageDirectory().getAbsolutePath()
                    + File.separator + "MentionImage" + File.separator + "Edited");

            if (!dir.isDirectory()) {
                dir.mkdirs();
            }
            File image_path = new File(dir, File.separator + getSystemTimeDate().replaceAll("[^A-Za-z0-9()\\[\\]]", "") + ".jpg");
            if (!image_path.exists()) {
                image_path.createNewFile();
            }
            FileOutputStream fout = new FileOutputStream(image_path);
            bm.compress(Bitmap.CompressFormat.JPEG, 100, fout);
            fout.flush();
            fout.close();
            Bundle bundle = new Bundle();
            bundle.putString("Content uri", String.valueOf(getImageContentUri(getContext(), image_path)));
            bundle.putString("Absolutepath of image", image_path.toString());

            refreshGallery(image_path);
            isImageSave = true;
            return bundle;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public BitmapDrawable EditImage(DrawingView drawingView, Bitmap bitmap) {
        BitmapDrawable drawable_ob = null;
        try {
            if (drawingView != null) {
                drawingView.reset();
                drawingView.destroyDrawingCache();
                drawable_ob = new BitmapDrawable(getResources(), bitmap);
                if (Build.VERSION.SDK_INT >= 16) {
                    drawingView.setLayoutParams(new FrameLayout.LayoutParams(
                            drawable_ob.getIntrinsicWidth(),
                            drawable_ob.getIntrinsicHeight()));
                    drawingView.setBackground(drawable_ob);

                } else {
                    drawingView.setLayoutParams(new FrameLayout.LayoutParams(
                            drawable_ob.getIntrinsicWidth(),
                            drawable_ob.getIntrinsicHeight()));
                    drawingView.setBackgroundDrawable(drawable_ob);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return drawable_ob;
    }

}