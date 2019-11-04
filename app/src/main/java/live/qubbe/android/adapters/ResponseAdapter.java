package live.qubbe.android.adapters;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
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
import java.util.List;
import java.util.Map;
import java.util.Objects;

import live.qubbe.android.R;
import live.qubbe.android.models.ResponseModel;
import live.qubbe.android.utils.Counter;
import live.qubbe.android.utils.NetworkConnection;

import static live.qubbe.android.utils.GetTimeAgo.getTimeAgo;

public class ResponseAdapter extends RecyclerView.Adapter<ResponseAdapter.ViewHolder> {

    private List<ResponseModel> responseList;
    private Context context;
    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth firebaseAuth;
    private String currentUserId;
    private Counter counter;
    private NetworkConnection networkConnection;

    public ResponseAdapter(List<ResponseModel> responseList) {
        this.responseList = responseList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_response, parent, false);
        context = parent.getContext();
        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
        networkConnection = new NetworkConnection();
        counter = new Counter();
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {

        if (position == (getItemCount() - 1)) {
            holder.mSpace.setVisibility(View.VISIBLE);
        } else {
            holder.mSpace.setVisibility(View.GONE);
        }

        try {
            currentUserId = firebaseAuth.getCurrentUser().getUid();
            final String docId = responseList.get(position).DocId;
            final long date_utc = responseList.get(position).getUtc().getTime();
            final String user_data = responseList.get(position).getUserId();
            final String postId = responseList.get(position).getPostId();
            final String response = responseList.get(position).getResponse();

            holder.mText.setText(response);
            holder.mDate.setText(getTimeAgo(date_utc, context));

            if (user_data.equals(currentUserId)) {
                holder.mDelete.setVisibility(View.VISIBLE);
            }

            firebaseFirestore.collection("Users").document(user_data)
                    .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                        @Override
                        public void onEvent(@javax.annotation.Nullable DocumentSnapshot documentSnapshot,
                                            @javax.annotation.Nullable FirebaseFirestoreException e) {
                            if (documentSnapshot != null) {
                                if (documentSnapshot.exists()) {
                                    holder.setImg(documentSnapshot.getString("image"));
                                    holder.mUserName.setText(documentSnapshot.getString("userName"));
                                }
                            }
                        }
                    });

