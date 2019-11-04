package live.qubbe.android.fragments;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.yuyakaido.android.cardstackview.CardStackView;
import com.yuyakaido.android.cardstackview.SwipeDirection;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import live.qubbe.android.R;
import live.qubbe.android.adapters.CardAdapter;
import live.qubbe.android.models.CardModel;
import live.qubbe.android.utils.InputKeyboardMethod;
import live.qubbe.android.utils.NetworkConnection;
import pl.bclogic.pulsator4droid.library.PulsatorLayout;

/**
 * A simple {@link Fragment} subclass.
 */
public class MainFragment extends Fragment {

    private View view;
    private String category;
    private PulsatorLayout mPulse;
    private CardStackView cardStackView;
    private CardAdapter adapter;
    private FloatingActionButton mReverse, mPassLeft, mPassRight, mRefresh;
    private EditText mSearchField;
    private ImageView mClearBtn, mSearchBtn;

    private FirebaseAuth mAuth;
    private FirebaseFirestore firebaseFirestore;
    private CardView mCardDetails;
    private String currentUserId, cardType;
    private TextView mCardSwipeText;
    private NetworkConnection networkConnection;
    private InputKeyboardMethod inputKeyboardMethod;

    public MainFragment() {
        // Required empty public constructor
    }

    public void passCategory(String cat) {
        category = cat;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_main, container, false);
        mAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();
        networkConnection = new NetworkConnection();
        inputKeyboardMethod = new InputKeyboardMethod();

        cardStackView = view.findViewById(R.id.card_stack_view);
        mPulse = view.findViewById(R.id.cardsPulse);
        mReverse = view.findViewById(R.id.reverseBtn);
        mPassLeft = view.findViewById(R.id.leftBtn);
        mPassRight = view.findViewById(R.id.rightBtn);
        mRefresh = view.findViewById(R.id.refreshBtn);
        mSearchField = view.findViewById(R.id.searchField);
        mSearchBtn = view.findViewById(R.id.search_btn);
        mClearBtn = view.findViewById(R.id.clearBtn);
        mCardDetails = view.findViewById(R.id.cardSwipe);
        mCardSwipeText = view.findViewById(R.id.cardSwipeText);
        cardType = "Category";

