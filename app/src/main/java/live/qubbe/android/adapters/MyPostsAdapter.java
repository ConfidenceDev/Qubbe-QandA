package live.qubbe.android.adapters;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import live.qubbe.android.R;
import live.qubbe.android.activities.ResponseActivity;
import live.qubbe.android.models.MyPostsModel;
import live.qubbe.android.utils.Counter;
import live.qubbe.android.utils.NetworkConnection;

import static live.qubbe.android.utils.GetTimeAgo.getTimeAgo;

public class MyPostsAdapter extends RecyclerView.Adapter<MyPostsAdapter.ViewHolder> {

    private LinkedList<MyPostsModel> myPostsModelLinkedList;
    private Context context;
    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth firebaseAuth;
    private Counter counter;
    private NetworkConnection networkConnection;

    public MyPostsAdapter(LinkedList<MyPostsModel> myPostsModelLinkedList) {
        this.myPostsModelLinkedList = myPostsModelLinkedList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_posts, parent, false);
        context = parent.getContext();
        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
        networkConnection = new NetworkConnection();
        counter = new Counter();
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        if (position == (getItemCount() - 1)) {
            holder.mSpace.setVisibility(View.VISIBLE);
        } else {
            holder.mSpace.setVisibility(View.GONE);
        }

        try {
            final String docId = myPostsModelLinkedList.get(position).DocId;
            final String currentUserId = firebaseAuth.getCurrentUser().getUid();

            String content = myPostsModelLinkedList.get(position).getContentText();
            String category = myPostsModelLinkedList.get(position).getCategory();
            String postId = myPostsModelLinkedList.get(position).getPostId();
            String posterId = myPostsModelLinkedList.get(position).getPosterId();
            String utc = myPostsModelLinkedList.get(position).getUtc().toString();
            long utcTime = myPostsModelLinkedList.get(position).getUtc().getTime();

            holder.mContent.setText(content);
            holder.mCategory.setText(category);
            holder.mDate.setText(getTimeAgo(utcTime, context));
            
            firebaseFirestore.collection("All")
                    .document(postId).collection("Likes")
                    .addSnapshotListener(new EventListener<QuerySnapshot>() {
                        @Override
                        public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots,
                                            @Nullable FirebaseFirestoreException e) {
                            if (queryDocumentSnapshots != null) {
                                if (!queryDocumentSnapshots.isEmpty()) {
                                    int count = queryDocumentSnapshots.size();
                                    holder.mLikesCount.setText(counter.countVal(Long.parseLong(String.valueOf(count)), context));
                                } else {
                                    holder.mLikesCount.setText("0");
                                }
                            }
                        }
                    });

