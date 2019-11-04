package live.qubbe.android.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import live.qubbe.android.R;
import live.qubbe.android.fragments.ResponseFragment;
import live.qubbe.android.utils.Counter;
import live.qubbe.android.utils.NetworkConnection;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class ResponseActivity extends AppCompatActivity {

    private ImageView mBackBtn;
    private TextView mResponseCount;
    private ImageView mSendBtn;
    private EditText mResponseField;

    private ResponseFragment responseFragment;
    private String postId, posterId, currentUserId;

    private FirebaseAuth mAuth;
    private FirebaseFirestore firebaseFirestore;
    private Counter counter;
    private NetworkConnection networkConnection;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        setContentView(R.layout.activity_response);

        mAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();
        networkConnection = new NetworkConnection();

        responseFragment = new ResponseFragment();
        counter = new Counter();
        postId = getIntent().getStringExtra("postId");
        posterId = getIntent().getStringExtra("posterId");

        mBackBtn = findViewById(R.id.responseBarBack);
        mResponseCount = findViewById(R.id.responseMainCount);
        mResponseField = findViewById(R.id.responseField);
        mSendBtn = findViewById(R.id.responseBtn);

        mBackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        //========================= Functions ==========================================
        try {
            if (mAuth.getCurrentUser() != null) {
                currentUserId = mAuth.getCurrentUser().getUid();

                Bundle bundle = new Bundle();
                bundle.putString("posterId", posterId);
                bundle.putString("postId", postId);
                responseFragment.setArguments(bundle);
                getSupportFragmentManager().beginTransaction().replace(R.id.responseFrame, responseFragment).commit();

                //============================== Get Comments Count =======================================
                firebaseFirestore.collection("All")
                        .document(postId).collection("Response")
                        .addSnapshotListener(this, new EventListener<QuerySnapshot>() {
                            @Override
                            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots,
                                                @Nullable FirebaseFirestoreException e) {

                                if (queryDocumentSnapshots != null) {
                                    if (!queryDocumentSnapshots.isEmpty()) {

                                        int count = queryDocumentSnapshots.size();
                                        mResponseCount.setText(counter.countVal
                                                (Long.parseLong(String.valueOf(count)), ResponseActivity.this));
                                    } else {
                                        mResponseCount.setText("0");
                                    }
                                }
                            }
                        });

                mSendBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String text = mResponseField.getText().toString();

                        if (!TextUtils.isEmpty(text)) {
                            if (!networkConnection.isConnected(ResponseActivity.this)) {
                                Toast.makeText(ResponseActivity.this,
                                        getResources().getString(R.string.no_internet), Toast.LENGTH_SHORT).show();
                            } else {
                                mResponseField.setText(null);
                                mSendBtn.setEnabled(false);
                                final String randomId = UUID.randomUUID().toString();
                                final Date utc = new Date(System.currentTimeMillis());

                                //final String dateString = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date());
                                //final String timeString = new SimpleDateFormat("hh:mm a", Locale.getDefault()).format(new Date());

                                Map<String, Object> userCommentMap = new HashMap<>();
                                userCommentMap.put("response", text);
                                userCommentMap.put("userId", currentUserId);
                                userCommentMap.put("responseId", randomId);
                                userCommentMap.put("postId", postId);
                                userCommentMap.put("posterId", posterId);
                                userCommentMap.put("utc", utc);

                                firebaseFirestore.collection("All")
                                        .document(postId).collection("Response")
                                        .document(randomId).set(userCommentMap)
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (!task.isSuccessful()) {
                                                    String err = Objects.requireNonNull(task.getException()).getMessage();
                                                    Toast.makeText(ResponseActivity.this, getResources().getString(R.string.err) + err, Toast.LENGTH_LONG).show();
                                                } else {
                                                    mSendBtn.setEnabled(true);
                                                    Toast.makeText(ResponseActivity.this, getResources().getString(R.string.sent), Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        });
                            }
                        } else {
                            Toast.makeText(ResponseActivity.this, getResources().getString(R.string.not_typed), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}