        //============================= Functions ===========================================
        try {
            if (mAuth.getCurrentUser() != null && getActivity() != null && !TextUtils.isEmpty(category)) {
                currentUserId = mAuth.getCurrentUser().getUid();

                mCardDetails.setVisibility(View.GONE);

                mReverse.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (networkConnection.isConnected(getActivity())) {
                            reverse();
                        }
                    }
                });

                mPassLeft.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (networkConnection.isConnected(getActivity())) {
                            swipeLeft();
                        }
                    }
                });

                mPassRight.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (networkConnection.isConnected(getActivity())) {
                            swipeRight();
                        }
                    }
                });

                mRefresh.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        loadCheck();
                    }
                });

                mSearchBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String text = mSearchField.getText().toString();
                        if (!TextUtils.isEmpty(text)) {
                            if (!networkConnection.isConnected(getActivity())) {
                                Toast.makeText(getActivity(), getResources().getString(R.string.no_internet), Toast.LENGTH_SHORT).show();
                            } else {
                                cardType = "Search";
                                reload(cardType, text.toLowerCase().trim());
                                inputKeyboardMethod.hideKeyboardFrom(getContext(), view);
                            }
                        } else {
                            Toast.makeText(getActivity(), getResources().getString(R.string.type_question),
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });

                mSearchField.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        String text = mSearchField.getText().toString();
                        text = text.replace("\n", "");
                        //String[] textArray = text.split(" ");
                        if (text.toCharArray().length > 0) {
                            mClearBtn.setVisibility(View.VISIBLE);
                        }
                    }

                    @Override
                    public void afterTextChanged(Editable s) {

                    }
                });

                mClearBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        mSearchField.setText("");
                        cardType = "Category";

                        inputKeyboardMethod.hideKeyboardFrom(getContext(), view);
                        loadCheck();
                        mClearBtn.setVisibility(View.GONE);
                    }
                });
                setup();
                mPulse.start();
                loadCheck();
                cardPaginate();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return view;
    }

    private void loadCheck() {
        if (getActivity() != null) {
            if (!networkConnection.isConnected(getActivity())) {
                cardStackView.setVisibility(View.GONE);
                mCardDetails.setVisibility(View.VISIBLE);
                mCardSwipeText.setText(getResources().getText(R.string.no_internet));
                mPulse.stop();

            } else {
                reload(cardType, null);
                mCardDetails.setVisibility(View.GONE);
            }
        }
    }

    private void setup() {
        //Add progress
        cardStackView.setCardEventListener(new CardStackView.CardEventListener() {
            @Override
            public void onCardDragging(float percentX, float percentY) {
                Log.d("CardStackView", "onCardDragging");
            }

            @Override
            public void onCardSwiped(SwipeDirection direction) {
                if (cardStackView.getTopIndex() == adapter.getCount() - 5) {
                    Log.d("CardStackView", "Paginate: " + cardStackView.getTopIndex());
                    paginate();
                }
                if (cardStackView == null) {
                    //cardStackView.setVisibility(View.GONE);
                    mCardDetails.setVisibility(View.VISIBLE);
                    mCardSwipeText.setText(getResources().getText(R.string.no_questions));
                }
            }

            @Override
            public void onCardReversed() {
                Log.d("CardStackView", "onCardReversed");
            }

            @Override
            public void onCardMovedToOrigin() {
                Log.d("CardStackView", "onCardMovedToOrigin");
            }

            @Override
            public void onCardClicked(int index) {
                Log.d("CardStackView", "onCardClicked: " + index);
            }
        });
    }

    private void reload(String type, String doc) {
        if (getContext() != null) {
            mPulse.start();
            cardStackView.setVisibility(View.GONE);
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    adapter = createCards(type, doc);
                    if (getContext() != null) {
                        cardStackView.setAdapter(adapter);
                    }
                    cardStackView.setVisibility(View.VISIBLE);
                }
            }, 1000);
        }
    }

    private LinkedList<CardModel> extractRemainingCardModels() {
        LinkedList<CardModel> spots = new LinkedList<>();
        for (int i = cardStackView.getTopIndex(); i < adapter.getCount(); i++) {
            spots.add(adapter.getItem(i));
        }
        return spots;
    }

    private CardAdapter createCards(String type, String doc) {
        if (getActivity() != null) {
            adapter = new CardAdapter(getActivity());

            Query dexQuery;
            if (type.equals("Search")) {
                dexQuery = firebaseFirestore.collection("All")
                        .orderBy("contentTextLower")
                        .startAt(doc).endAt(doc + "\uf8ff");
            } else {
                final String val = "amt";
                /*if (category.equals("All")){
                    val = "utc";
                } */

                dexQuery = firebaseFirestore.collection(category).orderBy(val,
                        Query.Direction.DESCENDING);
            }

            dexQuery.addSnapshotListener(getActivity(), new EventListener<QuerySnapshot>() {
                @Override
                public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots,
                                    @Nullable FirebaseFirestoreException e) {

                    if (queryDocumentSnapshots != null) {
                        if (!queryDocumentSnapshots.isEmpty()) {
                            for (DocumentChange doc : queryDocumentSnapshots.getDocumentChanges()) {
                                if (doc.getType() == DocumentChange.Type.ADDED) {

                                    mCardDetails.setVisibility(View.GONE);
                                    String docId = doc.getDocument().getId();

                                    final CardModel CardModel = doc.getDocument().toObject(CardModel.class)
                                            .withId(docId);
                                    mCardDetails.setVisibility(View.GONE);
                                    adapter.add(CardModel);
                                    adapter.notifyDataSetChanged();
                                    mPulse.stop();
                                }
                            }
                        } else {
                            mPulse.stop();
                            mCardDetails.setVisibility(View.VISIBLE);
                            if (type.equals("Search")) {
                                mCardSwipeText.setText(getResources().getText(R.string.no_question_found));
                            } else {
                                mCardSwipeText.setText(getResources().getText(R.string.no_quest));
                            }
                        }
                    }
                }
            });
        }
        return adapter;
    }
  /*  private void addFirst() {
        LinkedList<CardModel> spots = extractRemainingCardModels();
        spots.addFirst(createCardModel());
        adapter.clear();
        adapter.addAll(spots);
        adapter.notifyDataSetChanged();
    }

    private void addLast() {
        LinkedList<CardModel> spots = extractRemainingCardModels();
        spots.addLast(createCardModel());
        adapter.clear();
        adapter.addAll(spots);
        adapter.notifyDataSetChanged();
    }

    private void removeFirst() {
        LinkedList<CardModel> spots = extractRemainingCardModels();
        if (spots.isEmpty()) {
            return;
        }

        spots.removeFirst();
        adapter.clear();
        adapter.addAll(spots);
        adapter.notifyDataSetChanged();
    }

    private void removeLast() {
        LinkedList<CardModel> spots = extractRemainingCardModels();
        if (spots.isEmpty()) {
            return;
        }

        spots.removeLast();
        adapter.clear();
        adapter.addAll(spots);
        adapter.notifyDataSetChanged();
    } */

    private void swipeLeft() {

        List<CardModel> spots = extractRemainingCardModels();
        if (spots.isEmpty()) {
            return;
        }

        View target = cardStackView.getTopView();
        View targetOverlay = cardStackView.getTopView().getOverlayContainer();

        ValueAnimator rotation = ObjectAnimator.ofPropertyValuesHolder(
                target, PropertyValuesHolder.ofFloat("rotation", -10f));
        rotation.setDuration(200);
        ValueAnimator translateX = ObjectAnimator.ofPropertyValuesHolder(
                target, PropertyValuesHolder.ofFloat("translationX", 0f, -2000f));
        ValueAnimator translateY = ObjectAnimator.ofPropertyValuesHolder(
                target, PropertyValuesHolder.ofFloat("translationY", 0f, 500f));
        translateX.setStartDelay(100);
        translateY.setStartDelay(100);
        translateX.setDuration(500);
        translateY.setDuration(500);
        AnimatorSet cardAnimationSet = new AnimatorSet();
        cardAnimationSet.playTogether(rotation, translateX, translateY);

        ObjectAnimator overlayAnimator = ObjectAnimator.ofFloat(targetOverlay, "alpha", 0f, 1f);
        overlayAnimator.setDuration(200);
        AnimatorSet overlayAnimationSet = new AnimatorSet();
        overlayAnimationSet.playTogether(overlayAnimator);

        cardStackView.swipe(SwipeDirection.Left, cardAnimationSet, overlayAnimationSet);
        //cardPaginate();
        checkStack();
    }

    private void swipeRight() {

        if (getActivity() != null) {
            List<CardModel> spots = extractRemainingCardModels();
            if (spots.isEmpty()) {
                return;
            }

            View target = cardStackView.getTopView();
            View targetOverlay = cardStackView.getTopView().getOverlayContainer();

            ValueAnimator rotation = ObjectAnimator.ofPropertyValuesHolder(
                    target, PropertyValuesHolder.ofFloat("rotation", 10f));
            rotation.setDuration(200);
            ValueAnimator translateX = ObjectAnimator.ofPropertyValuesHolder(
                    target, PropertyValuesHolder.ofFloat("translationX", 0f, 2000f));
            ValueAnimator translateY = ObjectAnimator.ofPropertyValuesHolder(
                    target, PropertyValuesHolder.ofFloat("translationY", 0f, 500f));
            translateX.setStartDelay(100);
            translateY.setStartDelay(100);
            translateX.setDuration(500);
            translateY.setDuration(500);
            AnimatorSet cardAnimationSet = new AnimatorSet();
            cardAnimationSet.playTogether(rotation, translateX, translateY);

            ObjectAnimator overlayAnimator = ObjectAnimator.ofFloat(targetOverlay, "alpha", 0f, 1f);
            overlayAnimator.setDuration(200);
            AnimatorSet overlayAnimationSet = new AnimatorSet();
            overlayAnimationSet.playTogether(overlayAnimator);

            cardStackView.swipe(SwipeDirection.Right, cardAnimationSet, overlayAnimationSet);
            //cardPaginate();
            checkStack();
        }
    }

    private void cardPaginate() {
        cardStackView.setCardEventListener(new CardStackView.CardEventListener() {
            @Override
            public void onCardDragging(float percentX, float percentY) {
                Log.d("CardStackView", "onCardDragging");
            }

            @Override
            public void onCardSwiped(SwipeDirection direction) {
                Log.d("CardStackView", "onCardSwiped: " + direction.toString());
                Log.d("CardStackView", "topIndex: " + cardStackView.getTopIndex());
                if (cardStackView.getTopIndex() == adapter.getCount() - 5) {
                    Log.d("CardStackView", "Paginate: " + cardStackView.getTopIndex());
                    paginate();
                }

                checkStack();
                /*if (cardStackView == null) {
                    //cardStackView.setVisibility(View.GONE);
                    mCardDetails.setVisibility(View.VISIBLE);
                    mCardSwipeText.setText(getResources().getText(R.string.no_questions));
                } */
            }

            @Override
            public void onCardReversed() {
                Log.d("CardStackView", "onCardReversed");
            }

            @Override
            public void onCardMovedToOrigin() {
                Log.d("CardStackView", "onCardMovedToOrigin");
            }

            @Override
            public void onCardClicked(int index) {
                Log.d("CardStackView", "onCardClicked: " + index);
            }
        });
    }

    private void checkStack() {
        LinkedList<CardModel> spots = extractRemainingCardModels();
        if (cardStackView == null) {
            mCardDetails.setVisibility(View.VISIBLE);
            mCardSwipeText.setText(getResources().getText(R.string.no_questions));

        } else if (spots.isEmpty()) {
            mCardDetails.setVisibility(View.VISIBLE);
            mCardSwipeText.setText(getResources().getText(R.string.no_questions));
        }
    }

    private void reverse() {
        mCardDetails.setVisibility(View.GONE);
        cardStackView.reverse();
    }

    private void paginate() {
        cardStackView.setPaginationReserved();
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
