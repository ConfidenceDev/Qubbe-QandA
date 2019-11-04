package live.qubbe.android.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.firebase.auth.FirebaseAuth;

import live.qubbe.android.R;
import live.qubbe.android.utils.Constant;
import live.qubbe.android.utils.NetworkConnection;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class SettingsActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;

    private ConstraintLayout mAbout, mInvite, mShare, mRate, mWeb, mFeedback, mLicense, mTerms, mHelp;
    private TextView mSignOut, mDelete;
    private SharedPreferences mPreferences;
    private SharedPreferences.Editor mEditor;
    private GoogleApiClient mGoogleApiClient;
    private String currentUserId;
    private ImageView mBackBtn;
    private Constant constant;
    private NetworkConnection networkConnection;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        mAuth = FirebaseAuth.getInstance();
        if (mAuth.getCurrentUser() != null) {
            currentUserId = mAuth.getCurrentUser().getUid();
        }

        constant = new Constant(this);
        networkConnection = new NetworkConnection();

        mAbout = findViewById(R.id.con1);
        mInvite = findViewById(R.id.con2);
        mShare = findViewById(R.id.con3);
        mRate = findViewById(R.id.con4);
        mWeb = findViewById(R.id.con5);
        mFeedback = findViewById(R.id.con6);
        mLicense = findViewById(R.id.con7);
        mTerms = findViewById(R.id.con8);
        mHelp = findViewById(R.id.con9);
        mSignOut = findViewById(R.id.signOutBtn);
        mDelete = findViewById(R.id.deleteBtn);
        mBackBtn = findViewById(R.id.settingsBack);

        mPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        mEditor = mPreferences.edit();

        findViewById(R.id.editAccBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(SettingsActivity.this, EditAccountActivity.class));
                overridePendingTransition(R.anim.slide_in_right, R.anim.no_anim);
            }
        });

        //======================== Functions ============================================

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .
                        enableAutoManage(this, new GoogleApiClient.OnConnectionFailedListener() {
                            @Override
                            public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
                                Toast.makeText(SettingsActivity.this, getResources().getString(R.string.error), Toast.LENGTH_SHORT).show();
                            }
                        })
                .
                        addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .

                        build();
        mGoogleApiClient.connect();

        mAbout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder update_builder = new AlertDialog.Builder(SettingsActivity.this);
                update_builder
                        .setTitle(getResources().getString(R.string.about))
                        .setMessage(getResources().getString(R.string.version))
                        .setPositiveButton(getResources().getString(R.string.ok), null);

                AlertDialog alert_update = update_builder.create();
                alert_update.show();
            }
        });

        mInvite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent emailIntent = new Intent(Intent.ACTION_SEND);

                emailIntent.setData(Uri.parse("mailto:"));
                emailIntent.setType("text/plain");
                emailIntent.putExtra(Intent.EXTRA_SUBJECT, getResources().getString(R.string.invite_header));
                emailIntent.putExtra(Intent.EXTRA_TEXT, getResources().getString(R.string.invite_message));

                try {
                    startActivity(Intent.createChooser(emailIntent, getResources().getString(R.string.send_a_mail)));
                } catch (android.content.ActivityNotFoundException ex) {
                    Toast.makeText(SettingsActivity.this, getResources().getString(R.string.no_client), Toast.LENGTH_LONG).show();
                }
            }
        });

        mShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*ApplicationInfo api = SettingsActivity.this.getApplicationContext().getApplicationInfo();
                String apkPath = api.sourceDir;

                Intent share_intent = new Intent(Intent.ACTION_SEND);
                share_intent.setType("application/vnd.android.package-archive");

                share_intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(new File(apkPath)));
                startActivity(Intent.createChooser(share_intent, getResources().
                        getString(R.string.share_app_using))); */
                constant.shareApplication();
            }
        });

        mRate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openStore(getResources().getString(R.string.store_url));
            }
        });

        mWeb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent web_intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.sites.google.com/view/unstackmedia"));
                startActivity(web_intent);
            }
        });

        mFeedback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String[] TO = {"stackdriveinc@gmail.com"};
                Intent emailIntent = new Intent(Intent.ACTION_SEND);

                emailIntent.setData(Uri.parse("mailto:"));
                emailIntent.setType("text/plain");
                emailIntent.putExtra(Intent.EXTRA_EMAIL, TO);
                emailIntent.putExtra(Intent.EXTRA_SUBJECT, getResources().getString(R.string.feedback_header));
                emailIntent.putExtra(Intent.EXTRA_TEXT, getResources().getString(R.string.feedback_message));

                try {
                    startActivity(Intent.createChooser(emailIntent, getResources().getString(R.string.send_a_mail)));
                } catch (android.content.ActivityNotFoundException ex) {
                    Toast.makeText(SettingsActivity.this, getResources().getString(R.string.no_client), Toast.LENGTH_LONG).show();
                }
            }
        });

        mLicense.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SettingsActivity.this, LicenseActivity.class));
            }
        });

        mTerms.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SettingsActivity.this, TermsActivity.class));
                overridePendingTransition(R.anim.no_anim, R.anim.no_anim);
            }
        });

        mHelp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder tipsAlert = new AlertDialog.Builder(SettingsActivity.this);
                tipsAlert.setTitle(getResources().getString(R.string.help))
                        .setMessage(getResources().getString(R.string.help1) +
                                getResources().getString(R.string.help2) +
                                getResources().getString(R.string.help3))
                        .setPositiveButton(getResources().getString(R.string.finish), null);
                AlertDialog start_builder = tipsAlert.create();
                start_builder.show();
            }
        });

        mSignOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signOutMethod();
            }
        });

        mDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteMethod();
            }
        });

        mBackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    private void signOutMethod() {

        AlertDialog.Builder update_builder = new AlertDialog.Builder(this);
        update_builder
                .setMessage(getResources().getString(R.string.sign_out_message))
                .setPositiveButton(getResources().getString(R.string.yes), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (mAuth.getCurrentUser() != null) {
                            if (networkConnection.isConnected(SettingsActivity.this)) {
                                exitMethod();
                            } else {
                                Toast.makeText(SettingsActivity.this, getResources()
                                        .getString(R.string.no_internet), Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                }).setNegativeButton(getResources().getString(R.string.no), null);

        AlertDialog alert_update = update_builder.create();
        alert_update.show();
    }

    private void exitMethod() {
        try {
            Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(
                    new ResultCallback<Status>() {
                        @Override
                        public void onResult(@NonNull Status status) {
                            mAuth.signOut();
                            Intent exitIntent = new Intent(SettingsActivity.this, SignInActivity.class);
                            startActivity(exitIntent);
                            overridePendingTransition(R.anim.slide_in_left, R.anim.no_anim);
                            finishAndRemoveTask();
                        }
                    });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void deleteMethod() {

        AlertDialog.Builder update_builder = new AlertDialog.Builder(this);
        update_builder
                .setMessage(getResources().getString(R.string.delete_header))
                .setPositiveButton(getResources().getString(R.string.yes), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        String[] TO = {"stackdriveinc@gmail.com"};
                        Intent emailIntent = new Intent(Intent.ACTION_SEND);

                        emailIntent.setData(Uri.parse("mailto:"));
                        emailIntent.setType("text/plain");
                        emailIntent.putExtra(Intent.EXTRA_EMAIL, TO);
                        emailIntent.putExtra(Intent.EXTRA_SUBJECT, getResources().getString(R.string.delete_subject));
                        emailIntent.putExtra(Intent.EXTRA_TEXT, getResources().getString(R.string.delete_message));

                        try {
                            startActivity(Intent.createChooser(emailIntent, getResources().getString(R.string.send_a_mail)));
                        } catch (android.content.ActivityNotFoundException ex) {
                            Toast.makeText(SettingsActivity.this, getResources().getString(R.string.no_client), Toast.LENGTH_LONG).show();
                        }

                    }
                }).setNegativeButton(getResources().getString(R.string.no), null);

        AlertDialog alert_update = update_builder.create();
        alert_update.show();
    }

    private void openStore(String updateUrl) {
        final Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(updateUrl));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}
