package live.qubbe.android.adapters;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import live.qubbe.android.R;
import live.qubbe.android.activities.ResponseActivity;
import live.qubbe.android.models.CardModel;
import live.qubbe.android.utils.Counter;
import live.qubbe.android.utils.NetworkConnection;

import static live.qubbe.android.utils.GetTimeAgo.getTimeAgo;

public class CardAdapter extends ArrayAdapter<CardModel> {

    private FirebaseAuth mAuth;
    private FirebaseFirestore firebaseFirestore;
    private String currentUserId;
    private Context context;
    private Counter counter;
    private NetworkConnection networkConnection;

    public CardAdapter(Context context) {
        super(context, 0);
        this.context = context;
    }

    @NonNull
    @Override
    public View getView(int position, View contentView, @NonNull ViewGroup parent) {
        final ViewHolder holder;
        if (contentView == null) {
            LayoutInflater inflater = LayoutInflater.from(getContext());
            contentView = inflater.inflate(R.layout.item_card, parent, false);
            holder = new ViewHolder(contentView);
            contentView.setTag(holder);


        } else {
            holder = (ViewHolder) contentView.getTag();
        }

        mAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();
        counter = new Counter();
        networkConnection = new NetworkConnection();

        if (mAuth.getCurrentUser() != null) {
            currentUserId = mAuth.getCurrentUser().getUid();

            CardModel data = getItem(position);

            final String docId = data.DocId;
            final String posterId = data.getPosterId();
            final String category = data.getCategory();
            final String contentText = data.getContentText();
            long utcTime = data.getUtc().getTime();

            holder.mCategory.setText(category);
            holder.mDate.setText(getTimeAgo(utcTime, context));
            holder.mQuestion.setText(contentText);

            firebaseFirestore.collection("Users").document(posterId)
                    .get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {
                    if (documentSnapshot.exists()) {
                        String img = documentSnapshot.getString("image");

                        RequestOptions placeholderRequest = new RequestOptions();
                        placeholderRequest.placeholder(R.color.grey200);
                        Glide.with(context)
                                .setDefaultRequestOptions(placeholderRequest)
                                .load(img).into(holder.mPosterImg);
                    }
                }
            });

            holder.mPosterImgRel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (!TextUtils.isEmpty(posterId) && !currentUserId.equals(posterId)) {
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

                            firebaseFirestore.collection("Users").document(posterId)
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
                    .document(docId).collection("Likes")
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

            DocumentReference postDoc = firebaseFirestore.collection("All")
                    .document(docId).collection("Likes")
                    .document(currentUserId);

            postDoc.addSnapshotListener(new EventListener<DocumentSnapshot>() {
                @Override
                public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                    if (documentSnapshot != null) {
                        if (documentSnapshot.exists()) {
                            holder.mLikeBtn.setImageDrawable(context.getResources().getDrawable(R.mipmap.like_se));
                        } else {
                            holder.mLikeBtn.setImageDrawable(context.getResources().getDrawable(R.mipmap.like));
                        }
                    }
                }
            });

