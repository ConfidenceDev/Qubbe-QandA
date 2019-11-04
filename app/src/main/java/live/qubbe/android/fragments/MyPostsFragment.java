package live.qubbe.android.fragments;


import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.LinkedList;
import java.util.Objects;

import live.qubbe.android.R;
import live.qubbe.android.adapters.MyPostsAdapter;
import live.qubbe.android.models.MyPostsModel;
import live.qubbe.android.utils.NetworkConnection;

/**
 * A simple {@link Fragment} subclass.
 */
public class MyPostsFragment extends Fragment {

    private View view;
    private FirebaseAuth mAuth;
    private FirebaseFirestore firebaseFirestore;
    private String currentUserId;

    private SwipeRefreshLayout mSwipe;
    private RecyclerView mUploadList;
    private TextView mNoUploads;

    private LinkedList<MyPostsModel> myPostsModelLinkedList;
    private MyPostsAdapter myPostsAdapter;
    private DocumentSnapshot lastVisible;
    private Boolean isFirstPageFirstLoad = true;
    private NetworkConnection networkConnection;

    public MyPostsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_my_posts, container, false);

        mAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();
        networkConnection = new NetworkConnection();

        mSwipe = view.findViewById(R.id.uploadSwipe);
        mUploadList = view.findViewById(R.id.postsList);
        mNoUploads = view.findViewById(R.id.noPostsText);

        myPostsModelLinkedList = new LinkedList<>();

        myPostsAdapter = new MyPostsAdapter(myPostsModelLinkedList);
        mUploadList.setLayoutManager(new LinearLayoutManager(getActivity()));
        mUploadList.setHasFixedSize(true);
        mUploadList.setItemViewCacheSize(5);
        mUploadList.setAdapter(myPostsAdapter);
        isFirstPageFirstLoad = true;

        //============================ Functions ============================================
        try{
            if (getActivity() != null && mAuth.getCurrentUser() != null){
                currentUserId = mAuth.getCurrentUser().getUid();

                int RUN = 2000;
                mSwipe.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        if (networkConnection.isConnected(getActivity())) {
                            isFirstPageFirstLoad = true;
                            if (!myPostsModelLinkedList.isEmpty()) {
                                myPostsModelLinkedList.clear();
                                loadPosts();
                            } else {
                                loadPosts();
                            }
                        } else {
                            Toast.makeText(getActivity(), getResources()
                                    .getString(R.string.no_internet), Toast.LENGTH_SHORT).show();
                        }
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                mSwipe.setRefreshing(false);
                            }
                        }, RUN);
                    }
                });

                mUploadList.addOnScrollListener(new RecyclerView.OnScrollListener() {
                    @Override
                    public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                        super.onScrolled(recyclerView, dx, dy);
                        boolean reachedBottom = !recyclerView.canScrollVertically(1);
                        if (reachedBottom) {
                            loadMorePosts();
                        }
                    }
                });

                loadPosts();
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return view;
    }

    private void loadPosts() {
        firebaseFirestore.collection("Users").document(currentUserId)
                .collection("Posts").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@javax.annotation.Nullable QuerySnapshot queryDocumentSnapshots,
                                @javax.annotation.Nullable FirebaseFirestoreException e) {
                if (queryDocumentSnapshots != null) {
                    int count = queryDocumentSnapshots.size();
                    if (count == 0) {
                        mNoUploads.setVisibility(View.VISIBLE);
                    } else {
                        mNoUploads.setVisibility(View.GONE);
                    }
                }
            }
        });

        Query firstQuery = firebaseFirestore.collection("Users")
                .document(currentUserId).collection("Posts")
                .orderBy("utc", Query.Direction.DESCENDING).limit(15);
        firstQuery.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {

                if (queryDocumentSnapshots != null) {

                    if (!queryDocumentSnapshots.isEmpty()) {
                        if (isFirstPageFirstLoad) {
                            lastVisible = queryDocumentSnapshots.getDocuments().get(queryDocumentSnapshots.size() - 1);
                            myPostsModelLinkedList.clear();
                        }

                        for (@NonNull DocumentChange doc : queryDocumentSnapshots.getDocumentChanges()) {
                            if (doc.getType() == DocumentChange.Type.ADDED) {
                                String docId = doc.getDocument().getId();
                                final MyPostsModel myPostsModel = doc.getDocument().toObject(MyPostsModel.class).withId(docId);

                                if (isFirstPageFirstLoad) {
                                    myPostsModelLinkedList.add(myPostsModel);
                                } else {
                                    myPostsModelLinkedList.add(0, myPostsModel);
                                }
                                myPostsAdapter.notifyDataSetChanged();

                            }
                        }
                        isFirstPageFirstLoad = false;
                    }
                }
            }
        });

    }

    private void loadMorePosts() {
        try {
            if (networkConnection.isConnected(getActivity())) {
                Query nextQuery = firebaseFirestore.collection("Users")
                        .document(currentUserId).collection("Posts")
                        .orderBy("utc", Query.Direction.DESCENDING)
                        .startAfter(lastVisible).limit(15);
                nextQuery.addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots,
                                        @Nullable FirebaseFirestoreException e) {
                        if (queryDocumentSnapshots != null) {
                            if (!queryDocumentSnapshots.isEmpty()) {
                                lastVisible = queryDocumentSnapshots.getDocuments().get(queryDocumentSnapshots.size() - 1);

                                for (@NonNull DocumentChange doc : queryDocumentSnapshots.getDocumentChanges()) {
                                    if (doc.getType() == DocumentChange.Type.ADDED) {

                                        String docId = doc.getDocument().getId();
                                        @NonNull final MyPostsModel myPostsModel =
                                                doc.getDocument().toObject(MyPostsModel.class).withId(docId);

                                        myPostsModelLinkedList.add(myPostsModel);
                                        myPostsAdapter.notifyDataSetChanged();

                                    }
                                }
                            }
                        }
                    }
                });
            }else {
                Toast.makeText(getActivity(), getResources()
                        .getString(R.string.no_internet), Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
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
