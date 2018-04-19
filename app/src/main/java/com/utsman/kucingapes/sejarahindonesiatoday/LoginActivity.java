package com.utsman.kucingapes.sejarahindonesiatoday;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.jaeger.library.StatusBarUtil;
import com.onesignal.OSNotification;
import com.onesignal.OSNotificationOpenResult;
import com.onesignal.OneSignal;

import org.json.JSONObject;
import org.w3c.dom.Text;

import butterknife.BindView;
import butterknife.ButterKnife;
import jp.wasabeef.glide.transformations.BlurTransformation;
import me.relex.circleindicator.CircleIndicator;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

import static com.bumptech.glide.request.RequestOptions.bitmapTransform;

public class LoginActivity extends AppCompatActivity {

    private static final int RC_SIGN_IN = 222;
    private GoogleSignInClient mGoogleSignInClient;
    FirebaseAuth mAuth;
    ProgressDialog mProgressDialog;
    FirebaseUser user;

    @BindView(R.id.view_pager) ViewPager viewPager;
    @BindView(R.id.sign_button) SignInButton signInButton;
    @BindView(R.id.indicator) CircleIndicator indicator;
    @BindView(R.id.btnColor) Button btnColor;

    int page = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/Tajawal-Medium.ttf")
                .setFontAttrId(R.attr.fontPath)
                .build()
        );
        //StatusBarUtil.setTransparent(this);
        StatusBarUtil.setTranslucentForImageView(this, (int) 5f, viewPager);
        ButterKnife.bind(this);


        signInButton.setVisibility(View.GONE);
        if (!FirebaseApp.getApps(this).isEmpty()) {
            FirebaseDatabase.getInstance().setPersistenceEnabled(true);
        }

        // OneSignal Initialization
        OneSignal.startInit(this)
                .setNotificationOpenedHandler(new NotifOneSignal())
                //.setNotificationReceivedHandler(new NotifDeliver())
                .inFocusDisplaying(OneSignal.OSInFocusDisplayOption.Notification)
                .unsubscribeWhenNotificationsAreDisabled(true)
                .init();

        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        mAuth = FirebaseAuth.getInstance();
        //SignInButton signInButton = findViewById(R.id.sign_button);
        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signIn();
            }
        });

        btnColor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewPager.setCurrentItem(viewPager.getCurrentItem() +1, true);
            }
        });
        setupViewPager();
        pageChange();

        for (int i = 0; i < signInButton.getChildCount(); i++) {
            View v = signInButton.getChildAt(i);

            if (v instanceof TextView) {
                TextView tv = (TextView) v;
                tv.setText("Masuk");
                return;
            }
        }
    }

    private void setupViewPager() {
        StartAdapter startAdapter = new StartAdapter(this);
        viewPager.setAdapter(startAdapter);
        indicator.setViewPager(viewPager);
    }

    private void pageChange() {
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                page = position;
                switch (position) {
                    case 2:
                        btnColor.setVisibility(View.GONE);
                        signInButton.setVisibility(View.VISIBLE);
                        break;
                    default:
                        btnColor.setVisibility(View.VISIBLE);
                        signInButton.setVisibility(View.GONE);
                        break;
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    public void showProgressDialog() {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setCancelable(false);
            mProgressDialog.setMessage("Loading...");
        }
        mProgressDialog.show();
    }

    public void hideProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
    }

    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);
            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
                Toast.makeText(LoginActivity.this, "Login gagal", Toast.LENGTH_SHORT).show();

            }
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        // Log.d(TAG, "firebaseAuthWithGoogle:" + acct.getId());
        showProgressDialog();
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            user = mAuth.getCurrentUser();
                            Toast.makeText(LoginActivity.this, "Sukses", Toast.LENGTH_SHORT).show();
                            updateUI(user);

                        } else {
                            Toast.makeText(LoginActivity.this, "Gagal, cek koneksi internet anda", Toast.LENGTH_SHORT).show();
                        }

                        hideProgressDialog();

                    }
                });
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    protected void onStart() {
        super.onStart();

        final FirebaseUser currentUser = mAuth.getCurrentUser();
        updateUI(currentUser);
    }

    private void updateUI(FirebaseUser user) {
        hideProgressDialog();
        if (user != null) {
            Intent intent = new Intent(this, MainApp.class);
            startActivity(intent);
        }
    }

    private class NotifOneSignal implements OneSignal.NotificationOpenedHandler {
        String imgUrl, title, contBody, date;
        @Override
        public void notificationOpened(OSNotificationOpenResult result) {
            JSONObject data = result.notification.payload.additionalData;

            imgUrl = data.optString("img", null);
            title = data.optString("title", null);
            contBody = data.optString("body", null);
            date = data.optString("date", null);

            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("data");
            databaseReference.child("img").setValue(imgUrl);
            databaseReference.child("title").setValue(title);
            databaseReference.child("body").setValue(contBody);
            databaseReference.child("date").setValue(date);
            databaseReference.keepSynced(true);

            mAuth = FirebaseAuth.getInstance();
            user = mAuth.getCurrentUser();

            DatabaseReference favRef = FirebaseDatabase.getInstance().getReference().child("favDat");
            favRef.child(user.getUid()).child("fav").setValue("nggak");

            Object activityToLaunch = LoginActivity.class;
            Intent intent = new Intent(getApplicationContext(), (Class<?>) activityToLaunch);
            intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }
    }

    private class StartAdapter extends PagerAdapter {
        Context context;
        LayoutInflater inflater;

        public StartAdapter(Context context) {
            this.context = context;
        }

        int[] list_img = {
                R.drawable.thinker,
                R.drawable.molor,
                R.drawable.ngantuk
        };

        String[] list_judul = {
                "Fitur pemberitahuan",
                "Tidak ada eksplorasi tanggal!",
                "Aplikasi yang Simpel"
        };

        String[] list_desc = {
                "Dapatkan pemberitahuan momen bersejarah sepanjang tahun dan bagikan poster kepada orang lain!",
                "Aplikasi yang humanis, anda dituntut untuk hafal momen atau tunggu setahun lagi.",
                "Sederhana dalam poster yang menarik dan bebas iklan!"
        };

        int[] list_bg = {
                getResources().getColor(R.color.biru),
                getResources().getColor(R.color.kuning),
                getResources().getColor(R.color.hijau)
        };

        @Override
        public int getCount() {
            return list_judul.length;
        }

        @Override
        public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
            return (view == object);
        }

        @NonNull
        @Override
        public Object instantiateItem(@NonNull ViewGroup container, int position) {
            inflater = (LayoutInflater) context.getSystemService(LAYOUT_INFLATER_SERVICE);
            View view = inflater.inflate(R.layout.item_start, container, false);
            RelativeLayout relativeLayout = view.findViewById(R.id.login_start);
            TextView tvJudulStart = view.findViewById(R.id.start_judul);
            TextView tvDescStart = view.findViewById(R.id.start_desc);
            ImageView imgStart = view.findViewById(R.id.imgStart);

            //relativeLayout.setBackgroundColor(list_bg[position]);
            tvJudulStart.setText(list_judul[position]);
            tvDescStart.setText(list_desc[position]);
            //imgStart.setImageResource(list_img[position]);

            Glide.with(view)
                    .load(list_img[position])
                    .apply(bitmapTransform(new BlurTransformation(25, 3)))
                    .into(imgStart);

            container.addView(view);
            return view;
        }

        @Override
        public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
            container.removeView((RelativeLayout) object);
        }
    }
}
