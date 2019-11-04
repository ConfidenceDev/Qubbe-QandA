package live.qubbe.android.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import live.qubbe.android.R;
import live.qubbe.android.fragments.MainFragment;
import live.qubbe.android.fragments.MyPostsFragment;
import live.qubbe.android.fragments.NewFragment;
import live.qubbe.android.utils.CommaCounter;
import live.qubbe.android.utils.CustomTypefaceSpan;
import live.qubbe.android.utils.NetworkConnection;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class MainActivity extends AppCompatActivity
                        implements NavigationView.OnNavigationItemSelectedListener{

    private FirebaseAuth mAuth;
    private FirebaseFirestore firebaseFirestore;

    private MainFragment mainFragment;
    private MyPostsFragment myPostsFragment;
    private NewFragment newFragment;

    private DrawerLayout drawer;
    private ActionBarDrawerToggle toggle;
    private NavigationView navigationView;

    private View header;
    private TextView mInfoText, mQuestionsCount;
    private ImageView mMenuBtn, mMainBtn, mPostsBtn, mNewBtn, mSettingsBtn, mContributorsBtn;
    private NetworkConnection networkConnection;
    private CommaCounter commaCounter;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();
        networkConnection = new NetworkConnection();
        commaCounter = new CommaCounter();

        mainFragment = new MainFragment();
        myPostsFragment = new MyPostsFragment();
        newFragment = new NewFragment();

        mMenuBtn = findViewById(R.id.menuBtn);
        mMainBtn = findViewById(R.id.mainBtn);
        mPostsBtn = findViewById(R.id.postsBtn);
        mNewBtn = findViewById(R.id.newBtn);

        drawer = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        header = navigationView.getHeaderView(0);

        mSettingsBtn = header.findViewById(R.id.settingsBtn);
        mContributorsBtn = header.findViewById(R.id.contributorsBtn);
        mInfoText = header.findViewById(R.id.infoText);
        mQuestionsCount = header.findViewById(R.id.questionsCount);

        if (!networkConnection.isConnected(this)) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(getResources().getString(R.string.no_internet));
            builder.setMessage(getResources().getString(R.string.internet_continue));
            builder.setPositiveButton(getResources().getString(R.string.ok), null);

            AlertDialog createDialog = builder.create();
            createDialog.show();
        }

        toggle = new ActionBarDrawerToggle(
                this, drawer, null, 0, 0);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);

        //===================== Menu ====================================
        Menu m = navigationView.getMenu();
        for (int i = 0; i < m.size(); i++) {
            MenuItem mi = m.getItem(i);

            //for applying a font to subMenu ...
            SubMenu subMenu = mi.getSubMenu();
            if (subMenu != null && subMenu.size() > 0) {
                for (int j = 0; j < subMenu.size(); j++) {
                    MenuItem subMenuItem = subMenu.getItem(j);
                    applyFontToMenuItem(subMenuItem);
                }
            }
            //the method we have create in activity
            applyFontToMenuItem(mi);
        }

        replaceFragment(mainFragment, "All", false);
        mMainBtn.setImageDrawable(getDrawable(R.mipmap.cards_se));
        mPostsBtn.setImageDrawable(getDrawable(R.mipmap.posts));
        mNewBtn.setImageDrawable(getDrawable(R.mipmap.add));

       if (mAuth.getCurrentUser() != null){
           firebaseFirestore.collection("Media").document("Information")
                   .addSnapshotListener(this, new EventListener<DocumentSnapshot>() {
                       @Override
                       public void onEvent(@Nullable DocumentSnapshot documentSnapshot,
                                           @Nullable FirebaseFirestoreException e) {
                           if (documentSnapshot != null) {
                               if (documentSnapshot.exists()) {
                                   String info = documentSnapshot.getString("info");
                                   mInfoText.setText(info);
                               }
                           }
                       }
                   });

           firebaseFirestore.collection("All").addSnapshotListener(this,
                   new EventListener<QuerySnapshot>() {
                       @Override
                       public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots,
                                           @Nullable FirebaseFirestoreException e) {
                           if (queryDocumentSnapshots != null) {
                               int size = queryDocumentSnapshots.size();
                               mQuestionsCount.setText(commaCounter.getFormattedNumber(String.valueOf(size)));
                           }
                       }
                   });


           mMenuBtn.setOnClickListener(new View.OnClickListener() {
               @Override
               public void onClick(View v) {
                   try {
                       if (!drawer.isDrawerOpen(GravityCompat.START)) {
                           drawer.openDrawer(GravityCompat.START);
                       }
                   } catch (Exception r) {
                       r.printStackTrace();
                   }
               }
           });

           mMainBtn.setOnClickListener(new View.OnClickListener() {
               @Override
               public void onClick(View v) {
                   try {
                       replaceFragment(mainFragment, "All", false);
                       mMainBtn.setImageDrawable(getDrawable(R.mipmap.cards_se));
                       mPostsBtn.setImageDrawable(getDrawable(R.mipmap.posts));
                       mNewBtn.setImageDrawable(getDrawable(R.mipmap.add));
                   } catch (Exception r) {
                       r.printStackTrace();
                   }
               }
           });

           mPostsBtn.setOnClickListener(new View.OnClickListener() {
               @Override
               public void onClick(View v) {
                   try {
                       replaceFragment(myPostsFragment, null, false);
                       mMainBtn.setImageDrawable(getDrawable(R.mipmap.cards));
                       mPostsBtn.setImageDrawable(getDrawable(R.mipmap.posts_se));
                       mNewBtn.setImageDrawable(getDrawable(R.mipmap.add));
                   } catch (Exception r) {
                       r.printStackTrace();
                   }
               }
           });

           mNewBtn.setOnClickListener(new View.OnClickListener() {
               @Override
               public void onClick(View v) {
                   try {
                       replaceFragment(newFragment, null, false);
                       mMainBtn.setImageDrawable(getDrawable(R.mipmap.cards));
                       mPostsBtn.setImageDrawable(getDrawable(R.mipmap.posts));
                       mNewBtn.setImageDrawable(getDrawable(R.mipmap.add_se));
                   } catch (Exception r) {
                       r.printStackTrace();
                   }
               }
           });

           mSettingsBtn.setOnClickListener(new View.OnClickListener() {
               @Override
               public void onClick(View view) {
                   startActivity(new Intent(MainActivity.this, SettingsActivity.class));
                   overridePendingTransition(R.anim.slide_in_right, R.anim.no_anim);
               }
           });

           mContributorsBtn.setOnClickListener(new View.OnClickListener() {
               @Override
               public void onClick(View view) {
                   startActivity(new Intent(MainActivity.this, ContributorsActivity.class));
                   overridePendingTransition(R.anim.slide_in_right, R.anim.no_anim);
               }
           });
       }
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            sendToSignIn();
        } else {
            final String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            try {
                firebaseFirestore.collection("Users").document(userId)
                        .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                            @Override
                            public void onEvent(@Nullable DocumentSnapshot documentSnapshot,
                                                @Nullable FirebaseFirestoreException e) {
                                if (documentSnapshot != null) {
                                    if (!documentSnapshot.exists()) {
                                        startActivity(new Intent(MainActivity.this,
                                                EditAccountActivity.class));
                                        finish();
                                    }
                                }
                            }
                        });

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void sendToSignIn() {
        startActivity(new Intent(MainActivity.this, SignInActivity.class));
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_science:
                replaceFragment(mainFragment, "Science", true);
                fragments();
                break;
            case R.id.action_tech:
                replaceFragment(mainFragment, "Tech", true);
                fragments();
                break;
            case R.id.action_music:
                replaceFragment(mainFragment, "Music", true);
                fragments();
                break;
            case R.id.action_programming:
                replaceFragment(mainFragment, "Programming", true);
                fragments();
                break;
            case R.id.action_entertainment:
                replaceFragment(mainFragment, "Entertainment", true);
                fragments();
                break;
            case R.id.action_games:
                replaceFragment(mainFragment, "Games", true);
                fragments();
                break;
            case R.id.action_art:
                replaceFragment(mainFragment, "Art", true);
                fragments();
                break;
            case R.id.action_urban:
                replaceFragment(mainFragment, "Urban", true);
                fragments();
                break;
            case R.id.action_nature:
                replaceFragment(mainFragment, "Nature", true);
                fragments();
                break;
            case R.id.action_sports:
                replaceFragment(mainFragment, "Sports", true);
                fragments();
                break;
            case R.id.action_business:
                replaceFragment(mainFragment, "Business", true);
                fragments();
                break;
            case R.id.action_education:
                replaceFragment(mainFragment, "Education", true);
                fragments();
                break;
            case R.id.action_movies:
                replaceFragment(mainFragment, "Movies", true);
                fragments();
                break;
            case R.id.action_lifestyle:
                replaceFragment(mainFragment, "Lifestyle", true);
                fragments();
                break;
            case R.id.action_romance:
                replaceFragment(mainFragment, "Romance", true);
                fragments();
                break;
            case R.id.action_more:
                replaceFragment(mainFragment, "Others", true);
                fragments();
                break;
        }
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void fragments() {
        mMainBtn.setImageDrawable(getDrawable(R.mipmap.cards_se));
        mPostsBtn.setImageDrawable(getDrawable(R.mipmap.posts));
        mNewBtn.setImageDrawable(getDrawable(R.mipmap.add));
    }

    private void applyFontToMenuItem(MenuItem mi) {
        Typeface font = Typeface.createFromAsset(getAssets(), "fonts/ubuntu.regular.ttf");
        SpannableString mNewTitle = new SpannableString(mi.getTitle());
        mNewTitle.setSpan(new CustomTypefaceSpan("", font), 0, mNewTitle.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        mi.setTitle(mNewTitle);
    }

    public void replaceFragment(Fragment fragment, String category, boolean isReplaced) {
        try {
            Bundle bundle = new Bundle();
            bundle.putString("category", category);
            fragment.setArguments(bundle);

            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out);
            fragmentManager.executePendingTransactions();

            mainFragment.passCategory(category);

            try {
                if (!isReplaced) {
                    fragmentTransaction.replace(R.id.main_frame, fragment);
                    fragmentTransaction.commit();

                } else {
                    if (fragment.getArguments() != null) {
                        synchronized (fragment.getArguments()) {
                            fragment.getArguments().putString("category", category);
                        }
                    }

                    fragmentTransaction.replace(R.id.main_frame, fragment);
                    fragmentTransaction.detach(fragment).commitNowAllowingStateLoss();
                    fragmentTransaction.attach(fragment).commitAllowingStateLoss();


                }
            }catch (Exception m){
                m.printStackTrace();
            }
        } catch (Exception r) {
            r.printStackTrace();
        }
    }

    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            finish();
            super.onBackPressed();
        }
    }
}
