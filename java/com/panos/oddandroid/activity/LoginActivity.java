package com.panos.oddandroid.activity;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.textfield.TextInputEditText;
import androidx.appcompat.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.gamesparks.sdk.GSEventConsumer;
import com.gamesparks.sdk.android.GSAndroidPlatform;
import com.gamesparks.sdk.api.autogen.GSResponseBuilder;
import com.panos.oddandroid.customview.ImportantButton;
import com.panos.oddandroid.customview.PlayerImageView;
import com.panos.oddandroid.R;
import com.panos.oddandroid.model.User;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.UUID;

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends AppCompatActivity {

    // ANDROID VIEWS
    private TextInputEditText nameTextInput;
    private View progressView;
    private View loginFormView;
    private View editImagePortrait;
    private ImageView editImageCancelButton;
    private ImageView editImageConfirmButton;
    private View nameInitialBackground;
    private TextView nameInitialText;

    // PROJECT VIEWS
    private PlayerImageView userImage;

    // VARIABLES
    private Uri pickedImageUri;


    /**
     * Resets Edit state if the User decides not to keep his new Image.
     */
    void cancelImageEdit() {

        //userImage.loadImage();

        editImageCancelButton.setVisibility(View.INVISIBLE);
        editImageConfirmButton.setVisibility(View.INVISIBLE);

        editImagePortrait.setVisibility(View.VISIBLE);
        nameInitialBackground.setVisibility(View.VISIBLE);
        nameInitialText.setVisibility(View.VISIBLE);
    }

    /**
     * Upon accepting Image change, reset Edit state.
     */
    void confirmImageEdit() {

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
        setContentView(R.layout.activity_login);

        final ImportantButton signUpButton = findViewById(R.id.confirmButton);
        signUpButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                signUp();
            }
        });

        userImage = findViewById(R.id.userImage);

        editImagePortrait = findViewById(R.id.editImagePortrait);
        editImagePortrait.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startPickImageActivity();
            }
        });

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

        final TextView previewName = findViewById(R.id.previewNameText);
        nameInitialText = findViewById(R.id.nameInitialText);
        nameInitialBackground = findViewById(R.id.nameInitialBackground);

        nameTextInput = findViewById(R.id.name);
        nameTextInput.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    return true;
                }
                return false;
            }
        });
        nameTextInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {

                if (s.length() >= 1) {
                    previewName.setText(s.toString());
                    nameInitialText.setText(String.valueOf(s.charAt(0)));
                    if (s.length() >= 3) {
                        signUpButton.setEnabled(true);
                    } else {
                        signUpButton.setEnabled(false);
                    }
                } else {
                    previewName.setText(R.string.text_preview_default);
                    nameInitialText.setText("");
                }
            }
        });

        loginFormView = findViewById(R.id.loginForm);
        progressView = findViewById(R.id.loginProgress);
    }

    @TargetApi(Build.VERSION_CODES.M)
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

                nameInitialBackground.setVisibility(View.INVISIBLE);
                nameInitialText.setVisibility(View.INVISIBLE);

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
                Toast.makeText(this, "Error cropping image", Toast.LENGTH_LONG).show();
            }
        } else {
            editImagePortrait.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Callback received when a permissions request has been completed.
     */
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

    /**
     * Attempts to register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void signUp() {
        nameTextInput.setError(null);

        final String name = String.valueOf(nameTextInput.getText());

        // Start Registration only if name provided matches the RegEx
        if (name.matches("[a-zA-Z0-9]+")) {
            showProgress(true);

            // Create Preferences
            final SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences(getString(R.string.shared_preferences),Context.MODE_PRIVATE);
            final SharedPreferences.Editor prefEditor = sharedPreferences.edit();
            // Generate a random ID | Used on every authentication from now on.
            final String userID = sharedPreferences.contains(getString(R.string.pref_user_id)) ?
                    sharedPreferences.getString(getString(R.string.pref_user_id),null) : UUID.randomUUID().toString();

            // When GS is ready attempt to register
            GSAndroidPlatform.gs().setOnAvailable(new GSEventConsumer<Boolean>() {
                @Override
                public void onEvent(Boolean available) {
                    if (available) {
                        GSAndroidPlatform.gs().getRequestBuilder().createDeviceAuthenticationRequest()
                        .setDeviceId(userID)
                        .setDeviceOS(getString(R.string.device_os))
                        .setDisplayName(name)
                        .send(new GSEventConsumer<GSResponseBuilder.AuthenticationResponse>() {
                            @Override
                            public void onEvent(GSResponseBuilder.AuthenticationResponse authenticationResponse) {
                                // On success...
                                if (!authenticationResponse.hasErrors()) {
                                    Toast.makeText(getApplicationContext(),"Authenticated",Toast.LENGTH_LONG).show();
                                    // On first login...
                                    if (User.getUser() == null) {
                                        // Save the User Name and finalize the generated ID | Should the app uninstalls, credentials will be lost.
                                        prefEditor.putString(getString(R.string.pref_display_name), name);
                                        prefEditor.putString(getString(R.string.pref_user_id), userID);
                                        prefEditor.putInt(getString(R.string.pref_level), 1);
                                        prefEditor.putFloat(getString(R.string.pref_xp), 0f);
                                        prefEditor.apply();

                                        // Initialize User Singleton
                                        User.initialize(authenticationResponse.getUserId(),authenticationResponse.getDisplayName(),false,1,0f);

                                        // Save the User Image and attempt to upload it
                                        if (userImage.storeImage()) userImage.uploadImage();

                                        startActivity(new Intent(getApplicationContext(), MainActivity.class));
                                        finish();
                                    } else {
                                        // Re-initialize User Singleton
                                        User.initialize(authenticationResponse.getUserId(),
                                                authenticationResponse.getDisplayName(),
                                                sharedPreferences.getBoolean(getString(R.string.pref_using_image),false),
                                                authenticationResponse.getScriptData().getInteger("level"),
                                                authenticationResponse.getScriptData().getFloat("xp"));
                                    }
                                } else {
                                    showProgress(false);
                                }
                            }
                        });
                    }
                }
            });
            // Connect to GS
            GSAndroidPlatform.gs().start();
        } else {
            nameTextInput.setError("Use letters and/or numbers");
        }
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

        loginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        loginFormView.animate().setDuration(shortAnimTime).alpha(
                show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                loginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            }
        });

        progressView.setVisibility(show ? View.VISIBLE : View.GONE);
        progressView.animate().setDuration(shortAnimTime).alpha(
                show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                progressView.setVisibility(show ? View.VISIBLE : View.GONE);
            }
        });
    }
}

