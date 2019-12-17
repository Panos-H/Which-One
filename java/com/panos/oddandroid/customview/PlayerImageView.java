package com.panos.oddandroid.customview;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.AttributeSet;
import android.util.Base64;
import android.widget.Toast;

import androidx.appcompat.widget.AppCompatImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.gamesparks.sdk.GSEventConsumer;
import com.gamesparks.sdk.android.GSAndroidPlatform;
import com.gamesparks.sdk.api.autogen.GSResponseBuilder;
import com.panos.oddandroid.R;
import com.panos.oddandroid.model.User;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;

public class PlayerImageView extends AppCompatImageView {

    String imageFileName;
    private Uri imageUri;
    SharedPreferences sharedPreferences;

    public PlayerImageView(Context context) {
        super(context);
        init(null,0);
    }

    public PlayerImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs,0);
    }

    public PlayerImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs, defStyle);
    }

    void init(AttributeSet attrs, int defStyle) {

        TypedArray typedArray = getContext().getTheme().obtainStyledAttributes(attrs, R.styleable.PlayerImageView,0,0);
        // Get Path's attributes as defined in XML
        imageFileName = typedArray.getBoolean(R.styleable.PlayerImageView_user,true) ? "user_image" : "enemy_image";
    }

    public void applyImage() {

        Glide.with(this)
                .load(imageUri)
                .apply(RequestOptions.circleCropTransform())
                .into(this);
    }

    void applyImage(Uri uri) {

        Glide.with(this)
                .load(uri)
                .apply(RequestOptions.circleCropTransform())
                .into(this);
    }

    void applyImage(Bitmap bitmap) {

        Glide.with(this)
                .load(bitmap)
                .apply(RequestOptions.circleCropTransform())
                .into(this);
    }

    void applyImage(byte[] imageBytes) {

        Glide.with(this)
                .load(imageBytes)
                .apply(RequestOptions.circleCropTransform())
                .into(this);
    }

    /**
     * Loads Player's Image from a certain directory and applies it to the Image View.
     */
    public void loadImage() {

        // Load Image's file
        File file = new File(getContext().getFilesDir(), imageFileName);
        if (file.exists()) {
            // Decode it to Bitmap
            Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
            // Show it
            applyImage(bitmap);
        }
    }

    /**
     * Attempts to store picked Image inside App's data.
     * @param imageBytes The bytes of the converted Image.
     * @return Storage verification.
     */
    boolean storeImage(byte[] imageBytes) {

        FileOutputStream fos;
        try {
            fos = getContext().openFileOutput(imageFileName, Context.MODE_PRIVATE);
            fos.write(imageBytes);
            fos.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Converts User's picked cropped Image into bytes and stores it inside App's data.
     * @return Informs activity about the storage completion.
     */
    public boolean storeImage() {

        sharedPreferences = getContext().getSharedPreferences(getContext().getString(R.string.shared_preferences),Context.MODE_PRIVATE);

        assert imageUri != null;
        try {
            // Set cropped Image URI as input stream
            InputStream imageStream = getContext().getContentResolver().openInputStream(imageUri);
            // Create Image's Bitmap through Bitmap decoding
            Bitmap bitmap = BitmapFactory.decodeStream(imageStream);
            // Compress the Bitmap to bytes
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos);
            byte[] imageBytes = baos.toByteArray();

            // Store Image, as bytes, inside App's private data for future use
            if (storeImage(imageBytes)) {
                User.getUser().setUsingImage(true);
                SharedPreferences.Editor prefEditor = sharedPreferences.edit();
                prefEditor.putBoolean(getContext().getString(R.string.pref_using_image), true);
                prefEditor.putBoolean(getContext().getString(R.string.pref_image_uploaded), false);
                prefEditor.apply();
                return true;
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (NullPointerException n) {
            n.printStackTrace();
        }
        return false;
    }

    /**
     * Encodes Image to a Base 64 string and attempts to upload it to the server.
     * @param imageBytes Image data as Byte array.
     */
    private void uploadImage(byte[] imageBytes) {

        String imageBase64 = Base64.encodeToString(imageBytes, Base64.DEFAULT);

        GSAndroidPlatform.gs().getRequestBuilder().createLogEventRequest()
                .setEventKey("uploadImage")
                .setEventAttribute("imageB64", imageBase64)
                .send(new GSEventConsumer<GSResponseBuilder.LogEventResponse>() {
                    @Override
                    public void onEvent(GSResponseBuilder.LogEventResponse response) {
                        if (response.hasErrors()) {
                            Toast.makeText(getContext(),"Upload failed!", Toast.LENGTH_LONG).show();
                        } else {
                            SharedPreferences.Editor prefEditor = sharedPreferences.edit();
                            prefEditor.putBoolean(getContext().getString(R.string.pref_image_uploaded), true);
                            prefEditor.apply();

                            Toast.makeText(getContext(),"Upload done!", Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    public void uploadImage() {

        sharedPreferences = getContext().getSharedPreferences(getContext().getString(R.string.shared_preferences),Context.MODE_PRIVATE);

        File file = new File(getContext().getFilesDir(),imageFileName);
        if (file.exists()) {
            // Create Image's Bitmap through Bitmap decoding
            Bitmap imageBitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
            // Compress the Bitmap to bytes
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            imageBitmap.compress(Bitmap.CompressFormat.JPEG,80,baos);

            uploadImage(baos.toByteArray());
        }
    }

    public Uri getImageUri() {
        return imageUri;
    }

    public void setImageUri(Uri imageUri) {
        this.imageUri = imageUri;
    }

    public void setImage(Uri imageUri) {
        applyImage(imageUri);
    }

    public void setImage(byte[] imageBytes) {
        applyImage(imageBytes);
    }

    public void setImage(Bitmap imageBitmap) {
        applyImage(imageBitmap);
    }

    public void setUsingImage(boolean isUsing) {
        if (isUsing) loadImage();
    }
}
