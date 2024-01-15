package com.sajinvargheese.myapplication;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class MainActivity extends AppCompatActivity {
    ImageView imagePreview;
    Button submitBtn, addPhoto;
    Dialog imageChooser;
    final Context context = this;
    private static final int MY_CAMERA_PERMISSION_CODE = 100;
    Uri filePath = null;
    ProgressBar  loading;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imagePreview = findViewById(R.id.imagePreview);
        submitBtn = findViewById(R.id.sumBitBtn);
        addPhoto = findViewById(R.id.addPhoto);
        loading = findViewById(R.id.loading);

        submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                uploadToServer(filePath.getPath());
            }
        });
        addPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createChooser();
            }
        });

    }

    private void createChooser() {
        loading.setVisibility(View.VISIBLE);
        imageChooser = new Dialog(context);
        imageChooser.setContentView(R.layout.camera_chooser);
        Window window = imageChooser.getWindow();
        imageChooser.setCancelable(false);
        imageChooser.setCanceledOnTouchOutside(false);
        imageChooser.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
        imageChooser.show();
    }

    public void takeNormalPhoto(View view) {

        imageChooser.dismiss();
        if (checkSelfPermission(android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.CAMERA}, MY_CAMERA_PERMISSION_CODE);
        } else {
            Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
            imageOutput.launch(cameraIntent);
        }
    }

    public void UploadImage(View view) {
        imageChooser.dismiss();
        Intent i = new Intent();
        i.setType("image/*");
        imageOutput.launch(i);

    }
    ActivityResultLauncher<Intent> imageOutput = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        try {
                            if (result.getData().getData() != null) {
                                loading.setVisibility(View.GONE);
                                 Uri imageUri = result.getData().getData();
                                  imagePreview.setImageURI(imageUri);
                                filePath =imageUri;
                            }else if(data.getExtras().get("data")!= null){
                                loading.setVisibility(View.GONE);
                                Bitmap photo = (Bitmap) data.getExtras().get("data");
                                File outputdir = getApplicationContext().getCacheDir();
                                if (!outputdir.exists()) {
                                    outputdir.mkdirs();
                                }
                                OutputStream outStream = null;
                                File outputfile = File.createTempFile("temp", ".jpg", outputdir);
                                outStream = new FileOutputStream(outputfile);
                                photo.compress(Bitmap.CompressFormat.PNG, 85, outStream);
                                outStream.close();
                                imagePreview.setImageURI(Uri.fromFile(outputfile));
                                filePath =Uri.fromFile(outputfile);

                            }else{
                                loading.setVisibility(View.GONE);
                            }

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            });

    private void uploadToServer(String filePath) {
        loading.setVisibility(View.VISIBLE);
        Retrofit retrofit = BaseUrl.getRetrofitClient(this);
        EndAPIs uploadAPIs = retrofit.create(EndAPIs.class);
        File file = new File(filePath);

        RequestBody reReqBody = RequestBody.create(MediaType.parse("text/plain" + "; charset=utf-8"), file);
        MultipartBody.Part part = MultipartBody.Part.createFormData("file", reReqBody.toString());
        Call call = uploadAPIs.uploadImage(part);
        call.enqueue(new Callback() {
            @Override
            public void onResponse(Call call, Response response) {
                loading.setVisibility(View.GONE);
                Toast.makeText(context,"Upload Success",Toast.LENGTH_SHORT).show();

            }
            @Override
            public void onFailure(Call call, Throwable t) {
                loading.setVisibility(View.GONE);
                Toast.makeText(context,"Upload Failed",Toast.LENGTH_SHORT).show();
            }
        });







    }


}
