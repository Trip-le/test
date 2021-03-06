package com.example.app1;

import android.app.Activity;
import android.content.ClipData;
import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static com.example.app1.MainActivity.p_email;
import static com.example.app1.MainActivity.p_name;

public class JoinActivity extends AppCompatActivity {
    private Retrofit retrofit;
    private RetrofitInterface retrofitInterface;
    private String BASE_URL = "http://13.209.5.100:3000";
    private EditText email;
    private EditText pass;
    private EditText passCheck;
    private EditText name;
    private RadioGroup gen;
    private Button join;
    private ImageView pro;
    private RadioButton teacher;
    private RadioButton parent;
    private LinearLayout email_check_Li;
    private EditText email_check;
    private Button email_check_Button;
    private String email_string;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.join);

        retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        retrofitInterface = retrofit.create(RetrofitInterface.class);

        email = findViewById(R.id.email);
        pass = findViewById(R.id.pass);
        passCheck = findViewById(R.id.passCheck);
        name = findViewById(R.id.name);
        gen = findViewById(R.id.gen);


        pro=findViewById(R.id.profile);
        pro.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast myToast = Toast.makeText(getApplicationContext(),"????????? ?????? ??????", Toast.LENGTH_SHORT);
                myToast.show();

                Intent intent=new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(intent, 0);
            }
        });

        Button auth=findViewById(R.id.auth);
        email_check_Li=findViewById(R.id.email_check_Li);
        email_check=findViewById(R.id.email_check);
        email_check_Button=findViewById(R.id.email_check_button);

        auth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // ???????????? ?????????
                email_check_Li.setVisibility(View.VISIBLE);
                email_string = email.getText().toString();
                HashMap<String, String> map = new HashMap<>();
                map.put("email", email_string);


                Call<CheckResult> call = retrofitInterface.executeCheck(map);


                call.enqueue(new Callback<CheckResult>() {
                    @Override
                    public void onResponse(Call<CheckResult> call, Response<CheckResult> response) {
                        if (response.code() == 200) {
                            CheckResult result = response.body();

                            email_check_Button.setOnClickListener(new View.OnClickListener(){
                                public void onClick(View view){
                                    if((result.getChecking()).equals(email_check.getText().toString())) {
                                        Toast.makeText(JoinActivity.this, "????????? ?????????????????????.", Toast.LENGTH_LONG).show();
                                        email_check_Button.setText("?????? ??????");
                                    }
                                    else{
                                        Toast.makeText(JoinActivity.this, "??????????????? ???????????? ????????????.", Toast.LENGTH_LONG).show();
                                    }

                                }
                            });
                        }
                        else if(response.code() == 404){
                            Toast.makeText(JoinActivity.this, "404 ??????", Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<CheckResult> call, Throwable t) {
                        Toast.makeText(JoinActivity.this, t.getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                });


            }
        });

        Button putimg=findViewById(R.id.putimg);
        putimg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toGalery();
            }
        });

        teacher = findViewById(R.id.teacher);
        parent = findViewById(R.id.parent);
        join = findViewById(R.id.join);
        join.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!(pass.getText().toString().equals(passCheck.getText().toString()))){
                    Toast.makeText(JoinActivity.this, "??????????????? ???????????? ????????????.", Toast.LENGTH_LONG).show();
                }
                else {
                    HashMap<String, String> map = new HashMap<>();
                    map.put("email", email.getText().toString());
                    String part[] = email.getText().toString().split("@");
                    map.put("password", pass.getText().toString());
                    map.put("name", name.getText().toString()+"_"+part[0]);

                    if(teacher.isChecked()){
                        map.put("job", "?????????");
                    }
                    else{
                        map.put("job", "?????????");
                    }


                    Call<Void> call = retrofitInterface.executeSignup(map);
                    if(email_check_Button.getText().toString()=="?????? ??????"){
                        call.enqueue(new Callback<Void>() {
                            @Override
                            public void onResponse(Call<Void> call, Response<Void> response) {
                                if (response.code() == 200) {
                                    Toast.makeText(JoinActivity.this,
                                            "??????????????? ?????????????????????.", Toast.LENGTH_LONG).show();
                                    Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                                    startActivity(intent);

                                } else if (response.code() == 400) {
                                    Toast.makeText(JoinActivity.this, "?????? ????????? ???????????????.",
                                            Toast.LENGTH_LONG).show();
                                }
                            }

                            @Override
                            public void onFailure(Call<Void> call, Throwable t) {
                                Toast.makeText(JoinActivity.this, t.getMessage(),
                                        Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                    else{
                        Toast.makeText(JoinActivity.this,
                                "????????? ????????? ???????????? ???????????????.", Toast.LENGTH_LONG).show();
                    }
                }
            }
        });

    }

    private void toGalery(){
        Toast myToast = Toast.makeText(getApplicationContext(),"?????? ??????", Toast.LENGTH_SHORT);
        myToast.show();
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Audio.Media.EXTERNAL_CONTENT_URI);
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        intent.setType("image/*");
        startActivityForResult(intent, 101);
    }

    //????????? ?????????
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        hideKeyboard();
        return super.dispatchTouchEvent(ev);
    }

    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
        //Find the currently focused view, so we can grab the correct window token from it.
        View view = getCurrentFocus();
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = new View(this);
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }




    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 101) {
            if (resultCode == RESULT_OK) {
                //ClipData clipData = data.getClipData();
                Uri fileUri = data.getData();
                ClipData clipData = data.getClipData();
                ArrayList<Uri> filePathList = new ArrayList<>();
                /*
                while(clipData.getItemCount()<6) {
                    Toast myToast = Toast.makeText(getApplicationContext(),"5??? ?????? ??????????????????", Toast.LENGTH_SHORT);
                    myToast.show();
                    toGalery();
                }*/
                for (int i = 0; i < clipData.getItemCount(); i++) {
                    Uri tempUri;
                    tempUri = clipData.getItemAt(i).getUri();
                    Log.i("temp: ", i + " " + tempUri.toString());
                    filePathList.add(tempUri);
                }
                ContentResolver resolver = this.getContentResolver();
                try {
                    /*
                    InputStream instream = resolver.openInputStream(fileUri);
                    Bitmap imgBitmap = BitmapFactory.decodeStream(instream);
                    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                    imgBitmap.compress(Bitmap.CompressFormat.JPEG, 20, byteArrayOutputStream);
                    RequestBody requestBody = RequestBody.create(MediaType.parse("image/*"), byteArrayOutputStream.toByteArray());
                    MultipartBody.Part uploadFile = MultipartBody.Part.createFormData("postImg", p_name+".jpg" ,requestBody);
                    //imageView.setImageBitmap(imgBitmap);    // ????????? ????????? ??????????????? ???
                    instream.close();   // ????????? ????????????
                    //saveBitmapToJpeg(imgBitmap);    // ?????? ???????????? ??????*/

                    ArrayList<MultipartBody.Part> files = new ArrayList<>();
                    HashMap<String, RequestBody> map = new HashMap<>();


                    // ?????? ???????????? ??????????????? `ArrayList<Uri> filePathList`??? ????????? ?????????...
                    for (int i = 0; i < filePathList.size(); ++i) {
                        // Uri ????????? ??????????????? ????????? RequestBody ?????? ??????
                        RequestBody fileBody = RequestBody.create(MediaType.parse("image/*"), String.valueOf(filePathList.get(i)));
                        // ?????? ?????? ??????
                        String fileName = "photo" + i + ".jpg";
                        // RequestBody??? Multipart.Part ?????? ??????

                        MultipartBody.Part filePart = MultipartBody.Part.createFormData("photo", fileName, fileBody);
                        Log.i("fileName", filePart.toString());
                        // ??????
                        files.add(filePart);
                    }

                    RequestBody email = RequestBody.create(MediaType.parse("text/plain"), email_string);
                    map.put("email", email);

                    String token = email_string;
                    Call<Void> call = retrofitInterface.Image(token, files, map);


                    call.enqueue(new Callback<Void>() {
                        @Override
                        public void onResponse(Call<Void> call, Response<Void> response) {
                            if (response.code() == 200) {
                                Toast.makeText(JoinActivity.this, "?????? ???????????? ??????", Toast.LENGTH_SHORT).show();
                            }
                            else if(response.code() == 404){
                                Toast.makeText(JoinActivity.this, "404 ??????", Toast.LENGTH_LONG).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<Void> call, Throwable t) {
                            Toast.makeText(JoinActivity.this, t.getMessage(),
                                    Toast.LENGTH_LONG).show();
                        }
                    });
                } catch (Exception e) {
                    Toast.makeText(JoinActivity.this, "?????? ???????????? ??????", Toast.LENGTH_SHORT).show();
                }
            }
        }
        else if (requestCode == 0) {
            if (resultCode == RESULT_OK) {
                try {
                    Uri uri=data.getData();
                    Glide.with(getApplicationContext()).load(uri).into(pro);

                } catch (Exception e) {

                }
            } else if (resultCode == RESULT_CANCELED) {
                Toast.makeText(this, "?????? ?????? ??????", Toast.LENGTH_LONG).show();
            }
        }
    }
}

