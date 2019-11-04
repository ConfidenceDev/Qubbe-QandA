package live.qubbe.android.adapters;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import java.util.List;

import live.qubbe.android.R;
import live.qubbe.android.models.ContributorsModel;
import live.qubbe.android.utils.CommaCounter;

public class ContributorsAdapter extends RecyclerView.Adapter<ContributorsAdapter.ViewHolder> {

    private List<ContributorsModel> contributorsModelListList;
    private Context context;
    private FirebaseFirestore firebaseFirestore;
    private CommaCounter commaCounter;

    public ContributorsAdapter(List<ContributorsModel> contributorsModelListList) {
        this.contributorsModelListList = contributorsModelListList;
    }

    @NonNull
    @Override
    public ContributorsAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_contributor, parent, false);
        context = parent.getContext();
        firebaseFirestore = FirebaseFirestore.getInstance();
        commaCounter = new CommaCounter();
        return new ContributorsAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ContributorsAdapter.ViewHolder holder, final int position) {

        if (position == (getItemCount() - 1)) {
            holder.mSpace.setVisibility(View.VISIBLE);
        } else {
            holder.mSpace.setVisibility(View.GONE);
        }

        try {
            final String docId = contributorsModelListList.get(position).DocId;
            final String contributed = contributorsModelListList.get(position).getContributed();

            holder.mContributed.setText(commaCounter.getFormattedNumber(contributed));;

            firebaseFirestore.collection("Users").document(docId)
                    .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                        @Override
                        public void onEvent(@javax.annotation.Nullable DocumentSnapshot documentSnapshot,
                                            @javax.annotation.Nullable FirebaseFirestoreException e) {
                            if (documentSnapshot != null) {
                                if (documentSnapshot.exists()) {
                                    holder.setImg(documentSnapshot.getString("image"));
                                    holder.mUserName.setText(documentSnapshot.getString("userName"));
                                    holder.mLocation.setText(documentSnapshot.getString("location"));

                                    String face = documentSnapshot.getString("facebook");
                                    String ins = documentSnapshot.getString("instagram");
                                    String tw = documentSnapshot.getString("twitter");
                                    String wb = documentSnapshot.getString("website");

                                    holder.mFace.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            if (!TextUtils.isEmpty(face)) {
                                                Intent intent = null;
                                                try {
                                                    context.getPackageManager()
                                                            .getPackageInfo("com.facebook.katana", 0);
                                                    String url = "https://www.facebook.com/" + face;
                                                    intent = new Intent(Intent.ACTION_VIEW,
                                                            Uri.parse("fb://facewebmodal/f?href=" + url));

                                                } catch (Exception e) {
                                                    // no Facebook app, revert to browser
                                                    String url = "https://facebook.com/" + face;
                                                    intent = new Intent(Intent.ACTION_VIEW);
                                                    intent.setData(Uri.parse(url));
                                                }
                                                context.startActivity(intent);

                                            } else {
                                                Toast.makeText(context,
                                                        context.getResources().getString(R.string.not_available),
                                                        Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });

                                    holder.mInsta.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            if (!TextUtils.isEmpty(ins)) {
                                                Uri uri = Uri.parse("http://instagram.com/_u/" + ins);
                                                Intent instaIntent = new Intent(Intent.ACTION_VIEW, uri);
                                                instaIntent.setPackage("com.instagram.android");

                                                try {
                                                    context.startActivity(instaIntent);

                                                } catch (Exception e) {
                                                    context.startActivity(new Intent(Intent.ACTION_VIEW,
                                                            Uri.parse("https://instagram.com/" + ins)));
                                                }

                                            } else {
                                                Toast.makeText(context,
                                                        context.getResources().getString(R.string.not_available),
                                                        Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });

                                    holder.mTwitt.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            if (!TextUtils.isEmpty(tw)) {
                                                try {
                                                    Intent intent = new Intent(Intent.ACTION_VIEW,
                                                            Uri.parse("twitter://user?screen_name=" + tw));
                                                    context.startActivity(intent);
                                                } catch (Exception e) {
                                                    context.startActivity(new Intent(Intent.ACTION_VIEW,
                                                            Uri.parse("https://twitter.com/#!/" + tw)));
                                                }
                                            }else {
                                                Toast.makeText(context,
                                                        context.getResources().getString(R.string.not_available),
                                                        Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });

                                    holder.mWeb.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            if (!TextUtils.isEmpty(wb)) {
                                                Uri webPage = Uri.parse(wb);
                                                if (!wb.startsWith("http://") || !wb.startsWith("https://")) {
                                                    webPage = Uri.parse("http://" + wb);
                                                }
                                                Intent webIntent = new Intent(Intent.ACTION_VIEW, webPage);
                                                if (webIntent.resolveActivity(context.getPackageManager()) != null) {
                                                    context.startActivity(webIntent);
                                                }
                                            } else {
                                                Toast.makeText(context,
                                                        context.getResources().getString(R.string.not_available),
                                                        Toast.LENGTH_SHORT).show();
                                            }

                                        }
                                    });
                                }
                            }
                        }
                    });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        return contributorsModelListList.size();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private ImageView mProf, mFace, mInsta, mTwitt, mWeb;
        private TextView mUserName, mLocation, mContributed;
        private RelativeLayout mSpace;

        private ViewHolder(View itemView) {
            super(itemView);

          mProf = itemView.findViewById(R.id.conProfImage);
          mFace = itemView.findViewById(R.id.facebook_btn);
          mInsta = itemView.findViewById(R.id.insta_btn);
          mTwitt = itemView.findViewById(R.id.twit_btn);
          mWeb = itemView.findViewById(R.id.page_btn);
          mUserName = itemView.findViewById(R.id.conUserName);
          mLocation = itemView.findViewById(R.id.conUserLocation);
          mContributed = itemView.findViewById(R.id.contributeCount);
          mSpace = itemView.findViewById(R.id.conSpace);
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
