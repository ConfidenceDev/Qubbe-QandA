package live.qubbe.android.fragments;


import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import live.qubbe.android.R;
import live.qubbe.android.activities.MainActivity;
import live.qubbe.android.activities.PayActivity;
import live.qubbe.android.utils.NetworkConnection;
import pl.bclogic.pulsator4droid.library.PulsatorLayout;

/**
 * A simple {@link Fragment} subclass.
 */
public class NewFragment extends Fragment {

    private View view;
    private FirebaseAuth mAuth;
    private FirebaseFirestore firebaseFirestore;
    private String currentUserId;

    private TextView mCharCount;
    private EditText mcontentField;
    private PulsatorLayout mPulse;
    private Button mContinueBtn;
    private TextView mScience, mTech, mRomance, mMusic,
            mProgramming, mLifestyle, mArt, mUrban, mNature, mSports, mBusiness, mEducation,
            mMovies, mEntertainment, mGames, mMore;
    private String mClicked = null, amount = null;
    private NetworkConnection networkConnection;

    public NewFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        Objects.requireNonNull(getActivity()).getWindow()
                .setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        view = inflater.inflate(R.layout.fragment_new, container, false);

        mAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();
        networkConnection = new NetworkConnection();

        mCharCount = view.findViewById(R.id.charCount);
        mcontentField = view.findViewById(R.id.questionField);
        mPulse = view.findViewById(R.id.postPulse);
        mContinueBtn = view.findViewById(R.id.continueBtn);

        //============================== Categories =============================================
        mScience = view.findViewById(R.id.scienceText);
        mTech = view.findViewById(R.id.techText);
        mRomance = view.findViewById(R.id.romanceText);
        mMusic = view.findViewById(R.id.musicText);
        mProgramming = view.findViewById(R.id.programmingText);
        mLifestyle = view.findViewById(R.id.lifestyleText);
        mArt = view.findViewById(R.id.artText);
        mUrban = view.findViewById(R.id.urbanText);
        mNature = view.findViewById(R.id.natureText);
        mSports = view.findViewById(R.id.sportsText);
        mBusiness = view.findViewById(R.id.businessText);
        mEducation = view.findViewById(R.id.educationText);
        mMovies = view.findViewById(R.id.moviesText);
        mEntertainment = view.findViewById(R.id.entertainmentText);
        mGames = view.findViewById(R.id.gamesText);
        mMore = view.findViewById(R.id.moreText);

        //============================== Category list =======================================

        mScience.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mScience.setTextColor(getResources().getColor(R.color.colorAccent));
                mTech.setTextColor(getResources().getColor(R.color.hint_text));
                mRomance.setTextColor(getResources().getColor(R.color.hint_text));
                mMusic.setTextColor(getResources().getColor(R.color.hint_text));
                mProgramming.setTextColor(getResources().getColor(R.color.hint_text));
                mLifestyle.setTextColor(getResources().getColor(R.color.hint_text));
                mArt.setTextColor(getResources().getColor(R.color.hint_text));
                mUrban.setTextColor(getResources().getColor(R.color.hint_text));
                mNature.setTextColor(getResources().getColor(R.color.hint_text));
                mSports.setTextColor(getResources().getColor(R.color.hint_text));
                mBusiness.setTextColor(getResources().getColor(R.color.hint_text));
                mEducation.setTextColor(getResources().getColor(R.color.hint_text));
                mMovies.setTextColor(getResources().getColor(R.color.hint_text));
                mEntertainment.setTextColor(getResources().getColor(R.color.hint_text));
                mGames.setTextColor(getResources().getColor(R.color.hint_text));
                mMore.setTextColor(getResources().getColor(R.color.hint_text));