            firebaseFirestore.collection("All")
                    .document(postId).collection("Likes")
                    .document(currentUserId).addSnapshotListener(new EventListener<DocumentSnapshot>() {
                @Override
                public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                    if (documentSnapshot != null) {
                        if (documentSnapshot.exists()) {
                            holder.mLikesBtn.setImageDrawable(context.getResources().getDrawable(R.mipmap.like_se));
                        } else {
                            holder.mLikesBtn.setImageDrawable(context.getResources().getDrawable(R.mipmap.like));
                        }
                    }
                }
            });

            holder.mLikesBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (networkConnection.isConnected(context)) {
                        firebaseFirestore.collection("Users").document(currentUserId)
                                .collection("Posts")
                                .document(postId).addSnapshotListener(new EventListener<DocumentSnapshot>() {
                            @Override
                            public void onEvent(@Nullable DocumentSnapshot documentSnapshot,
                                                @Nullable FirebaseFirestoreException e) {
                                if (documentSnapshot != null) {
                                    if (documentSnapshot.exists()) {
                                        firebaseFirestore.collection("All")
                                                .document(postId).collection("Likes")
                                                .document(currentUserId).get()
                                                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                        if (task.isSuccessful()) {
                                                            if (task.getResult().exists()) {
                                                                firebaseFirestore.collection("All")
                                                                        .document(postId).collection("Likes")
                                                                        .document(currentUserId).delete();
                                                            } else {
                                                                Date utc = new Date(System.currentTimeMillis());
                                                                Map<String, Object> likesMap = new HashMap<>();
                                                                likesMap.put("utc", utc);

                                                                firebaseFirestore.collection("All")
                                                                        .document(postId).collection("Likes")
                                                                        .document(currentUserId).set(likesMap);
                                                            }
                                                        }
                                                    }
                                                });
                                    } else {
                                        Toast.makeText(context, context.getResources()
                                                        .getString(R.string.post_removed),
                                                Toast.LENGTH_LONG).show();
                                    }
                                }
                            }
                        });
                    }
                }
            });

            firebaseFirestore.collection("All")
                    .document(postId).collection("Response")
                    .addSnapshotListener(new EventListener<QuerySnapshot>() {
                        @Override
                        public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                            if (queryDocumentSnapshots != null) {
                                if (!queryDocumentSnapshots.isEmpty()) {
                                    int count = queryDocumentSnapshots.size();
                                    holder.mResponseCount.setText(counter.countVal(Long.parseLong(String.valueOf(count)), context));
                                } else {
                                    holder.mResponseCount.setText("0");
                                }
                            }
                        }
                    });

            holder.mResponseBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    firebaseFirestore.collection("Users").document(currentUserId)
                            .collection("Posts")
                            .document(postId).addSnapshotListener(new EventListener<DocumentSnapshot>() {
                        @Override
                        public void onEvent(@Nullable DocumentSnapshot documentSnapshot,
                                            @Nullable FirebaseFirestoreException e) {
                            if (documentSnapshot != null) {
                                if (documentSnapshot.exists()) {
                                    Intent commentIntent = new Intent(context, ResponseActivity.class);
                                    commentIntent.putExtra("posterId", posterId);
                                    commentIntent.putExtra("postId", postId);
                                    context.startActivity(commentIntent);
                                } else {
                                    Toast.makeText(context, context.getResources()
                                                    .getString(R.string.post_removed),
                                            Toast.LENGTH_LONG).show();
                                }
                            }
                        }
                    });
                }
            });

            // ============================================ Delete ===================================
            holder.mDeleteBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AlertDialog.Builder update_builder = new AlertDialog.Builder(context);
                    update_builder
                            .setMessage(context.getResources().getString(R.string.delete_post))
                            .setPositiveButton(context.getResources().getString(R.string.yes), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                    if (!networkConnection.isConnected(context)) {
                                        Toast.makeText(context, context.getResources().getString(R.string.no_internet), Toast.LENGTH_SHORT).show();
                                    } else {
                                        try {
                                            firebaseFirestore.collection("All").document(docId).delete();
                                            firebaseFirestore.collection(category).document(docId).delete();
                                            firebaseFirestore.collection("Users")
                                                    .document(posterId)
                                                    .collection("Posts").document(docId)
                                                    .delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    myPostsModelLinkedList.remove(position);
                                                    Toast.makeText(context, context.getResources().getString(R.string.deleted), Toast.LENGTH_LONG).show();
                                                    notifyDataSetChanged();
                                                }
                                            }).addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    Toast.makeText(context, context.getResources().getString(R.string.err) + e.getMessage(), Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }
                            })
                            .setNegativeButton(context.getResources().getString(R.string.no), null);

                    AlertDialog alert_update = update_builder.create();
                    alert_update.show();
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        return myPostsModelLinkedList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private View mView;
        private ImageView mLikesBtn, mResponseBtn, mDeleteBtn;
        private TextView mCategory, mDate, mLikesCount, mResponseCount, mContent;
        private RelativeLayout mSpace;

        private ViewHolder(View itemView) {
            super(itemView);

            mView = itemView;
            mCategory = mView.findViewById(R.id.myCategoryText);
            mDate = mView.findViewById(R.id.myDate);
            mLikesCount = mView.findViewById(R.id.myLikesCount);
            mResponseCount = mView.findViewById(R.id.myResponseCount);
            mContent = mView.findViewById(R.id.myQuestionArea);
            mLikesBtn = mView.findViewById(R.id.myLikeBtn);
            mResponseBtn = mView.findViewById(R.id.myResponseBtn);
            mSpace = mView.findViewById(R.id.spaceRel);
            mDeleteBtn = mView.findViewById(R.id.myDeleteBtn);
        }
    }
}