            holder.mResponseImgRel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!TextUtils.isEmpty(user_data) && !currentUserId.equals(user_data)) {
                        Dialog dialog = new Dialog(context);
                        Window window = dialog.getWindow();
                        if (window != null) {
                            window.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
                            window.setDimAmount(.7f);
                        }
                        dialog.setCancelable(false);
                        dialog.setContentView(R.layout.dialog_profile);
                        TextView userName, userLocation;
                        ImageView userImage, closeBtn;

                        userName = dialog.findViewById(R.id.dialogName);
                        userLocation = dialog.findViewById(R.id.dialogLocation);
                        userImage = dialog.findViewById(R.id.dialogProfImage);
                        closeBtn = dialog.findViewById(R.id.closeDialogBtn);

                        try {

                            firebaseFirestore.collection("Users").document(user_data)
                                    .get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                @Override
                                public void onSuccess(DocumentSnapshot documentSnapshot) {
                                    if (documentSnapshot.exists()) {
                                        String img = documentSnapshot.getString("image");
                                        String name = documentSnapshot.getString("userName");
                                        String location = documentSnapshot.getString("location");

                                        userName.setText(name);
                                        userLocation.setText(location);
                                        RequestOptions placeholderRequest = new RequestOptions();
                                        placeholderRequest.placeholder(R.color.grey200);
                                        Glide.with(context)
                                                .setDefaultRequestOptions(placeholderRequest)
                                                .load(img).into(userImage);
                                    }
                                }
                            });

                            closeBtn.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    dialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
                                    dialog.dismiss();
                                }
                            });

                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                        dialog.show();

                    }
                }
            });

            firebaseFirestore.collection("All")
                    .document(postId).collection("Response")
                    .document(docId).collection("Likes")
                    .addSnapshotListener(new EventListener<QuerySnapshot>() {
                        @Override
                        public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                            if (queryDocumentSnapshots != null) {
                                if (!queryDocumentSnapshots.isEmpty()) {
                                    int count = queryDocumentSnapshots.size();
                                    holder.mLikeCount.setText(counter.countVal(Long.parseLong(String.valueOf(count)), context));
                                } else {
                                    holder.mLikeCount.setText("0");
                                }
                            }
                        }
                    });

            firebaseFirestore.collection("All")
                    .document(postId).collection("Response")
                    .document(docId).collection("Likes")
                    .document(currentUserId).addSnapshotListener(new EventListener<DocumentSnapshot>() {
                @Override
                public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                    if (documentSnapshot != null) {
                        if (documentSnapshot.exists()) {
                            holder.mLikeBtn.setTextColor(context.getResources().getColor(R.color.colorAccent));
                        } else {
                            holder.mLikeBtn.setTextColor(context.getResources().getColor(R.color.black_200));
                        }
                    }
                }
            });

            holder.mLikeBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (networkConnection.isConnected(context)) {
                        try {
                            firebaseFirestore.collection("All")
                                    .document(postId).collection("Response")
                                    .document(docId).collection("Likes")
                                    .document(currentUserId).get()
                                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                            if (task.isSuccessful()) {
                                                if (task.getResult().exists()) {
                                                    firebaseFirestore.collection("All")
                                                            .document(postId).collection("Response")
                                                            .document(docId).collection("Likes")
                                                            .document(currentUserId).delete();
                                                } else {
                                                    Date utc = new Date(System.currentTimeMillis());
                                                    Map<String, Object> likesMap = new HashMap<>();
                                                    likesMap.put("utc", utc);

                                                    setTopContribution(user_data);

                                                    firebaseFirestore.collection("All")
                                                            .document(postId).collection("Response")
                                                            .document(docId).collection("Likes")
                                                            .document(currentUserId).set(likesMap);
                                                }
                                            }
                                        }
                                    });
                        } catch (Exception c) {
                            c.printStackTrace();
                        }
                    }
                }
            });

            // ============================================ Delete ===================================
            holder.mDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AlertDialog.Builder update_builder = new AlertDialog.Builder(context);
                    update_builder
                            .setMessage(context.getResources().getString(R.string.delete_response))
                            .setPositiveButton(context.getResources().getString(R.string.yes), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                    if (!networkConnection.isConnected(context)) {
                                        Toast.makeText(context, context.getResources().getString(R.string.no_internet), Toast.LENGTH_SHORT).show();
                                    } else {
                                        try {

                                            firebaseFirestore.collection("All")
                                                    .document(postId).collection("Response")
                                                    .document(docId).delete()
                                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                        @Override
                                                        public void onSuccess(Void aVoid) {

                                                            responseList.remove(position);
                                                            Toast.makeText(context, context.getResources().getString(R.string.deleted), Toast.LENGTH_LONG).show();
                                                            notifyDataSetChanged();
                                                        }
                                                    }).addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    Toast.makeText(context, context.getResources().getString(R.string.error), Toast.LENGTH_SHORT).show();
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
        return responseList.size();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    private void setTopContribution(String userId) {
        firebaseFirestore.collection("Contributors").document(userId)
                .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    if (task.getResult().exists()) {
                        int count = Integer.parseInt(task.getResult().getString("contributed"));

                        int total = count + 1;

                        try {
                            Date utc = new Date(System.currentTimeMillis());
                            Map<String, Object> contributeMap = new HashMap<>();
                            contributeMap.put("contributed", String.valueOf(total));
                            contributeMap.put("utc", utc);

                            firebaseFirestore.collection("Contributors")
                                    .document(userId).update(contributeMap);
                        } catch (Exception u) {
                            u.printStackTrace();
                        }
                    } else {
                        int total = 1;

                        try {
                            Date utc = new Date(System.currentTimeMillis());
                            Map<String, Object> contributeMap = new HashMap<>();
                            contributeMap.put("contributed", String.valueOf(total));
                            contributeMap.put("utc", utc);

                            firebaseFirestore.collection("Contributors")
                                    .document(userId).set(contributeMap);

                        } catch (Exception u) {
                            u.printStackTrace();
                        }
                    }
                }
            }
        });
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private View mView;
        private ImageView mProf;
        private TextView mUserName, mDate, mText, mLikeBtn, mLikeCount, mDelete;
        private RelativeLayout mSpace, mResponseImgRel;

        private ViewHolder(View itemView) {
            super(itemView);

            mView = itemView;
            mProf = mView.findViewById(R.id.responseItemImg);
            mUserName = mView.findViewById(R.id.responseItemName);
            mText = mView.findViewById(R.id.responseItemText);
            mDate = mView.findViewById(R.id.responseItemDate);
            mLikeBtn = mView.findViewById(R.id.responseLikeText);
            mLikeCount = mView.findViewById(R.id.responseLikeCount);
            mDelete = mView.findViewById(R.id.deleteResponseBtn);
            mSpace = mView.findViewById(R.id.responseItemSpaceRel);
            mResponseImgRel = mView.findViewById(R.id.responseItemImgRel);
        }

        private void setImg(String downloadUrl) {

            if (!TextUtils.isEmpty(downloadUrl)) {
                RequestOptions placeholderRequest = new RequestOptions();
                placeholderRequest.placeholder(R.color.grey200);
                Glide.with(context).setDefaultRequestOptions(placeholderRequest)
                        .load(downloadUrl)
                        .into(mProf);

            }
        }
    }
}
