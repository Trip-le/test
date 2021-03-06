package com.example.app1;

import android.Manifest;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.face.FirebaseVisionFace;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetector;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetectorOptions;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static android.app.Activity.RESULT_OK;
import static com.example.app1.MainActivity.p_email;
import static com.example.app1.MainActivity.p_name;
import static com.example.app1.show_img_adapter.suc;

public class MainGroup extends Fragment {
    private Retrofit retrofit;
    private RetrofitInterface retrofitInterface;
    private String BASE_URL = "http://192.168.219.106:3000";
    private GridLayoutManager GridLayoutManager;
    private GroupAdapter Gadapter;
    private FirebaseVisionFaceDetectorOptions highAccuracyOpts;
    private ArrayList<Uri> uriList2 = new ArrayList<>();//????????? ?????? ????????? ?????????
    Dialog Dinfo;
    Dialog Dname;
    String GName;
    show_img_dialog show_img_dialog;
    CheckTypesTask task;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        //?????? ?????? ?????? ??????
        highAccuracyOpts =
                new FirebaseVisionFaceDetectorOptions.Builder()
                        .setPerformanceMode(FirebaseVisionFaceDetectorOptions.ACCURATE)
                        .setClassificationMode(FirebaseVisionFaceDetectorOptions.ALL_CLASSIFICATIONS)
                        .build();


        Gson gson=new GsonBuilder()
                .setLenient()
                .create();

        retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();

        retrofitInterface = retrofit.create(RetrofitInterface.class);

        setHasOptionsMenu(true);
        View v= inflater.inflate(R.layout.maingroup, container, false);

        Bundle bundle=getArguments();
        GName=bundle.getString("name");
        TextView Gname=(TextView)v.findViewById(R.id.Gname);
        Gname.setText(GName);


        Dinfo=new Dialog(container.getContext());
        Dinfo.requestWindowFeature(Window.FEATURE_NO_TITLE);
        Dinfo.setContentView(R.layout.grouppop);

