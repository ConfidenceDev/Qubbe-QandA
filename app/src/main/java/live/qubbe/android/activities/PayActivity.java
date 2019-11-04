package live.qubbe.android.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import co.paystack.android.Paystack;
import co.paystack.android.PaystackSdk;
import co.paystack.android.Transaction;
import co.paystack.android.exceptions.ExpiredAccessCodeException;
import co.paystack.android.model.Card;
import co.paystack.android.model.Charge;
import live.qubbe.android.R;
import live.qubbe.android.utils.NetworkConnection;
import pl.bclogic.pulsator4droid.library.PulsatorLayout;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class PayActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore firebaseFirestore;

    private String currentUserId, content, category, amt, dollarAmt = null;
    private EditText mEmailField, mCardNameField, mCardNumberField,
            mMonthField, mYearField, mCVVField;
    private Button mPayBtn;
    private TextView mAmtView;
    private Card card;
    private Charge charge;
    private PulsatorLayout mPulse;
    private Transaction transaction;
    private NetworkConnection networkConnection;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        setContentView(R.layout.activity_pay);

        mAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();
        PaystackSdk.initialize(getApplicationContext());
        networkConnection = new NetworkConnection();

        content = getIntent().getStringExtra("content");
        category = getIntent().getStringExtra("category");
        amt = getIntent().getStringExtra("amt");

        mEmailField = findViewById(R.id.edit_email_address);
        mCardNameField = findViewById(R.id.edit_card_name);
        mCardNumberField = findViewById(R.id.edit_card_number);
        mMonthField = findViewById(R.id.edit_expiry_month);
        mYearField = findViewById(R.id.edit_expiry_year);
        mCVVField = findViewById(R.id.edit_cvv);
        mPayBtn = findViewById(R.id.pay_button);
        mPulse = findViewById(R.id.payPulse);
        mAmtView = findViewById(R.id.amtView);

        mAmtView.setText("" + getResources().getString(R.string.dollar_sign) + amt);

        firebaseFirestore.collection("Media").document("Information")
                .addSnapshotListener(this, new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(@Nullable DocumentSnapshot documentSnapshot,
                                        @Nullable FirebaseFirestoreException e) {
                        if (documentSnapshot != null) {
                            if (documentSnapshot.exists()) {
                                dollarAmt = documentSnapshot.getString("dollarAmt");
                            }
                        }
                    }
                });

        //============================ Functions =================================================

        findViewById(R.id.payBack).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        mCardNumberField.addTextChangedListener(new TextWatcher() {
            private static final char space = ' ';

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                /*if (s.length() > 0 && (s.length() % 5) == 0) {
                    char c = s.charAt(s.length() - 1);
                    if (Character.isDigit(c)) {
                        mCardNumberField.append(String.valueOf(space));
                    }
                }

                if (mCardNumberField.length() == 4) {
                    mCardNumberField.setText(mCardNumberField.getText() + " ");
                } */
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() > 0 && (s.length() % 5) == 0) {
                    char c = s.charAt(s.length() - 1);
                    if (space == c) {
                        s.delete(s.length() - 1, s.length());
                    }
                }
                if (s.length() > 0 && (s.length() % 5) == 0) {
                    char c = s.charAt(s.length() - 1);
                    if (Character.isDigit(c) && TextUtils.split(s.toString(),
                            String.valueOf(space)).length <= 3) {
                        s.insert(s.length() - 1, String.valueOf(space));
                    }
                }
            }
        });

        try {
            if (mAuth.getCurrentUser() != null) {
                currentUserId = mAuth.getCurrentUser().getUid();

                mPayBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (networkConnection.isConnected(PayActivity.this)) {
                            if (!validateForm()) {
                                return;
                            }

                            if (dollarAmt == null || TextUtils.isEmpty(dollarAmt)){
                                dollarAmt = "360";
                            }

                            /* final String cardNumber = mCardNumberField.getText().toString().trim();
                            final int expiryMonth = Integer.parseInt(mMonthField.getText().toString().trim());
                            final int expiryYear = Integer.parseInt(mYearField.getText().toString().trim());
                            final String cvv = mCVVField.getText().toString().trim(); */

                            String cardNumber = "507850785078507812";
                            int expiryMonth = 10; //any month in the future
                            int expiryYear = 21; // any year in the future
                            String cvv = "081";

                            card = new Card(cardNumber, expiryMonth, expiryYear, cvv);

                            if (card.isValid()) {
//                                Toast.makeText(PayActivity.this, getResources().getString(R.string.card_valid),
//                                        Toast.LENGTH_SHORT).show();
                                disableInit();
                                performCharge();
                            } else {
                                Toast.makeText(PayActivity.this,
                                        getResources().getString(R.string.card_not_valid), Toast.LENGTH_LONG).show();
                            }
                        } else {
                            Toast.makeText(PayActivity.this,
                                    getResources().getString(R.string.no_internet),
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Method to perform the charging of the card
     */
    private void performCharge() {
        //create a Charge object
        charge = new Charge();
        //set the card to charge
        charge.setCard(card);
        //charge.setCurrency("USD");

        mPulse.setVisibility(View.VISIBLE);
        mPulse.start();
        //call this method if you set a plan
        //charge.setPlan("PLN_yourplan");
        //Integer.parseInt(amt)
        String totalAmt = String.valueOf(Integer.parseInt(dollarAmt) * Integer.parseInt(amt));
        String mainVal = totalAmt.concat("00");
        charge.setEmail("stackdriveinc@gmail.com"); //dummy email address
        charge.setAmount(Integer.parseInt(mainVal)); //test amount
        charge.setReference("QubbeAndroid_" + Calendar.getInstance().getTimeInMillis());

        PaystackSdk.chargeCard(PayActivity.this, charge, new Paystack.TransactionCallback() {
            @Override
            public void onSuccess(Transaction transaction) {
                // This is called only after transaction is deemed successful.
                // Retrieve the transaction, and send its reference to your server
                // for verification.

                PayActivity.this.transaction = transaction;
                final Date utc = new Date(System.currentTimeMillis());
                final String randomId = UUID.randomUUID().toString();

                Map<String, Object> payMap = new HashMap<>();
                payMap.put("posterId", currentUserId);
                payMap.put("postId", randomId);
                payMap.put("mail", mEmailField.getText().toString());
                payMap.put("card_name", mCardNameField.getText().toString());
                payMap.put("reference", transaction.getReference());
                payMap.put("utc", utc);

                firebaseFirestore.collection("Payments").document(currentUserId)
                        .collection("Paid").document(randomId).set(payMap)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (!task.isSuccessful()) {
                                    Toast.makeText(PayActivity.this, getResources()
                                                    .getString(R.string.request_err),
                                            Toast.LENGTH_LONG).show();
                                } else {
                                    continuePost(content, category, amt);
                                }
                            }
                        });
            }

            @Override
            public void beforeValidate(Transaction transaction) {
                // This is called only before requesting OTP.
                // Save reference so you may send to server. If
                // error occurs with OTP, you should still verify on server.

                //PayActivity.this.transaction = transaction;

            }

            @Override
            public void onError(Throwable error, Transaction transaction) {
                //handle error here
                if (error instanceof ExpiredAccessCodeException) {
                    return;
                }

                mPulse.stop();
                mPulse.setVisibility(View.GONE);
                enableInit();
                PayActivity.this.transaction = transaction;
                if (transaction != null) {
                    Toast.makeText(PayActivity.this, getResources().getString(R.string.transaction_err)
                            + transaction.getReference(), Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(PayActivity.this, getResources().getString(R.string.transaction_failed)
                            , Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void continuePost(String content, String category, String amt) {
        final String randomId = UUID.randomUUID().toString();
        final Date utc = new Date(System.currentTimeMillis());

        Map<String, Object> mapToPost = new HashMap<>();
        mapToPost.put("contentText", content);
        mapToPost.put("contentTextLower", content.toLowerCase());
        mapToPost.put("amt", amt);
        mapToPost.put("category", category);
        mapToPost.put("postId", randomId);
        mapToPost.put("posterId", currentUserId);
        mapToPost.put("utc", utc);

        firebaseFirestore.collection("All").document(randomId).set(mapToPost);
        firebaseFirestore.collection(category).document(randomId).set(mapToPost);
        firebaseFirestore.collection("Users").document(currentUserId).
                collection("Posts").document(randomId).set(mapToPost)
                .addOnSuccessListener(this, new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                        Toast.makeText(PayActivity.this, getResources().getString(R.string.post_created),
                                Toast.LENGTH_SHORT).show();

                        mPulse.stop();
                        mPulse.setVisibility(View.GONE);
                        enableInit();
                        backIntent();
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(PayActivity.this, getResources().getString(R.string.err) +
                        e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    private boolean validateForm() {
        boolean valid = true;

        String email = mEmailField.getText().toString();
        if (TextUtils.isEmpty(email)) {
            mEmailField.setError(getResources().getString(R.string.required));
            valid = false;
        } else {
            mEmailField.setError(null);
        }

        String cardName = mCardNameField.getText().toString();
        if (TextUtils.isEmpty(cardName)) {
            mCardNameField.setError(getResources().getString(R.string.required));
            valid = false;
        } else {
            mCardNameField.setError(null);
        }

        String cardNumber = mCardNumberField.getText().toString();
        if (TextUtils.isEmpty(cardNumber)) {
            mCardNumberField.setError(getResources().getString(R.string.required));
            valid = false;
        } else {
            mCardNumberField.setError(null);
        }

        String expiryMonth = mMonthField.getText().toString();
        if (TextUtils.isEmpty(expiryMonth)) {
            mMonthField.setError(getResources().getString(R.string.required));
            valid = false;
        } else {
            mMonthField.setError(null);
        }

        String expiryYear = mYearField.getText().toString();
        if (TextUtils.isEmpty(expiryYear)) {
            mYearField.setError(getResources().getString(R.string.required));
            valid = false;
        } else {
            mYearField.setError(null);
        }

        String cvv = mCVVField.getText().toString();
        if (TextUtils.isEmpty(cvv)) {
            mCVVField.setError(getResources().getString(R.string.required));
            valid = false;
        } else {
            mCVVField.setError(null);
        }

        return valid;
    }

    @Override
    protected void onPause() {
        super.onPause();
        mPulse.stop();
        mPulse.setVisibility(View.GONE);
    }

    private void disableInit() {
        mCardNumberField.setEnabled(false);
        mMonthField.setEnabled(false);
        mYearField.setEnabled(false);
        mCVVField.setEnabled(false);
        mPayBtn.setEnabled(false);
    }

    private void enableInit() {
        mCardNumberField.setEnabled(true);
        mMonthField.setEnabled(true);
        mYearField.setEnabled(true);
        mCVVField.setEnabled(true);
        mPayBtn.setEnabled(true);
    }

    @Override
    public void onBackPressed() {
        if (transaction != null) {
            AlertDialog.Builder tipsAlert = new AlertDialog.Builder(PayActivity.this);
            tipsAlert.setMessage(getResources().getString(R.string.cancel_payment))
                    .setPositiveButton(getResources().getString(R.string.wait), null)
                    .setNegativeButton(getResources().getString(R.string.cancel),
                            new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            transaction = null;
                            backIntent();
                        }
                    });
            AlertDialog start_builder = tipsAlert.create();
            start_builder.show();
        } else {
            backIntent();
            super.onBackPressed();
        }
    }

    private void backIntent() {
        startActivity(new Intent(PayActivity.this, MainActivity.class));
        finish();
        overridePendingTransition(R.anim.slide_in_left, R.anim.no_anim);
    }
}
