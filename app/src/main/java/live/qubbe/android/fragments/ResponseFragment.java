package live.qubbe.android.fragments;


import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.LinkedList;
import java.util.List;

import live.qubbe.android.R;
import live.qubbe.android.adapters.ResponseAdapter;
import live.qubbe.android.models.ResponseModel;
import live.qubbe.android.utils.NetworkConnection;

/**
 * A simple {@link Fragment} subclass.
 */
public class ResponseFragment extends Fragment {

    private View view;
    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth mAuth;
    private String userId;
    private RecyclerView mResponseList;

    private LinkedList<ResponseModel> responseModelList;
    private ResponseAdapter responseAdapter;
    private DocumentSnapshot lastVisible;
    private Boolean isFirstPageFirstLoad = true;
    private TextView mNoResponse;
    private String postId, posterId;
    private NetworkConnection networkConnection;

    public ResponseFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (this.getArguments() != null) {
            postId = this.getArguments().getString("postId");
            posterId = this.getArguments().getString("posterId");
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        if (view == null) {
            view = inflater.inflate(R.layout.fragment_response, container, false);

            firebaseFirestore = FirebaseFirestore.getInstance();
            mAuth = FirebaseAuth.getInstance();
            userId = mAuth.getCurrentUser().getUid();
            networkConnection = new NetworkConnection();

            mResponseList = view.findViewById(R.id.response_list);
            responseModelList = new LinkedList<>();

            responseAdapter = new ResponseAdapter(responseModelList);
            mResponseList.setLayoutManager(new LinearLayoutManager(getActivity()));
            mResponseList.setHasFixedSize(true);
            mResponseList.setAdapter(responseAdapter);

            mNoResponse = view.findViewById(R.id.firstResponseText);

            try {
                //============================ Load Data ===================================================
                if (getActivity() != null && postId != null && posterId != null) {

                    mResponseList.addOnScrollListener(new RecyclerView.OnScrollListener() {
                        @Override
                        public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                            super.onScrolled(recyclerView, dx, dy);
                            boolean reachedBottom = !recyclerView.canScrollVertically(1);
                            if (reachedBottom) {
                                loadMorePost();
                            }
                        }
                    });

                    firebaseFirestore.collection("All")
                            .document(postId).collection("Response")
                            .get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                        @Override
                        public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                            int size = queryDocumentSnapshots.size();
                            if (size == 0) {
                                try {
                                    mNoResponse.setVisibility(View.VISIBLE);
                                    mNoResponse.setText(getResources().getString(R.string.be_the_first_to_respond));
                                } catch (NullPointerException e) {
                                    e.printStackTrace();
                                }
                            } else {
                                mNoResponse.setVisibility(View.GONE);
                            }
                        }
                    });

                    Query firstQuery = firebaseFirestore.collection("All")
                            .document(postId).collection("Response")
                            .orderBy("utc", Query.Direction.DESCENDING).limit(15);
                    firstQuery.addSnapshotListener(new EventListener<QuerySnapshot>() {
                        @Override
                        public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {

                            if (queryDocumentSnapshots != null) {

                                if (!queryDocumentSnapshots.isEmpty()) {
                                    if (isFirstPageFirstLoad) {
                                        lastVisible = queryDocumentSnapshots.getDocuments().get(queryDocumentSnapshots.size() - 1);
                                        responseModelList.clear();
                                    }

                                    for (@NonNull DocumentChange doc : queryDocumentSnapshots.getDocumentChanges()) {
                                        if (doc.getType() == DocumentChange.Type.ADDED) {
                                            mNoResponse.setVisibility(View.GONE);

                                            String docId = doc.getDocument().getId();
                                            final ResponseModel responseModel = doc.getDocument().toObject(ResponseModel.class).withId(docId);

                                            if (isFirstPageFirstLoad) {
                                                responseModelList.add(responseModel);
                                            } else {
                                                responseModelList.add(0, responseModel);
                                            }
                                            responseAdapter.notifyDataSetChanged();

                                        }
                                    }
                                    isFirstPageFirstLoad = false;
                                }
                            }
                        }
                    });

                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return view;
    }

    private void loadMorePost() {
        try {
            if (getActivity() != null) {
                if (networkConnection.isConnected(getActivity())) {
                    Query nextQuery = firebaseFirestore.collection("All")
                            .document(postId).collection("Response")
                            .orderBy("utc", Query.Direction.DESCENDING)
                            .startAfter(lastVisible).limit(15);
                    nextQuery.addSnapshotListener(new EventListener<QuerySnapshot>() {
                        @Override
                        public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                            if (queryDocumentSnapshots != null) {
                                if (!queryDocumentSnapshots.isEmpty()) {
                                    lastVisible = queryDocumentSnapshots.getDocuments().get(queryDocumentSnapshots.size() - 1);

                                    for (@NonNull DocumentChange doc : queryDocumentSnapshots.getDocumentChanges()) {
                                        if (doc.getType() == DocumentChange.Type.ADDED) {

                                            String docId = doc.getDocument().getId();
                                            @NonNull final ResponseModel responseModel = doc.getDocument().toObject(ResponseModel.class)
                                                    .withId(docId);

                                            responseModelList.add(responseModel);
                                            responseAdapter.notifyDataSetChanged();

                                        }
                                    }
                                }
                            }
                        }
                    });

                }else {
                    Toast.makeText(getActivity(), getResources().getString(R.string.no_internet),
                            Toast.LENGTH_SHORT).show();
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
