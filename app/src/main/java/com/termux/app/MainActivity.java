package com.termux.app;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Bundle;
import android.app.Activity;
import android.provider.MediaStore;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;
import com.termux.R;

import java.io.IOException;

import static android.content.ContentValues.TAG;

public class MainActivity extends Activity {

    Button openCam;
    Button openGallery;
    Context context;

    static final int REQUEST_IMAGE_CAPTURE = 1;
    private int PICK_IMAGE_REQUEST = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        context = getApplicationContext();

        openCam = (Button) findViewById(R.id.openCam);
        openCam.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent cameraActivity = new Intent(MainActivity.this, CameraActivity.class);
                startActivity(cameraActivity);
            }
        });

        openGallery = (Button) findViewById(R.id.openGallery);
        openGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {

            Uri uri = data.getData();

            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                TextRecognizer textRecognizer = new TextRecognizer.Builder(context).build();

                if (!textRecognizer.isOperational()) {
                    Log.w(TAG, "Detector dependencies are not yet available.");

                    // Check for low storage.  If there is low storage, the native library will not be
                    // downloaded, so detection will not become operational.
                    IntentFilter lowstorageFilter = new IntentFilter(Intent.ACTION_DEVICE_STORAGE_LOW);
                    boolean hasLowStorage = registerReceiver(null, lowstorageFilter) != null;

                    if (hasLowStorage) {
                        Toast.makeText(this, R.string.low_storage_error, Toast.LENGTH_LONG).show();
                        Log.w(TAG, getString(R.string.low_storage_error));
                    }
                }

                Frame imageFrame = new Frame.Builder().setBitmap(bitmap).build();

                SparseArray<TextBlock> textBlocks = textRecognizer.detect(imageFrame);
                String detectedText = null;

                for (int i = 0; i < textBlocks.size(); i++) {
                    TextBlock textBlock = textBlocks.get(textBlocks.keyAt(i));
                    String text = textBlock.getValue();
                    detectedText += text + "\n";
                }

                Intent termuxEditor = new Intent(MainActivity.this,TermuxActivity.class);
                termuxEditor.putExtra("detectedText",detectedText);
                startActivity(termuxEditor);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


}
