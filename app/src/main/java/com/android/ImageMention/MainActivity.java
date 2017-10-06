package com.android.ImageMention;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import com.android.drawingimage.DrawingView;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_IMAGE_PICK = 2;
    DrawingView drawingView;
    ImageView imageView;
    private Bitmap bmp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        drawingView = (DrawingView) findViewById(R.id.drawing);
        drawingView.setDrawingCacheEnabled(true);
        imageView = (ImageView) findViewById(R.id.iv_attachment);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menuoption, menu);
        return true;
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.edit:
                drawingView.setDrawingButtonClicked(true);
                break;
            case R.id.save:
                bmp = drawingView.getDrawingCache();
                Bundle bundle = drawingView.saveImage(bmp);
                imageView.setImageBitmap(bmp);
                imageView.setVisibility(View.VISIBLE);
                drawingView.setVisibility(View.INVISIBLE);
                Log.e("Content uri", bundle.getString("Content uri"));
                Log.e("Absolutepath of image", bundle.getString("Absolutepath of image"));
                drawingView.setDrawingButtonClicked(false);
                break;
            case R.id.back:
                drawingView.undoLastPath();
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        imageView.setVisibility(View.INVISIBLE);
        drawingView.setVisibility(View.VISIBLE);
        if (requestCode == REQUEST_IMAGE_PICK && resultCode == RESULT_OK) {
            if (data != null) {
                Uri imageFileUri = data.getData();
                try {
                    BitmapFactory.Options bmpFactoryOptions = new BitmapFactory.Options();
                    bmpFactoryOptions.inJustDecodeBounds = false;
                    bmp = BitmapFactory.decodeStream(getContentResolver().openInputStream(
                            imageFileUri), null, bmpFactoryOptions);
                    bmp = drawingView.getResizedBitmap(bmp, 1000);
                    BitmapDrawable bitmapDrawable = drawingView.EditImage(drawingView, bmp);
                    drawingView.setBackground(bitmapDrawable);
                } catch (Exception e) {
                    Log.v("ERROR", e.toString());
                }
            }
        }
    }

    public void getToGallery(View view) {
        if (Build.VERSION.SDK_INT >= 23 && ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    REQUEST_IMAGE_PICK);
        } else
            fetchImageFromGallery();
    }

    private void fetchImageFromGallery() {
        Intent getImageFromGallery = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        getImageFromGallery.setType("image/*");
        String[] mimetypes = {"image/jpeg", "image/png"};
        getImageFromGallery.putExtra(Intent.EXTRA_MIME_TYPES, mimetypes);
        getImageFromGallery.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        getImageFromGallery.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
        startActivityForResult(getImageFromGallery, REQUEST_IMAGE_PICK);
    }
}
