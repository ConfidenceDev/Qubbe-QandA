package live.qubbe.android.activities;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.LinkedList;

import live.qubbe.android.R;
import live.qubbe.android.adapters.ContributorsAdapter;
import live.qubbe.android.adapters.ContributorsAdapter;
import live.qubbe.android.models.ContributorsModel;
import live.qubbe.android.models.MyPostsModel;
import live.qubbe.android.utils.NetworkConnection;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class ContributorsActivity extends AppCompatActivity {

    private FirebaseFirestore firebaseFirestore;

    private RecyclerView mContributorsList;
    private TextView mNoContributor;

    private LinkedList<ContributorsModel> contributorsModelLinkedList;
    private ContributorsAdapter contributorsAdapter;
    private Boolean isFirstPageFirstLoad = true;
    private ImageView mBackBtn;
    
    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contributors);

        firebaseFirestore = FirebaseFirestore.getInstance();

        mContributorsList = findViewById(R.id.contributorsList);
        mNoContributor = findViewById(R.id.noContributorsText);
        mBackBtn = findViewById(R.id.contributorsBack);

        contributorsModelLinkedList = new LinkedList<>();

        contributorsAdapter = new ContributorsAdapter(contributorsModelLinkedList);
        mContributorsList.setLayoutManager(new LinearLayoutManager(this));
        mContributorsList.setHasFixedSize(true);
        mContributorsList.setItemViewCacheSize(5);
        mContributorsList.setAdapter(contributorsAdapter);
        isFirstPageFirstLoad = true;

        mBackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
        //========================== Functions =============================================

        loadPosts();
    }

    private void loadPosts() {
        firebaseFirestore.collection("Contributors")
                .addSnapshotListener(this, new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@javax.annotation.Nullable QuerySnapshot queryDocumentSnapshots,
                                @javax.annotation.Nullable FirebaseFirestoreException e) {
                if (queryDocumentSnapshots != null) {
                    int count = queryDocumentSnapshots.size();
                    if (count == 0) {
                        mNoContributor.setVisibility(View.VISIBLE);
                    } else {
                        mNoContributor.setVisibility(View.GONE);
                    }
                }
            }
        });

        Query firstQuery = firebaseFirestore.collection("Contributors")
                .orderBy("utc", Query.Direction.DESCENDING).limit(10);
        firstQuery.addSnapshotListener(this, new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots,
                                @Nullable FirebaseFirestoreException e) {
                if (queryDocumentSnapshots != null) {

                    if (!queryDocumentSnapshots.isEmpty()) {
                        if (isFirstPageFirstLoad) {
                            contributorsModelLinkedList.clear();
                        }

                        for (@NonNull DocumentChange doc : queryDocumentSnapshots.getDocumentChanges()) {
                            if (doc.getType() == DocumentChange.Type.ADDED) {
                                String docId = doc.getDocument().getId();
                                final ContributorsModel contributorsModel = doc.getDocument()
                                        .toObject(ContributorsModel.class).withId(docId);

                                if (isFirstPageFirstLoad) {
                                    contributorsModelLinkedList.add(contributorsModel);
                                } else {
                                    contributorsModelLinkedList.add(0, contributorsModel);
                                }
                                contributorsAdapter.notifyDataSetChanged();

                            }
                        }
                        isFirstPageFirstLoad = false;
                    }
                }
            }
        });

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}
