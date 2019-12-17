package com.panos.oddandroid.activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.transition.AutoTransition;
import androidx.transition.TransitionManager;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.gamesparks.sdk.android.GSAndroidPlatform;
import com.panos.oddandroid.customview.PlayerImageView;
import com.panos.oddandroid.R;
import com.panos.oddandroid.databinding.ActivityProfileBinding;
import com.panos.oddandroid.model.CompleteChallenge;
import com.panos.oddandroid.model.User;
import com.panos.oddandroid.viewmodel.CompleteChallengeViewModel;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.List;

public class ProfileActivity extends AppCompatActivity {

    // PROJECT VIEWS
    private PlayerImageView userImage;

    // ANDROID VIEWS
    private View editButton;
    private View editImagePortrait;
    private View editTextPortrait;
    private ImageView editImageCancelButton;
    private ImageView editImageConfirmButton;

    // VARIABLES
    private Uri pickedImageUri;
    private boolean editOccurred;

    private CompleteChallengeViewModel completeChallengeViewModel;


    /**
     * Resets Edit state if the User decides not to keep his new Image.
     */
    void cancelImageEdit() {

        userImage.loadImage();

        editImageCancelButton.setVisibility(View.INVISIBLE);
        editImageConfirmButton.setVisibility(View.INVISIBLE);
        editImagePortrait.setVisibility(View.VISIBLE);
    }

    /**
     * Upon accepting Image change, store new Image and attempt to upload it.
     * In addition, resets Edit state.
     */
    void confirmImageEdit() {

        if (userImage.storeImage()) {
            editOccurred = true;
            if (GSAndroidPlatform.gs().isAvailable()) {
                userImage.uploadImage();
            } else {
                Toast.makeText(getApplicationContext(),"No Network", Toast.LENGTH_LONG).show();
            }
        }

        editImageCancelButton.setVisibility(View.INVISIBLE);
        editImageConfirmButton.setVisibility(View.INVISIBLE);
        editImagePortrait.setVisibility(View.VISIBLE);
    }

    /**
     * Starts the Activity responsible for picking an Image from Phone's files.
     */
    void startPickImageActivity(){

        editImagePortrait.setVisibility(View.INVISIBLE);

        CropImage.startPickImageActivity(this);
    }

    /**
     * Starts the Activity where the Cropping is taking place.
     */
    void startCropImageActivity(){

        CropImage.activity(pickedImageUri)
                .setGuidelines(CropImageView.Guidelines.ON)
                .setCropShape(CropImageView.CropShape.OVAL)
                .setAspectRatio(1,1)
                .setFixAspectRatio(true)
                .start(this);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityProfileBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_profile);

        binding.setUser(User.getUser());

        completeChallengeViewModel = new ViewModelProvider.AndroidViewModelFactory(getApplication()).create(CompleteChallengeViewModel.class);

        editOccurred = false;

        userImage = findViewById(R.id.image);

        editImagePortrait = findViewById(R.id.editImagePortrait);
        editImagePortrait.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startPickImageActivity();
            }
        });

        editTextPortrait = findViewById(R.id.editTextPortrait);

        editImageCancelButton = findViewById(R.id.imageEditCancelButton);
        editImageCancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cancelImageEdit();
            }
        });

        editImageConfirmButton = findViewById(R.id.imageEditConfirmButton);
        editImageConfirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                confirmImageEdit();
            }
        });

        editButton = findViewById(R.id.editButton);
        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (editImagePortrait.getVisibility() == View.INVISIBLE) {

                    TransitionManager.beginDelayedTransition((ViewGroup) findViewById(R.id.mainLayout), new AutoTransition());

                    findViewById(R.id.portrait).setVisibility(View.INVISIBLE);
                    findViewById(R.id.xpBar).setVisibility(View.INVISIBLE);
                    findViewById(R.id.levelBg).setVisibility(View.INVISIBLE);
                    findViewById(R.id.level).setVisibility(View.INVISIBLE);
                    findViewById(R.id.userNameTextBg).setVisibility(View.INVISIBLE);

                    editImagePortrait.setVisibility(View.VISIBLE);
                    editTextPortrait.setVisibility(View.VISIBLE);
                } else {
                    TransitionManager.beginDelayedTransition((ViewGroup) findViewById(R.id.mainLayout), new AutoTransition());

                    editImagePortrait.setVisibility(View.INVISIBLE);
                    editTextPortrait.setVisibility(View.INVISIBLE);

                    findViewById(R.id.portrait).setVisibility(View.VISIBLE);
                    findViewById(R.id.xpBar).setVisibility(View.VISIBLE);
                    findViewById(R.id.levelBg).setVisibility(View.VISIBLE);
                    findViewById(R.id.level).setVisibility(View.VISIBLE);
                    findViewById(R.id.userNameTextBg).setVisibility(View.VISIBLE);
                }
            }
        });

        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        final ChallengeListAdapter adapter = new ChallengeListAdapter(this);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new GridLayoutManager(this,2, RecyclerView.VERTICAL,false));

        completeChallengeViewModel.getAllChallenges().observe(this, new Observer<List<CompleteChallenge>>() {
            @Override
            public void onChanged(List<CompleteChallenge> challenges) {
                adapter.setChallenges(challenges);
            }
        });

        findViewById(R.id.deleteButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                completeChallengeViewModel.deleteAllChallenges();
            }
        });

        //CompleteChallenge challenge = new CompleteChallenge(String.valueOf(id),0,0,0,0,0,0);
        //completeChallengeViewModel.insert(challenge);
    }

    @SuppressLint("NewApi")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CropImage.PICK_IMAGE_CHOOSER_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            // In case User picked an Image, get the URI
            pickedImageUri = CropImage.getPickImageResultUri(this, data);

            // For API >= 23 we need to check specifically that we have permissions to read external storage.
            if (CropImage.isReadExternalStoragePermissionsRequired(this, pickedImageUri)) {
                // request permissions and handle the result in onRequestPermissionsResult()
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, CropImage.PICK_IMAGE_PERMISSIONS_REQUEST_CODE);
            } else {
                // no permissions required or already granted, can start crop image activity
                startCropImageActivity();
            }
        } else if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            // Get cropped Image Data
            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if (resultCode == RESULT_OK) {
                // Get cropped Image URI
                assert result != null;
                userImage.setImageUri(result.getUri());
                userImage.applyImage();

                editImageCancelButton.setVisibility(View.VISIBLE);
                editImageConfirmButton.setVisibility(View.VISIBLE);

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {

                assert result != null;
                Exception error = result.getError();
                try {
                    throw error;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } else {
            editImagePortrait.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if (requestCode == CropImage.PICK_IMAGE_PERMISSIONS_REQUEST_CODE) {
            if (pickedImageUri != null && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // required permissions granted, start crop image activity
                startCropImageActivity();
            } else {
                Toast.makeText(this,"Cancelling, required permissions are not granted", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void onBackPressed() {
        if (editOccurred) {
            setResult(RESULT_OK);
        }
        super.onBackPressed();
    }
}
