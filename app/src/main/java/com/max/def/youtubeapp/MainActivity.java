package com.max.def.youtubeapp;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.max.def.youtubeapp.Adapter.VideoAdapter;
import com.max.def.youtubeapp.Model.VideoModel;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity
{
    private String onlineUserId;

    private DatabaseReference databaseReference;

    private StorageReference storageReference;

    private List<VideoModel> videoModelList = new ArrayList<>();

    private VideoAdapter videoAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        onlineUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        databaseReference = FirebaseDatabase.getInstance().getReference().child("Users").child(onlineUserId);

        storageReference = FirebaseStorage.getInstance().getReference().child(onlineUserId);

        AppCompatButton uploadVideo = findViewById(R.id.upload_video);

        uploadVideo.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Intent videoIntent = new Intent(Intent.ACTION_GET_CONTENT);
                videoIntent.setType("video/*");
                startActivityForResult(videoIntent,100);
            }
        });

        RecyclerView videoRecyclerView = findViewById(R.id.video_recyclerview);
        videoRecyclerView.setLayoutManager(new LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false));

        databaseReference.addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                videoModelList.clear();

                for(DataSnapshot snapshot : dataSnapshot.getChildren())
                {
                    VideoModel videoModel = snapshot.getValue(VideoModel.class);

                    videoModelList.add(videoModel);
                }

                videoAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError)
            {
                Toast.makeText(MainActivity.this, databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        videoAdapter = new VideoAdapter(videoModelList,this);

        videoRecyclerView.setAdapter(videoAdapter);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK)
        {
            if (requestCode == 100)
            {
                final Uri videoUri = data.getData();

                if (videoUri != null)
                {
                    MediaMetadataRetriever retriever = new MediaMetadataRetriever();

                    retriever.setDataSource(videoUri.getEncodedPath());

                    Bitmap thumbBitmap = retriever.getFrameAtTime();

                    final Uri thumbUri = getUriFromBitmap(MainActivity.this,thumbBitmap);

                    DatabaseReference userReference = databaseReference.push();

                    final String key = userReference.getKey();

                    final StorageReference videoReference = storageReference.child("Videos").child(key + ".mp4");

                    UploadTask uploadTask = videoReference.putFile(videoUri);

                    uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>()
                    {
                        @Override
                        public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception
                        {
                            if (!task.isSuccessful())
                            {
                                Toast.makeText(MainActivity.this, task.getException().getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                            }

                            return videoReference.getDownloadUrl();
                        }
                    }).addOnCompleteListener(new OnCompleteListener<Uri>()
                    {
                        @Override
                        public void onComplete(@NonNull Task<Uri> task)
                        {
                            if (task.isSuccessful())
                            {
                                final String videoDownloadUrl = task.getResult().toString();

                                final StorageReference imageReference = storageReference.child("Images").child(key + ".jpg");

                                UploadTask imageUploadTask = imageReference.putFile(thumbUri);

                                imageUploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>()
                                {
                                    @Override
                                    public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception
                                    {
                                        if (!task.isSuccessful())
                                        {
                                            Toast.makeText(MainActivity.this, task.getException().getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                        return imageReference.getDownloadUrl();
                                    }
                                }).addOnCompleteListener(new OnCompleteListener<Uri>()
                                {
                                    @Override
                                    public void onComplete(@NonNull Task<Uri> task)
                                    {
                                        if (task.isSuccessful())
                                        {
                                            String imageDownloadUrl = task.getResult().toString();

                                            databaseReference.child(key).child("video_url").setValue(videoDownloadUrl);
                                            databaseReference.child(key).child("thumb_url").setValue(imageDownloadUrl).addOnCompleteListener(new OnCompleteListener<Void>()
                                            {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task)
                                                {
                                                    if (task.isSuccessful())
                                                    {
                                                        Toast.makeText(MainActivity.this, "Video Uploaded Successfully", Toast.LENGTH_SHORT).show();
                                                    }
                                                }
                                            });
                                        }
                                    }
                                });
                            }
                        }
                    });
                }
            }
        }
    }

    private Uri getUriFromBitmap(Context context, Bitmap thumbBitmap)
    {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        thumbBitmap.compress(Bitmap.CompressFormat.PNG,100,outputStream);

        String path = MediaStore.Images.Media.insertImage(context.getContentResolver(),thumbBitmap,"My Image",null);

        return Uri.parse(path);
    }
}