        ImageView info=(ImageView)v.findViewById(R.id.info);
        info.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showinfo();
            }
        });

        Dname=new Dialog(container.getContext());
        Dname.requestWindowFeature(Window.FEATURE_NO_TITLE);
        Dname.setContentView(R.layout.groupname);

        Gname.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showinfo2();
            }
        });

        ImageButton back = (ImageButton) v.findViewById(R.id.back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((MainActivity)getActivity()).addGroup2();
            }
        });

        FloatingActionButton down = (FloatingActionButton) v.findViewById(R.id.down);
        down.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast myToast = Toast.makeText(getActivity().getApplicationContext(),"????????????", Toast.LENGTH_SHORT);
                myToast.show();
            }
        });

        FloatingActionButton up = (FloatingActionButton) v.findViewById(R.id.up);
        up.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast myToast = Toast.makeText(getActivity().getApplicationContext(),"?????????", Toast.LENGTH_SHORT);
                myToast.show();
                //????????????
                checkSelfPermission();

                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Audio.Media.EXTERNAL_CONTENT_URI);
                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                intent.setType("image/*");
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), 101);

            }
        });

        RecyclerView recyclerView = v.findViewById(R.id.grecyclerView);
        GridLayoutManager = new GridLayoutManager(getActivity().getApplicationContext(),2);
        recyclerView.setLayoutManager(GridLayoutManager);
        Gadapter= new GroupAdapter();
        GroupAdapter.items.clear();
        GroupAdapter.items.add(new folder("name1","","","",""));
        GroupAdapter.items.add(new folder("name2","","","",""));
        GroupAdapter.items.add(new folder("name3","","","",""));
        recyclerView.setAdapter(Gadapter);
        return v;
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }


    public void showinfo(){
        Dinfo.show(); // ??????????????? ?????????
        TextView tv1=Dinfo.findViewById(R.id.groupn);
        tv1.setText("?????????: "+GName);
        TextView tv2=Dinfo.findViewById(R.id.groupl);
        TextView tv3=Dinfo.findViewById(R.id.groupd);
        TextView tv4=Dinfo.findViewById(R.id.groupf);

        HashMap<String, String> map = new HashMap<>();

        map.put("email", MainActivity.p_email);
        map.put("groupname", GName);

        Call<groupResult> call = retrofitInterface.showGroup(map);

        call.enqueue(new Callback<groupResult>() {
            @Override
            public void onResponse(Call<groupResult> call, Response<groupResult> response) {
                Toast.makeText(getContext(),String.valueOf(response.code()),Toast.LENGTH_SHORT).show();
                if (response.code() == 200) {

                    groupResult result = response.body();

                    tv2.setText("??????: "+result.getGroup_place());
                    tv3.setText("??????: "+result.getGroup_date());
                    tv4.setText("?????? ?????????: "+result.getGroup_schoolinfo());

                }
                else if(response.code() == 400){
                    Toast.makeText(getView().getContext(), "???????????? ??????", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<groupResult> call, Throwable t) {
                Toast.makeText(getView().getContext(), t.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });



        Dinfo.findViewById(R.id.ok).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Dinfo.dismiss();
            }
        });
    }



    public void showinfo2(){
        Dname.show(); // ??????????????? ?????????
        TextView names=Dname.findViewById(R.id.namearr);
        names.setText(names.getText()+" "+"a b c d");

        Dname.findViewById(R.id.ok2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Dname.dismiss();
            }
        });
    }


    private ArrayList<Bitmap> fileList = new ArrayList<>();
    private ArrayList<Uri> uriList = new ArrayList<>();

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) { // ?????????
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 101) {
            if (resultCode == RESULT_OK) {
                ClipData clipData = data.getClipData();

                for (int i = 0; i < clipData.getItemCount(); i++) {
                    Uri tempUri;
                    tempUri = clipData.getItemAt(i).getUri();
                    uriList.add(tempUri);

                    ContentResolver resolver = getActivity().getContentResolver();

                    InputStream instream = null;
                    try {
                        instream = resolver.openInputStream(tempUri);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                    Bitmap imgBitmap = BitmapFactory.decodeStream(instream);
                    try {
                        instream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    fileList.add(imgBitmap);
                }


                task = new CheckTypesTask();
                task.execute();


/*
                try {
                    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                    imgBitmap.compress(Bitmap.CompressFormat.JPEG, 20, byteArrayOutputStream);
                    RequestBody requestBody = RequestBody.create(MediaType.parse("image/jpg"), byteArrayOutputStream.toByteArray());
                    MultipartBody.Part uploadFile = MultipartBody.Part.createFormData("postImg", p_name+".jpg" ,requestBody);
                    //imageView.setImageBitmap(imgBitmap);    // ????????? ????????? ??????????????? ???
                    instream.close();   // ????????? ????????????
                    //saveBitmapToJpeg(imgBitmap);    // ?????? ???????????? ??????

                    Call<ImageResult> call = retrofitInterface.Image(p_email, uploadFile);



                    call.enqueue(new Callback<ImageResult>() {
                        @Override
                        public void onResponse(Call<ImageResult> call, Response<ImageResult> response) {
                            if (response.code() == 200) {
                                Toast.makeText(getContext(), "?????? ???????????? ??????", Toast.LENGTH_SHORT).show();
                            }
                            else if(response.code() == 404){
                                Toast.makeText(getContext(), "404 ??????", Toast.LENGTH_LONG).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<ImageResult> call, Throwable t) {
                            Toast.makeText(getContext(), t.getMessage(),
                                    Toast.LENGTH_LONG).show();
                        }
                    });

                } catch (Exception e) {
                    Toast.makeText(getContext(), "?????? ???????????? ??????", Toast.LENGTH_SHORT).show();
                }
            */
            }
        }
    }

    private int flag=0;
    private class CheckTypesTask extends AsyncTask<Void, Void, Void> {

        ProgressDialog asyncDialog = new ProgressDialog(getContext());

        @Override
        public void onPreExecute() {
            asyncDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            asyncDialog.setMessage("???????????????????????????..");

            //show dialog
            asyncDialog.show();
            super.onPreExecute();
        }

        @Override
        public Void doInBackground(Void... voids) { //????????? ?????? ??????
            for(int i=0; i<fileList.size();i++){
                FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(fileList.get(i));
                FirebaseVisionFaceDetector detector = FirebaseVision.getInstance()
                        .getVisionFaceDetector(highAccuracyOpts);

                int finalI = i;//?????? ????????? ??????

                Task<List<FirebaseVisionFace>> result =
                        detector.detectInImage(image)
                                .addOnSuccessListener(
                                        new OnSuccessListener<List<FirebaseVisionFace>>() {
                                            @Override
                                            public void onSuccess(List<FirebaseVisionFace> faces) {
                                                for (FirebaseVisionFace face : faces) {
                                                    if (face.getSmilingProbability() != FirebaseVisionFace.UNCOMPUTED_PROBABILITY) {
                                                        float smileProb = face.getSmilingProbability();
                                                        ((MainActivity)getActivity()).getlog(Float.toString(smileProb));
                                                        if(smileProb>0.5){
                                                            uriList2.add(uriList.get(finalI));
                                                        }
                                                        if(finalI==fileList.size()-1){
                                                            flag=1;
                                                            ((MainActivity)getActivity()).getlog(Integer.toString(flag));
                                                        }
                                                    }
                                                }
                                            }
                                        })
                                .addOnFailureListener(
                                        new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                // Task failed with an exception
                                                // ...
                                            }
                                        });

            }


            try {
                while(flag!=1){
                    Thread.sleep(500);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        public void onPostExecute(Void aVoid) {//????????? max??? ??????
            asyncDialog.dismiss();
            super.onPostExecute(aVoid);

            show_img_dialog=new show_img_dialog(getContext(), (MainActivity) getActivity(), uriList2, new DialogClickListener() {
                @Override
                public void onPositiveClick() {
                    Toast.makeText(getContext(),"????????? ??????",Toast.LENGTH_SHORT).show();

                    try{

                    ArrayList<MultipartBody.Part> files = new ArrayList<>();
                    HashMap<String, RequestBody> map = new HashMap<>();


                    for (int i = 0; i < uriList2.size(); ++i) {
                        // Uri ????????? ??????????????? ????????? RequestBody ?????? ??????
                        RequestBody fileBody = RequestBody.create(MediaType.parse("image/*"), String.valueOf(uriList2.get(i)));
                        // ?????? ?????? ??????
                        String fileName = GName + "_" + p_name + "_" + p_email + i + ".jpg";
                        // RequestBody??? Multipart.Part ?????? ??????

                        MultipartBody.Part filePart = MultipartBody.Part.createFormData("photo", fileName, fileBody);
                        Log.i("fileName", filePart.toString());
                        // ??????
                        files.add(filePart);
                    }

                    RequestBody email = RequestBody.create(MediaType.parse("text/plain"), p_email);
                    map.put("email", email);

                    String token = p_email;
                    Call<Void> call = retrofitInterface.EmotionImage(token, files, map);

                    call.enqueue(new Callback<Void>() {
                        @Override
                        public void onResponse(Call<Void> call, Response<Void> response) {
                            if (response.code() == 200) {
                                Toast.makeText(getContext(), "?????? ???????????? ??????", Toast.LENGTH_SHORT).show();
                            }
                            else if(response.code() == 404){
                                Toast.makeText(getContext(), "404 ??????", Toast.LENGTH_LONG).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<Void> call, Throwable t) {
                            Toast.makeText(getContext(), t.getMessage(),
                                    Toast.LENGTH_LONG).show();
                        }
                    });
                } catch (Exception e) {
                    Toast.makeText(getContext(), "?????? ???????????? ??????", Toast.LENGTH_SHORT).show();
                }

                }

                @Override
                public void onNegativeClick() {
                    Toast.makeText(getContext(),"???????????? ?????????????????????.",Toast.LENGTH_SHORT).show();
                }
            });
            show_img_dialog.setCanceledOnTouchOutside(false);//??????????????? ?????? ????????? ??????
            show_img_dialog.setCancelable(true);//???????????? ???????????? ??????
            show_img_dialog.show();
        }
    }

    public void checkSelfPermission() {
        String temp = "";
        //?????? ?????? ?????? ??????
        if (ContextCompat.checkSelfPermission(getView().getContext(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            temp += Manifest.permission.READ_EXTERNAL_STORAGE + " ";
        }

        if (ContextCompat.checkSelfPermission(getView().getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            temp += Manifest.permission.WRITE_EXTERNAL_STORAGE + " ";
        }

        if (TextUtils.isEmpty(temp) == false) { // ?????? ??????
            ActivityCompat.requestPermissions((MainActivity) getActivity(), temp.trim().split(" "), 1);
        } else { // ?????? ?????? ??????
            Toast.makeText(getActivity().getApplicationContext(), "????????? ?????? ??????", Toast.LENGTH_SHORT).show();
        }
    }

/*
    public void saveBitmapToJpeg(Bitmap bitmap) {   // ????????? ????????? ?????? ???????????? ??????
        File tempFile = new File(getCacheDir(), imgName);    // ?????? ????????? ?????? ??????
        try {
            tempFile.createNewFile();   // ???????????? ??? ????????? ????????????
            FileOutputStream out = new FileOutputStream(tempFile);  // ????????? ??? ??? ?????? ???????????? ????????????
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);   // compress ????????? ????????? ???????????? ???????????? ????????????
            out.close();    // ????????? ????????????
            Toast.makeText(getContext(), "?????? ?????? ??????", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(getContext(), "?????? ?????? ??????", Toast.LENGTH_SHORT).show();
        }
    }*/
}


