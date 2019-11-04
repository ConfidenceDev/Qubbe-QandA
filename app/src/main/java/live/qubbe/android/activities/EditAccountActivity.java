package live.qubbe.android.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;
import java.util.Map;

import live.qubbe.android.R;
import live.qubbe.android.utils.NetworkConnection;
import pl.bclogic.pulsator4droid.library.PulsatorLayout;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class EditAccountActivity extends AppCompatActivity {

    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth mAuth;
    private String currentUserId;

    private EditText mName, mLocation, mFace, mInsta, mTwit, mWebsite;
    private Button mFinishBtn;
    private ImageView mProfImg, mBackBtn;

    private Uri mainImageUri = null;
    private Boolean isChanged = false;
    private PulsatorLayout mEditPulse;
    private StorageReference storageReference;
    private SharedPreferences mPreferences;
    private SharedPreferences.Editor mEditor;
    private RelativeLayout mEditBtn;
    private NetworkConnection networkConnection;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        super.onCreate(savedInstanceState);
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        setContentView(R.layout.activity_edit_account);

        mAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();
        mEditor = mPreferences.edit();
        networkConnection = new NetworkConnection();

        if (mAuth.getCurrentUser() != null) {
            currentUserId = mAuth.getCurrentUser().getUid();

            //------------------------ Initialize ---------------------------------------------------

            mFinishBtn = findViewById(R.id.saveBtn);
            mEditPulse = findViewById(R.id.editPulse);

            mName = findViewById(R.id.userNameField);
            mProfImg = findViewById(R.id.profImage);
            mLocation = findViewById(R.id.locationField);
            mFace = findViewById(R.id.faceField);
            mInsta = findViewById(R.id.instaField);
            mTwit = findViewById(R.id.twitField);
            mWebsite = findViewById(R.id.webField);
            mBackBtn = findViewById(R.id.editBack);
            mEditBtn = findViewById(R.id.editBtn);

            mEditPulse.setVisibility(View.VISIBLE);
            mEditPulse.start();

            try {
                //----------------------- Functions -------------------------------------------
                firebaseFirestore.collection("Users").document(currentUserId)
                        .addSnapshotListener(this, new EventListener<DocumentSnapshot>() {
                            @Override
                            public void onEvent(@javax.annotation.Nullable DocumentSnapshot documentSnapshot,
                                                @javax.annotation.Nullable FirebaseFirestoreException e) {

                                if (documentSnapshot != null) {
                                    if (documentSnapshot.exists()) {
                                        String name = documentSnapshot.getString("userName");
                                        String location = documentSnapshot.getString("location");
                                        String face = documentSnapshot.getString("facebook");
                                        String insta = documentSnapshot.getString("instagram");
                                        String twit = documentSnapshot.getString("twitter");
                                        String website = documentSnapshot.getString("website");
                                        String image = documentSnapshot.getString("image");

                                        mName.setText(name);
                                        mLocation.setText(location);
                                        mFace.setText(face);
                                        mInsta.setText(insta);
                                        mTwit.setText(twit);
                                        mWebsite.setText(website);

                                        if (image != null && !image.equals("")) {
                                            mainImageUri = Uri.parse(image);

                                            RequestOptions placeholderRequest = new RequestOptions();
                                            placeholderRequest.placeholder(R.color.black_300);
                                            Glide.with(EditAccountActivity.this).setDefaultRequestOptions(placeholderRequest)
                                                    .load(image).into(mProfImg);
                                        }
                                    }
                                    mEditPulse.setVisibility(View.GONE);
                                    mEditPulse.stop();
                                }
                            }
                        });

                mFinishBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        final String name = mName.getText().toString();
                        final String location = mLocation.getText().toString();
                        final String face = mFace.getText().toString();
                        final String insta = mInsta.getText().toString();
                        final String twit = mTwit.getText().toString();
                        final String web = mWebsite.getText().toString();

                        if (!TextUtils.isEmpty(name) && mainImageUri != null) {

                            if (!networkConnection.isConnected(EditAccountActivity.this)) {
                                Toast.makeText(EditAccountActivity.this, getResources().getString(R.string.no_internet),
                                        Toast.LENGTH_SHORT).show();
                            } else {

                                mEditPulse.setVisibility(View.VISIBLE);
                                mEditPulse.start();
                                mFinishBtn.setEnabled(false);

                                Query query = firebaseFirestore.collection("Users")
                                        .orderBy("userName").startAt(name).endAt(name + "\uf8ff");
                                query.addSnapshotListener(new EventListener<QuerySnapshot>() {
                                    @Override
                                    public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots,
                                                        @Nullable FirebaseFirestoreException e) {
                                        if (queryDocumentSnapshots != null) {
                                            if (!queryDocumentSnapshots.isEmpty()) {

                                                for (@NonNull DocumentChange doc : queryDocumentSnapshots.getDocumentChanges()) {
                                                    if (doc.getType() == DocumentChange.Type.ADDED) {
                                                        String docId = doc.getDocument().getId();

                                                        if (docId.equals(currentUserId)) {
                                                            startStorage(name, location, face, insta, twit, web);

                                                        } else {
                                                            mEditPulse.setVisibility(View.GONE);
                                                            mEditPulse.stop();
                                                            mFinishBtn.setEnabled(true);

                                                            Toast.makeText(EditAccountActivity.this,
                                                                    getResources()
                                                                            .getString(R.string.username_exist),
                                                                    Toast.LENGTH_LONG).show();
                                                        }
                                                    }
                                                }

                                            } else {
                                                startStorage(name, location, face, insta, twit, web);
                                            }
                                        }
                                    }
                                });
                            }

                        } else {
                            Toast.makeText(EditAccountActivity.this, getResources().getString(R.string.not_complete), Toast.LENGTH_SHORT).show();
                        }
                    }
                });

                mEditBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            if (ContextCompat.checkSelfPermission(EditAccountActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)
                                    != PackageManager.PERMISSION_GRANTED) {

                                ActivityCompat.requestPermissions(EditAccountActivity.this, new String[]
                                        {Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
                            } else {
                                // start picker to get image for cropping and then use the image in cropping activity
                                BringImagePicker();
                            }
                        } else {
                            BringImagePicker();
                        }
                    }
                });

                mBackBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onBackPressed();
                    }
                });

            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

    private void startStorage(String name, String location, String face,
                              String insta, String twit, String web) {
        if (isChanged) {
            StorageReference image_path = storageReference.child("profile_images").child(currentUserId + ".jpg");
            image_path.putFile(mainImageUri).addOnCompleteListener(EditAccountActivity.this,
                    new OnCompleteListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                            if (task.isSuccessful()) {
                                storeFirestore(task, name, location, face, insta, twit, web);

                            } else {
                                String error = task.getException().getMessage();
                                Toast.makeText(EditAccountActivity.this,
                                        getResources().getString(R.string.err) + error,
                                        Toast.LENGTH_LONG).show();
                            }
                            mFinishBtn.setEnabled(true);
                        }
                    });
        } else {
            storeFirestore(null, name, location, face, insta, twit, web);
        }
    }

    private void storeFirestore(Task<UploadTask.TaskSnapshot> task, final String name,
                                String location, String face,
                                String insta, String twit, String web) {

        Uri download_uri;
        String loc, fac, ins, twi, website;

        if (task != null) {
            download_uri = task.getResult().getDownloadUrl();
        } else {
            download_uri = mainImageUri;
        }

        if (!TextUtils.isEmpty(location)) {
            loc = location;
        } else {
            loc = "";
        }

        if (!TextUtils.isEmpty(face)) {
            fac = face;
        } else {
            fac = "";
        }

        if (!TextUtils.isEmpty(insta)) {
            ins = insta;
        } else {
            ins = "";
        }

        if (!TextUtils.isEmpty(twit)) {
            twi = twit;
        } else {
            twi = "";
        }

        if (!TextUtils.isEmpty(web)) {
            website = web;
        } else {
            website = "";
        }

        Map<String, Object> userMap = new HashMap<>();
        userMap.put("userName", name);
        userMap.put("userNameLower", name.toLowerCase());
        userMap.put("location", loc);
        userMap.put("facebook", fac);
        userMap.put("instagram", ins);
        userMap.put("twitter", twi);
        userMap.put("website", website);
        userMap.put("image", download_uri.toString());

        firebaseFirestore.collection("Users").document(currentUserId).
                set(userMap).addOnCompleteListener(EditAccountActivity.this, new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(EditAccountActivity.this, getResources().
                            getString(R.string.saved), Toast.LENGTH_SHORT).show();
                    Intent viewIntent = new Intent(EditAccountActivity.this, MainActivity.class);
                    startActivity(viewIntent);
                    overridePendingTransition(R.anim.slide_in_right, R.anim.no_anim);
                    finish();
                } else {
                    String err = task.getException().getMessage();
                    Toast.makeText(EditAccountActivity.this, err, Toast.LENGTH_LONG).show();
                }
                mEditPulse.setVisibility(View.GONE);
                mEditPulse.stop();
            }
        });

    }

    private void BringImagePicker() {
        CropImage.activity()
                .setGuidelines(CropImageView.Guidelines.ON)
                .setAspectRatio(1, 1)
                .start(EditAccountActivity.this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {

                isChanged = true;
                mainImageUri = result.getUri();
                mProfImg.setImageURI(mainImageUri);

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}