                mClicked = mScience.getText().toString();

            }
        });

        mTech.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mScience.setTextColor(getResources().getColor(R.color.hint_text));
                mTech.setTextColor(getResources().getColor(R.color.colorAccent));
                mRomance.setTextColor(getResources().getColor(R.color.hint_text));
                mMusic.setTextColor(getResources().getColor(R.color.hint_text));
                mProgramming.setTextColor(getResources().getColor(R.color.hint_text));
                mLifestyle.setTextColor(getResources().getColor(R.color.hint_text));
                mArt.setTextColor(getResources().getColor(R.color.hint_text));
                mUrban.setTextColor(getResources().getColor(R.color.hint_text));
                mNature.setTextColor(getResources().getColor(R.color.hint_text));
                mSports.setTextColor(getResources().getColor(R.color.hint_text));
                mBusiness.setTextColor(getResources().getColor(R.color.hint_text));
                mEducation.setTextColor(getResources().getColor(R.color.hint_text));
                mMovies.setTextColor(getResources().getColor(R.color.hint_text));
                mEntertainment.setTextColor(getResources().getColor(R.color.hint_text));
                mMore.setTextColor(getResources().getColor(R.color.hint_text));

                mClicked = mTech.getText().toString();
            }
        });

        mRomance.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mScience.setTextColor(getResources().getColor(R.color.hint_text));
                mTech.setTextColor(getResources().getColor(R.color.hint_text));
                mRomance.setTextColor(getResources().getColor(R.color.colorAccent));
                mMusic.setTextColor(getResources().getColor(R.color.hint_text));
                mProgramming.setTextColor(getResources().getColor(R.color.hint_text));
                mLifestyle.setTextColor(getResources().getColor(R.color.hint_text));
                mArt.setTextColor(getResources().getColor(R.color.hint_text));
                mUrban.setTextColor(getResources().getColor(R.color.hint_text));
                mNature.setTextColor(getResources().getColor(R.color.hint_text));
                mSports.setTextColor(getResources().getColor(R.color.hint_text));
                mBusiness.setTextColor(getResources().getColor(R.color.hint_text));
                mEducation.setTextColor(getResources().getColor(R.color.hint_text));
                mMovies.setTextColor(getResources().getColor(R.color.hint_text));
                mEntertainment.setTextColor(getResources().getColor(R.color.hint_text));
                mGames.setTextColor(getResources().getColor(R.color.hint_text));
                mMore.setTextColor(getResources().getColor(R.color.hint_text));

                mClicked = mRomance.getText().toString();
            }
        });

        mMusic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mScience.setTextColor(getResources().getColor(R.color.hint_text));
                mTech.setTextColor(getResources().getColor(R.color.hint_text));
                mRomance.setTextColor(getResources().getColor(R.color.hint_text));
                mMusic.setTextColor(getResources().getColor(R.color.colorAccent));
                mProgramming.setTextColor(getResources().getColor(R.color.hint_text));
                mLifestyle.setTextColor(getResources().getColor(R.color.hint_text));
                mArt.setTextColor(getResources().getColor(R.color.hint_text));
                mUrban.setTextColor(getResources().getColor(R.color.hint_text));
                mNature.setTextColor(getResources().getColor(R.color.hint_text));
                mSports.setTextColor(getResources().getColor(R.color.hint_text));
                mBusiness.setTextColor(getResources().getColor(R.color.hint_text));
                mEducation.setTextColor(getResources().getColor(R.color.hint_text));
                mMovies.setTextColor(getResources().getColor(R.color.hint_text));
                mEntertainment.setTextColor(getResources().getColor(R.color.hint_text));
                mGames.setTextColor(getResources().getColor(R.color.hint_text));
                mMore.setTextColor(getResources().getColor(R.color.hint_text));

                mClicked = mMusic.getText().toString();
            }
        });

        mProgramming.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mScience.setTextColor(getResources().getColor(R.color.hint_text));
                mTech.setTextColor(getResources().getColor(R.color.hint_text));
                mRomance.setTextColor(getResources().getColor(R.color.hint_text));
                mMusic.setTextColor(getResources().getColor(R.color.hint_text));
                mProgramming.setTextColor(getResources().getColor(R.color.colorAccent));
                mLifestyle.setTextColor(getResources().getColor(R.color.hint_text));
                mArt.setTextColor(getResources().getColor(R.color.hint_text));
                mUrban.setTextColor(getResources().getColor(R.color.hint_text));
                mNature.setTextColor(getResources().getColor(R.color.hint_text));
                mSports.setTextColor(getResources().getColor(R.color.hint_text));
                mBusiness.setTextColor(getResources().getColor(R.color.hint_text));
                mEducation.setTextColor(getResources().getColor(R.color.hint_text));
                mMovies.setTextColor(getResources().getColor(R.color.hint_text));
                mEntertainment.setTextColor(getResources().getColor(R.color.hint_text));
                mGames.setTextColor(getResources().getColor(R.color.hint_text));
                mMore.setTextColor(getResources().getColor(R.color.hint_text));

                mClicked = mProgramming.getText().toString();
            }
        });

        mLifestyle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mScience.setTextColor(getResources().getColor(R.color.hint_text));
                mTech.setTextColor(getResources().getColor(R.color.hint_text));
                mRomance.setTextColor(getResources().getColor(R.color.hint_text));
                mMusic.setTextColor(getResources().getColor(R.color.hint_text));
                mProgramming.setTextColor(getResources().getColor(R.color.hint_text));
                mLifestyle.setTextColor(getResources().getColor(R.color.colorAccent));
                mArt.setTextColor(getResources().getColor(R.color.hint_text));
                mUrban.setTextColor(getResources().getColor(R.color.hint_text));
                mNature.setTextColor(getResources().getColor(R.color.hint_text));
                mSports.setTextColor(getResources().getColor(R.color.hint_text));
                mBusiness.setTextColor(getResources().getColor(R.color.hint_text));
                mEducation.setTextColor(getResources().getColor(R.color.hint_text));
                mMovies.setTextColor(getResources().getColor(R.color.hint_text));
                mEntertainment.setTextColor(getResources().getColor(R.color.hint_text));
                mGames.setTextColor(getResources().getColor(R.color.hint_text));
                mMore.setTextColor(getResources().getColor(R.color.hint_text));

                mClicked = mLifestyle.getText().toString();
            }
        });

        mArt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mScience.setTextColor(getResources().getColor(R.color.hint_text));
                mTech.setTextColor(getResources().getColor(R.color.hint_text));
                mRomance.setTextColor(getResources().getColor(R.color.hint_text));
                mMusic.setTextColor(getResources().getColor(R.color.hint_text));
                mProgramming.setTextColor(getResources().getColor(R.color.hint_text));
                mLifestyle.setTextColor(getResources().getColor(R.color.hint_text));
                mArt.setTextColor(getResources().getColor(R.color.colorAccent));
                mUrban.setTextColor(getResources().getColor(R.color.hint_text));
                mNature.setTextColor(getResources().getColor(R.color.hint_text));
                mSports.setTextColor(getResources().getColor(R.color.hint_text));
                mBusiness.setTextColor(getResources().getColor(R.color.hint_text));
                mEducation.setTextColor(getResources().getColor(R.color.hint_text));
                mMovies.setTextColor(getResources().getColor(R.color.hint_text));
                mEntertainment.setTextColor(getResources().getColor(R.color.hint_text));
                mGames.setTextColor(getResources().getColor(R.color.hint_text));
                mMore.setTextColor(getResources().getColor(R.color.hint_text));

                mClicked = mArt.getText().toString();
            }
        });

        mUrban.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mScience.setTextColor(getResources().getColor(R.color.hint_text));
                mTech.setTextColor(getResources().getColor(R.color.hint_text));
                mRomance.setTextColor(getResources().getColor(R.color.hint_text));
                mMusic.setTextColor(getResources().getColor(R.color.hint_text));
                mProgramming.setTextColor(getResources().getColor(R.color.hint_text));
                mLifestyle.setTextColor(getResources().getColor(R.color.hint_text));
                mArt.setTextColor(getResources().getColor(R.color.hint_text));
                mUrban.setTextColor(getResources().getColor(R.color.colorAccent));
                mNature.setTextColor(getResources().getColor(R.color.hint_text));
                mSports.setTextColor(getResources().getColor(R.color.hint_text));
                mBusiness.setTextColor(getResources().getColor(R.color.hint_text));
                mEducation.setTextColor(getResources().getColor(R.color.hint_text));
                mMovies.setTextColor(getResources().getColor(R.color.hint_text));
                mEntertainment.setTextColor(getResources().getColor(R.color.hint_text));
                mGames.setTextColor(getResources().getColor(R.color.hint_text));
                mMore.setTextColor(getResources().getColor(R.color.hint_text));

                mClicked = mUrban.getText().toString();
            }
        });

        mNature.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mScience.setTextColor(getResources().getColor(R.color.hint_text));
                mTech.setTextColor(getResources().getColor(R.color.hint_text));
                mRomance.setTextColor(getResources().getColor(R.color.hint_text));
                mMusic.setTextColor(getResources().getColor(R.color.hint_text));
                mProgramming.setTextColor(getResources().getColor(R.color.hint_text));
                mLifestyle.setTextColor(getResources().getColor(R.color.hint_text));
                mArt.setTextColor(getResources().getColor(R.color.hint_text));
                mUrban.setTextColor(getResources().getColor(R.color.hint_text));
                mNature.setTextColor(getResources().getColor(R.color.colorAccent));
                mSports.setTextColor(getResources().getColor(R.color.hint_text));
                mBusiness.setTextColor(getResources().getColor(R.color.hint_text));
                mEducation.setTextColor(getResources().getColor(R.color.hint_text));
                mMovies.setTextColor(getResources().getColor(R.color.hint_text));
                mEntertainment.setTextColor(getResources().getColor(R.color.hint_text));
                mGames.setTextColor(getResources().getColor(R.color.hint_text));
                mMore.setTextColor(getResources().getColor(R.color.hint_text));

                mClicked = mNature.getText().toString();
            }
        });

        mSports.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mScience.setTextColor(getResources().getColor(R.color.hint_text));
                mTech.setTextColor(getResources().getColor(R.color.hint_text));
                mRomance.setTextColor(getResources().getColor(R.color.hint_text));
                mMusic.setTextColor(getResources().getColor(R.color.hint_text));
                mProgramming.setTextColor(getResources().getColor(R.color.hint_text));
                mLifestyle.setTextColor(getResources().getColor(R.color.hint_text));
                mArt.setTextColor(getResources().getColor(R.color.hint_text));
                mUrban.setTextColor(getResources().getColor(R.color.hint_text));
                mNature.setTextColor(getResources().getColor(R.color.hint_text));
                mSports.setTextColor(getResources().getColor(R.color.colorAccent));
                mBusiness.setTextColor(getResources().getColor(R.color.hint_text));
                mEducation.setTextColor(getResources().getColor(R.color.hint_text));
                mMovies.setTextColor(getResources().getColor(R.color.hint_text));
                mEntertainment.setTextColor(getResources().getColor(R.color.hint_text));
                mGames.setTextColor(getResources().getColor(R.color.hint_text));
                mMore.setTextColor(getResources().getColor(R.color.hint_text));

                mClicked = mSports.getText().toString();
            }
        });

        mBusiness.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mScience.setTextColor(getResources().getColor(R.color.hint_text));
                mTech.setTextColor(getResources().getColor(R.color.hint_text));
                mRomance.setTextColor(getResources().getColor(R.color.hint_text));
                mMusic.setTextColor(getResources().getColor(R.color.hint_text));
                mProgramming.setTextColor(getResources().getColor(R.color.hint_text));
                mLifestyle.setTextColor(getResources().getColor(R.color.hint_text));
                mArt.setTextColor(getResources().getColor(R.color.hint_text));
                mUrban.setTextColor(getResources().getColor(R.color.hint_text));
                mNature.setTextColor(getResources().getColor(R.color.hint_text));
                mSports.setTextColor(getResources().getColor(R.color.hint_text));
                mBusiness.setTextColor(getResources().getColor(R.color.colorAccent));
                mEducation.setTextColor(getResources().getColor(R.color.hint_text));
                mMovies.setTextColor(getResources().getColor(R.color.hint_text));
                mEntertainment.setTextColor(getResources().getColor(R.color.hint_text));
                mGames.setTextColor(getResources().getColor(R.color.hint_text));
                mMore.setTextColor(getResources().getColor(R.color.hint_text));

                mClicked = mBusiness.getText().toString();
            }
        });

        mEducation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mScience.setTextColor(getResources().getColor(R.color.hint_text));
                mTech.setTextColor(getResources().getColor(R.color.hint_text));
                mRomance.setTextColor(getResources().getColor(R.color.hint_text));
                mMusic.setTextColor(getResources().getColor(R.color.hint_text));
                mProgramming.setTextColor(getResources().getColor(R.color.hint_text));
                mLifestyle.setTextColor(getResources().getColor(R.color.hint_text));
                mArt.setTextColor(getResources().getColor(R.color.hint_text));
                mUrban.setTextColor(getResources().getColor(R.color.hint_text));
                mNature.setTextColor(getResources().getColor(R.color.hint_text));
                mSports.setTextColor(getResources().getColor(R.color.hint_text));
                mBusiness.setTextColor(getResources().getColor(R.color.hint_text));
                mEducation.setTextColor(getResources().getColor(R.color.colorAccent));
                mMovies.setTextColor(getResources().getColor(R.color.hint_text));
                mEntertainment.setTextColor(getResources().getColor(R.color.hint_text));
                mGames.setTextColor(getResources().getColor(R.color.hint_text));
                mMore.setTextColor(getResources().getColor(R.color.hint_text));

                mClicked = mEducation.getText().toString();
            }
        });

        mMovies.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mScience.setTextColor(getResources().getColor(R.color.hint_text));
                mTech.setTextColor(getResources().getColor(R.color.hint_text));
                mRomance.setTextColor(getResources().getColor(R.color.hint_text));
                mMusic.setTextColor(getResources().getColor(R.color.hint_text));
                mProgramming.setTextColor(getResources().getColor(R.color.hint_text));
                mLifestyle.setTextColor(getResources().getColor(R.color.hint_text));
                mArt.setTextColor(getResources().getColor(R.color.hint_text));
                mUrban.setTextColor(getResources().getColor(R.color.hint_text));
                mNature.setTextColor(getResources().getColor(R.color.hint_text));
                mSports.setTextColor(getResources().getColor(R.color.hint_text));
                mBusiness.setTextColor(getResources().getColor(R.color.hint_text));
                mEducation.setTextColor(getResources().getColor(R.color.hint_text));
                mMovies.setTextColor(getResources().getColor(R.color.colorAccent));
                mEntertainment.setTextColor(getResources().getColor(R.color.hint_text));
                mGames.setTextColor(getResources().getColor(R.color.hint_text));
                mMore.setTextColor(getResources().getColor(R.color.hint_text));

                mClicked = mMovies.getText().toString();
            }
        });

        mEntertainment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mScience.setTextColor(getResources().getColor(R.color.hint_text));
                mTech.setTextColor(getResources().getColor(R.color.hint_text));
                mRomance.setTextColor(getResources().getColor(R.color.hint_text));
                mMusic.setTextColor(getResources().getColor(R.color.hint_text));
                mProgramming.setTextColor(getResources().getColor(R.color.hint_text));
                mLifestyle.setTextColor(getResources().getColor(R.color.hint_text));
                mArt.setTextColor(getResources().getColor(R.color.hint_text));
                mUrban.setTextColor(getResources().getColor(R.color.hint_text));
                mNature.setTextColor(getResources().getColor(R.color.hint_text));
                mSports.setTextColor(getResources().getColor(R.color.hint_text));
                mBusiness.setTextColor(getResources().getColor(R.color.hint_text));
                mEducation.setTextColor(getResources().getColor(R.color.hint_text));
                mMovies.setTextColor(getResources().getColor(R.color.hint_text));
                mEntertainment.setTextColor(getResources().getColor(R.color.colorAccent));
                mGames.setTextColor(getResources().getColor(R.color.hint_text));
                mMore.setTextColor(getResources().getColor(R.color.hint_text));

                mClicked = mEntertainment.getText().toString();
            }
        });


        mGames.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mScience.setTextColor(getResources().getColor(R.color.hint_text));
                mTech.setTextColor(getResources().getColor(R.color.hint_text));
                mRomance.setTextColor(getResources().getColor(R.color.hint_text));
                mMusic.setTextColor(getResources().getColor(R.color.hint_text));
                mProgramming.setTextColor(getResources().getColor(R.color.hint_text));
                mLifestyle.setTextColor(getResources().getColor(R.color.hint_text));
                mArt.setTextColor(getResources().getColor(R.color.hint_text));
                mUrban.setTextColor(getResources().getColor(R.color.hint_text));
                mNature.setTextColor(getResources().getColor(R.color.hint_text));
                mSports.setTextColor(getResources().getColor(R.color.hint_text));
                mBusiness.setTextColor(getResources().getColor(R.color.hint_text));
                mEducation.setTextColor(getResources().getColor(R.color.hint_text));
                mMovies.setTextColor(getResources().getColor(R.color.hint_text));
                mEntertainment.setTextColor(getResources().getColor(R.color.hint_text));
                mGames.setTextColor(getResources().getColor(R.color.colorAccent));
                mMore.setTextColor(getResources().getColor(R.color.hint_text));

                mClicked = mGames.getText().toString();
            }
        });

        mMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mScience.setTextColor(getResources().getColor(R.color.hint_text));
                mTech.setTextColor(getResources().getColor(R.color.hint_text));
                mRomance.setTextColor(getResources().getColor(R.color.hint_text));
                mMusic.setTextColor(getResources().getColor(R.color.hint_text));
                mProgramming.setTextColor(getResources().getColor(R.color.hint_text));
                mLifestyle.setTextColor(getResources().getColor(R.color.hint_text));
                mArt.setTextColor(getResources().getColor(R.color.hint_text));
                mUrban.setTextColor(getResources().getColor(R.color.hint_text));
                mNature.setTextColor(getResources().getColor(R.color.hint_text));
                mSports.setTextColor(getResources().getColor(R.color.hint_text));
                mBusiness.setTextColor(getResources().getColor(R.color.hint_text));
                mEducation.setTextColor(getResources().getColor(R.color.hint_text));
                mMovies.setTextColor(getResources().getColor(R.color.hint_text));
                mEntertainment.setTextColor(getResources().getColor(R.color.hint_text));
                mGames.setTextColor(getResources().getColor(R.color.hint_text));
                mMore.setTextColor(getResources().getColor(R.color.colorAccent));

                mClicked = mMore.getText().toString();
            }
        });

        //============================== Functions ===========================================
        try {
            if (getActivity() != null && mAuth.getCurrentUser() != null) {
                currentUserId = mAuth.getCurrentUser().getUid();

                firebaseFirestore.collection("Media").document("Information")
                        .addSnapshotListener(getActivity(), new EventListener<DocumentSnapshot>() {
                            @Override
                            public void onEvent(@Nullable DocumentSnapshot documentSnapshot,
                                                @Nullable FirebaseFirestoreException e) {
                                if (documentSnapshot != null) {
                                    if (documentSnapshot.exists()) {
                                        amount = documentSnapshot.getString("amt");
                                    }
                                }
                            }
                        });

                mcontentField.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        String text = mcontentField.getText().toString();
                        text = text.replace("\n", "");
                        //String[] textArray = text.split(" ");
                        mCharCount.setText(text.toCharArray().length + "/150");
                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                    }
                });

                mContinueBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String content = mcontentField.getText().toString();
                        final String category = mClicked;

                        if (mClicked == null || TextUtils.isEmpty(mClicked)) {
                            Toast.makeText(getActivity(), getResources().getString(R.string.no_category), Toast.LENGTH_SHORT).show();
                        } else {
                            if (TextUtils.isEmpty(content)) {
                                Toast.makeText(getActivity(), getResources().getString(R.string.no_question), Toast.LENGTH_SHORT).show();
                            } else {
                                if (!networkConnection.isConnected(getActivity())) {
                                    Toast.makeText(getActivity(), getResources().getString(R.string.no_internet), Toast.LENGTH_SHORT).show();
                                } else {

                                    if (amount == null || TextUtils.isEmpty(amount)){
                                        amount = "2";
                                    }

                                    AlertDialog.Builder update_builder = new AlertDialog.Builder(getActivity());
                                    update_builder
                                            .setMessage(getResources().getString(R.string.stack_text) + " "
                                            + getResources().getString(R.string.dollar_sign) + amount)
                                            .setPositiveButton(getResources().getString(R.string.push), new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    stackPay(content, category, amount);
                                                }
                                            }).setNegativeButton(getResources().getString(R.string.post), new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            dialogInterface.dismiss();
                                            continuePost(content, category, "0");
                                        }
                                    });

                                    AlertDialog alert_update = update_builder.create();
                                    alert_update.show();
                                }
                            }
                        }
                    }
                });

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return view;
    }

    private void stackPay(String content, String category, String amt) {
        Intent payIntent = new Intent(getActivity(), PayActivity.class);
        payIntent.putExtra("content", content);
        payIntent.putExtra("category", category);
        payIntent.putExtra("amt", amt);
        startActivity(payIntent);
        Objects.requireNonNull(getActivity()).overridePendingTransition(R.anim.slide_in_right, R.anim.no_anim);
    }

    private void continuePost(String content, String category, String amt) {
        mContinueBtn.setEnabled(false);
        mPulse.start();

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
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                        mClicked = null;
                        mcontentField.setText(null);
                        mCharCount.setText("0/150");
                        mContinueBtn.setEnabled(true);
                        mPulse.stop();
                        mPulse.setVisibility(View.GONE);

                        mScience.setTextColor(getResources().getColor(R.color.hint_text));
                        mTech.setTextColor(getResources().getColor(R.color.hint_text));
                        mRomance.setTextColor(getResources().getColor(R.color.hint_text));
                        mMusic.setTextColor(getResources().getColor(R.color.hint_text));
                        mProgramming.setTextColor(getResources().getColor(R.color.hint_text));
                        mLifestyle.setTextColor(getResources().getColor(R.color.hint_text));
                        mArt.setTextColor(getResources().getColor(R.color.hint_text));
                        mUrban.setTextColor(getResources().getColor(R.color.hint_text));
                        mNature.setTextColor(getResources().getColor(R.color.hint_text));
                        mSports.setTextColor(getResources().getColor(R.color.hint_text));
                        mEducation.setTextColor(getResources().getColor(R.color.hint_text));
                        mMovies.setTextColor(getResources().getColor(R.color.hint_text));
                        mEntertainment.setTextColor(getResources().getColor(R.color.hint_text));
                        mGames.setTextColor(getResources().getColor(R.color.hint_text));
                        mMore.setTextColor(getResources().getColor(R.color.hint_text));

                        Toast.makeText(getActivity(), getResources().getString(R.string.post_created), Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getActivity(), getResources().getString(R.string.err) +
                        e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Fragment fragment = Objects.requireNonNull(getActivity()).getSupportFragmentManager().findFragmentByTag(this.getClass().getName());
        if (fragment != null){
            getActivity().getSupportFragmentManager().beginTransaction().remove(fragment).commit();
        }
    }

}