            holder.mLikeBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (networkConnection.isConnected(context)) {

                        postDoc.addSnapshotListener(new EventListener<DocumentSnapshot>() {
                            @Override
                            public void onEvent(@Nullable DocumentSnapshot documentSnapshot,
                                                @Nullable FirebaseFirestoreException e) {
                                if (documentSnapshot != null) {
                                    if (documentSnapshot.exists()) {
                                        postDoc.get()
                                                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                        if (task.isSuccessful()) {
                                                            if (task.getResult().exists()) {
                                                                postDoc.delete();
                                                            } else {
                                                                Date utc = new Date(System.currentTimeMillis());
                                                                Map<String, Object> likesMap = new HashMap<>();
                                                                likesMap.put("utc", utc);

                                                                postDoc.set(likesMap);
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
                    .document(docId).collection("Response")
                    .addSnapshotListener(new EventListener<QuerySnapshot>() {
                        @Override
                        public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots,
                                            @Nullable FirebaseFirestoreException e) {
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
                    firebaseFirestore.collection("Users").document(posterId)
                            .collection("Posts")
                            .document(docId).addSnapshotListener(new EventListener<DocumentSnapshot>() {
                        @Override
                        public void onEvent(@Nullable DocumentSnapshot documentSnapshot,
                                            @Nullable FirebaseFirestoreException e) {
                            if (documentSnapshot != null) {
                                if (documentSnapshot.exists()) {
                                    Intent commentIntent = new Intent(context, ResponseActivity.class);
                                    commentIntent.putExtra("posterId", posterId);
                                    commentIntent.putExtra("postId", docId);
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


            holder.mFlagBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    PopupMenu popup = new PopupMenu(context, holder.mFlagBtn);
                    MenuInflater inflater = popup.getMenuInflater();
                    inflater.inflate(R.menu.report_menu, popup.getMenu());
                    popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem menuItem) {
                            if (menuItem.getItemId() == R.id.action_report) {
                                AlertDialog.Builder update_builder = new AlertDialog.Builder(context);
                                update_builder
                                        .setMessage(context.getResources().getString(R.string.flag_msg))
                                        .setPositiveButton(context.getResources().getString(R.string.yes), new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                if (!networkConnection.isConnected(context)) {
                                                    Toast.makeText(context, context.getResources().getString(R.string.no_internet), Toast.LENGTH_SHORT).show();
                                                } else {
                                                    reportMethod(docId, posterId, category);
                                                }
                                            }
                                        }).setNegativeButton(context.getResources().getString(R.string.no), null);

                                AlertDialog alert_update = update_builder.create();
                                alert_update.show();
                                return true;
                            }
                            return false;
                        }
                    });
                    popup.show();
                }
            });
        }
        return contentView;
    }

    private void reportMethod(String id, String posterId, String category) {
        Date utc = new Date(System.currentTimeMillis());
        Map<String, Object> reportMap = new HashMap<>();
        reportMap.put("utc", utc);

        try {
            firebaseFirestore.collection("Flags").document(id)
                    .collection("PostReports").get()
                    .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                        @Override
                        public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                            int size = queryDocumentSnapshots.size();
                            if (size == 0) {
                                firebaseFirestore.collection("Flags").document(id)
                                        .collection("PostReports").document(currentUserId)
                                        .set(reportMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            doneTask();
                                        }
                                    }
                                });

                            } else if (size < 5 && size > 0) {
                                firebaseFirestore.collection("Flags").document(id)
                                        .collection("PostReports").document(currentUserId)
                                        .set(reportMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            doneTask();
                                        }
                                    }
                                });

                            } else if (size == 5) {
                                try {
                                    firebaseFirestore.collection("Flags").document(id)
                                            .collection("PostReports").get()
                                            .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                                @Override
                                                public void onComplete(@NonNull Task<QuerySnapshot> task) {

                                                    if (task.isSuccessful()) {
                                                        if (!task.getResult().isEmpty()) {
                                                            for (DocumentChange doc : task.getResult().getDocumentChanges()) {
                                                                String id = doc.getDocument().getId();

                                                                if (id.equals(currentUserId)) {
                                                                    doneTask();
                                                                } else {
                                                                    firebaseFirestore.collection("All").document(id).delete();
                                                                    firebaseFirestore.collection(category).document(id).delete();
                                                                    firebaseFirestore.collection("Users")
                                                                            .document(posterId)
                                                                            .collection("Posts").document(id)
                                                                            .delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                        @Override
                                                                        public void onSuccess(Void aVoid) {
                                                                            doneTask();
                                                                        }
                                                                    }).addOnFailureListener(new OnFailureListener() {
                                                                        @Override
                                                                        public void onFailure(@NonNull Exception e) {
                                                                            Toast.makeText(context, context.getResources().getString(R.string.err) + e.getMessage(), Toast.LENGTH_SHORT).show();
                                                                        }
                                                                    });
                                                                }
                                                            }
                                                        }
                                                    }

                                                }
                                            });

                                } catch (Exception e) {
                                    e.printStackTrace();
                                }

                            } else {
                                doneTask();
                            }
                        }
                    });

        } catch (Exception e) {
            Toast.makeText(context, context.
                    getResources().getString(R.string.err) + e.getMessage(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }

    private void doneTask() {
        Toast.makeText(context, context.getResources().getString(R.string.flag_sent), Toast.LENGTH_SHORT).show();
    }

    private static class ViewHolder {
        private TextView mCategory, mDate, mQuestion, mLikesCount, mResponseCount;
        private ImageView mPosterImg, mLikeBtn, mResponseBtn, mFlagBtn;
        private RelativeLayout mPosterImgRel;

        private ViewHolder(View view) {
            this.mCategory = view.findViewById(R.id.categoryText);
            this.mDate = view.findViewById(R.id.postDate);
            this.mQuestion =  view.findViewById(R.id.questionArea);
            this.mLikesCount =  view.findViewById(R.id.likesCount);
            this.mResponseCount =  view.findViewById(R.id.responseCount);
            this.mPosterImg =  view.findViewById(R.id.cardUserImg);
            this.mLikeBtn =  view.findViewById(R.id.likeBtn);
            this.mResponseBtn =  view.findViewById(R.id.responseBtn);
            this.mFlagBtn =  view.findViewById(R.id.flagBtn);
            this.mPosterImgRel = view.findViewById(R.id.cardUserImgRel);
        }
    }
}
