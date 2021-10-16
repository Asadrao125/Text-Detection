package com.appsxone.textdetection;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.Manifest;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.TextRecognizerOptions;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import java.io.FileNotFoundException;
import java.io.InputStream;

public class MainActivity extends AppCompatActivity {
    DrawerLayout dl;
    ActionBarDrawerToggle t;
    ImageView image, icCross;
    RelativeLayout imageLayout;
    RelativeLayout contentFrame;
    TextView tvResult, tvVersion;
    NavigationView navigationView;
    RelativeLayout about, share, rate;
    LinearLayout cameraLayout, galleryLayout, btnLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setTitle("Home");
        image = findViewById(R.id.image);
        cameraLayout = findViewById(R.id.cameraLayout);
        tvResult = findViewById(R.id.tvResult);
        galleryLayout = findViewById(R.id.galleryLayout);
        btnLayout = findViewById(R.id.btnLayout);
        icCross = findViewById(R.id.icCross);
        imageLayout = findViewById(R.id.imageLayout);
        dl = (DrawerLayout) findViewById(R.id.activity_main);
        contentFrame = findViewById(R.id.contentFrame);
        navigationView = (NavigationView) findViewById(R.id.nv);
        about = findViewById(R.id.about);
        share = findViewById(R.id.share);
        rate = findViewById(R.id.rate);
        tvVersion = findViewById(R.id.tvVersion);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        t = new ActionBarDrawerToggle(this, dl, R.string.Open, R.string.Close) {
            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
                super.onDrawerSlide(drawerView, slideOffset);
                contentFrame.setTranslationX(slideOffset * drawerView.getWidth());
                dl.bringChildToFront(drawerView);
                dl.requestLayout();
            }
        };

        dl.addDrawerListener(t);
        t.syncState();
        t.getDrawerArrowDrawable().setColor(getResources().getColor(R.color.white));

        tvVersion.setText("Version: " + BuildConfig.VERSION_NAME);

        about.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                aboutDialog();
                dl.closeDrawers();
            }
        });

        share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                shareIntent();
                dl.closeDrawers();
            }
        });

        rate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + getPackageName())));
                dl.closeDrawers();
            }
        });

        cameraLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkPermission();
            }
        });

        icCross.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnLayout.setVisibility(View.VISIBLE);
                imageLayout.setVisibility(View.GONE);
            }
        });

        galleryLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
                photoPickerIntent.setType("image/*");
                startActivityForResult(photoPickerIntent, 124);
            }
        });
    }

    private void aboutDialog() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_about, null);
        dialogBuilder.setView(dialogView);
        AlertDialog alertDialog = dialogBuilder.create();
        alertDialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
        ImageView imgCross = dialogView.findViewById(R.id.imgClose);
        Button btnCLose = dialogView.findViewById(R.id.btnCLose);

        imgCross.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertDialog.dismiss();
            }
        });

        btnCLose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertDialog.dismiss();
            }
        });

        alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        alertDialog.show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (t.onOptionsItemSelected(item))
            return true;
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (!dl.isDrawerOpen(GravityCompat.START)) {
            super.onBackPressed();
        } else {
            dl.closeDrawers();
        }
    }

    public void shareIntent() {
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, "Let me recommed you Text Detection" +
                "\nhttps://play.google.com/store/apps/details?id=" + BuildConfig.APPLICATION_ID);
        sendIntent.setType("text/plain");
        startActivity(sendIntent);
    }

    private void checkPermission() {
        Dexter.withContext(this)
                .withPermission(Manifest.permission.CAMERA)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse response) {
                        captureImage();
                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse response) {

                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {
                        token.continuePermissionRequest();
                    }
                }).check();
    }

    private void detectText(Bitmap bitmap) {
        tvResult.setText("");
        imageLayout.setVisibility(View.VISIBLE);
        btnLayout.setVisibility(View.GONE);
        InputImage inputImage = InputImage.fromBitmap(bitmap, 0);
        TextRecognizer textRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);
        Task<Text> result = textRecognizer.process(inputImage).addOnSuccessListener(new OnSuccessListener<Text>() {
            @Override
            public void onSuccess(@NonNull Text text) {
                StringBuilder stringBuilder = new StringBuilder();
                for (Text.TextBlock block : text.getTextBlocks()) {
                    String blockText = block.getText();
                    Point[] blockCornerPoint = block.getCornerPoints();
                    Rect bloclFrame = block.getBoundingBox();
                    for (Text.Line line : block.getLines()) {
                        String lineText = line.getText();
                        Point[] lineCornerPoint = line.getCornerPoints();
                        Rect lineRect = line.getBoundingBox();
                        for (Text.Element element : line.getElements()) {
                            String elementText = element.getText();
                            stringBuilder.append(elementText);
                        }
                    }
                    tvResult.append(blockText + "\n");
                }
                if (text.getText().isEmpty()) {
                    tvResult.setText("No text detected");
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                tvResult.setText("Failed to detect text");
            }
        });
    }

    private void captureImage() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, 123);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 123 && data != null) {
            Bitmap photo = (Bitmap) data.getExtras().get("data");
            image.setImageBitmap(photo);
            detectText(photo);
        } else if (resultCode == RESULT_OK && data != null && requestCode == 124) {
            try {
                final Uri imageUri = data.getData();
                final InputStream imageStream = getContentResolver().openInputStream(imageUri);
                final Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);
                image.setImageBitmap(selectedImage);
                detectText(selectedImage);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
    }
}