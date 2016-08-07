package com.dayandnightstudio.bebop;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.Window;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.amazonaws.mobile.AWSMobileClient;
import com.amazonaws.mobile.user.IdentityManager;

import java.util.regex.Pattern;

import redis.clients.jedis.Transaction;

public class MainActivity extends AppCompatActivity {
    /** The identity manager used to keep track of the current user account. */
    private IdentityManager identityManager;

    //dialog
    Typeface tfLight;
    Toast toast;
    TextView tvTitle2;
    EditText etGroupName, etPassword;
    Button bCancel, bJoin, bCreate, bRetry, bDismiss, bRate;
    RadioButton rbPublic, rbPrivate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Obtain a reference to the mobile client. It is created in the Application class,
        // but in case a custom Application class is not used, we initialize it here if necessary.
        AWSMobileClient.initializeMobileClientIfNecessary(this);

        // Obtain a reference to the mobile client. It is created in the Application class.
        final AWSMobileClient awsMobileClient = AWSMobileClient.defaultMobileClient();

        // Obtain a reference to the identity manager.
        identityManager = awsMobileClient.getIdentityManager();

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        setContentView(R.layout.activity_main);

        final Data data = Data.getData();

        tfLight = Typeface.createFromAsset(getAssets(), "fonts/Roboto-Light.ttf");

        TextView tvTitle = (TextView) findViewById(R.id.tvTitle);
        tvTitle.setTypeface(tfLight);

        TextView tvJoin = (TextView) findViewById(R.id.tvJoin);
        tvJoin.setTypeface(tfLight);

        TextView tvCreate = (TextView) findViewById(R.id.tvCreate);
        tvCreate.setTypeface(tfLight);

        TextView tvLogout = (TextView) findViewById(R.id.tvLogout);
        tvLogout.setTypeface(tfLight);

        final Animation aSlideIn = new TranslateAnimation(dp2px(-150), dp2px(0), 0, 0);
        aSlideIn.setDuration(250);
        aSlideIn.setInterpolator(new AccelerateDecelerateInterpolator());
        aSlideIn.setFillAfter(true);

        final Animation aSlideOut = new TranslateAnimation(dp2px(0), dp2px(-150), 0, 0);
        aSlideOut.setDuration(250);
        aSlideOut.setInterpolator(new AccelerateDecelerateInterpolator());
        aSlideOut.setFillAfter(true);

        final Animation aFadeIn = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade_in);
        final Animation aFadeOut = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade_out);

        final RelativeLayout rlMenu = (RelativeLayout) findViewById(R.id.rlMenu);
        final RelativeLayout rlMask = (RelativeLayout) findViewById(R.id.rlMask);

        final ImageButton ibMenu = (ImageButton) findViewById(R.id.ibMenu);
        ibMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(rlMenu.getVisibility() == View.GONE) {
                    rlMenu.setVisibility(View.VISIBLE);
                    rlMenu.startAnimation(aSlideIn);

                    rlMask.setVisibility(View.VISIBLE);
                    rlMask.startAnimation(aFadeIn);
                } else {
                    rlMenu.startAnimation(aSlideOut);
                    rlMenu.setVisibility(View.GONE);

                    rlMask.startAnimation(aFadeOut);
                    rlMask.setVisibility(View.GONE);
                }
            }
        });

        rlMask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ibMenu.performClick();
            }
        });

        RelativeLayout rlJoin = (RelativeLayout) findViewById(R.id.rlJoin);
        rlJoin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Dialog dialog = new Dialog(MainActivity.this);
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.setContentView(R.layout.dialog_join);

                tvTitle2 = (TextView) dialog.findViewById(R.id.tvTitle);
                tvTitle2.setTypeface(tfLight);

                etGroupName = (EditText) dialog.findViewById(R.id.etGroupName);
                etGroupName.setTypeface(tfLight);

                etPassword = (EditText) dialog.findViewById(R.id.etPassword);
                etPassword.setTypeface(tfLight);

                bCancel = (Button) dialog.findViewById(R.id.bCancel);
                bCancel.setTypeface(tfLight);
                bCancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });

                bJoin = (Button) dialog.findViewById(R.id.bJoin);
                bJoin.setTypeface(tfLight);
                bJoin.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        checkConnection();

                        String groupName = etGroupName.getText().toString();
                        if (groupName.isEmpty()) {
                            toast.makeText(getApplicationContext(), R.string.missing_group, Toast.LENGTH_SHORT).show();
                            return;
                        } else if (!isValidGroupName(groupName)) {
                            toast.makeText(getApplicationContext(), R.string.group_format, Toast.LENGTH_LONG).show();
                            return;
                        }

                        String temp = data.jedis.hget("group:" + groupName, "password");
                        if (temp == null) {
                            toast.makeText(getApplicationContext(), R.string.invalid_group, Toast.LENGTH_SHORT).show();
                            return;
                        } else if (!temp.isEmpty()) {
                            if (etPassword.getVisibility() == View.VISIBLE) {
                                String password = etPassword.getText().toString();
                                if (!isValidPassword(password)) {
                                    toast.makeText(getApplicationContext(), R.string.password_format, Toast.LENGTH_LONG).show();
                                    return;
                                } else if (!password.equals(temp)) {
                                    toast.makeText(getApplicationContext(), R.string.incorrect_group, Toast.LENGTH_SHORT).show();
                                    return;
                                }
                            } else {
                                etPassword.setVisibility(View.VISIBLE);
                                toast.makeText(getApplicationContext(), R.string.requires_password, Toast.LENGTH_SHORT).show();
                                return;
                            }
                        }

                        data.jedis.hincrBy("group:" + groupName, "nUsers", 1);
                        data.initGroup(groupName);
                        toast.makeText(getApplicationContext(), "Joined " + groupName, Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(MainActivity.this, GroupActivity.class));
                    }
                });

                dialog.show();
            }
        });

        RelativeLayout rlCreate = (RelativeLayout) findViewById(R.id.rlCreate);
        rlCreate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Dialog dialog = new Dialog(MainActivity.this);
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.setContentView(R.layout.dialog_create);

                tvTitle2 = (TextView) dialog.findViewById(R.id.tvTitle);
                tvTitle2.setTypeface(tfLight);

                etGroupName = (EditText) dialog.findViewById(R.id.etGroupName);
                etGroupName.setTypeface(tfLight);

                etPassword = (EditText) dialog.findViewById(R.id.etPassword);
                etPassword.setTypeface(tfLight);

                rbPublic = (RadioButton) dialog.findViewById(R.id.rbPublic);
                rbPublic.setTypeface(tfLight);
                rbPublic.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (rbPrivate.isChecked()) {
                            rbPrivate.setChecked(false);
                            etPassword.setVisibility(View.GONE);
                        }
                    }
                });

                rbPrivate = (RadioButton) dialog.findViewById(R.id.rbPrivate);
                rbPrivate.setTypeface(tfLight);
                rbPrivate.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (rbPublic.isChecked()) {
                            rbPublic.setChecked(false);
                            etPassword.setVisibility(View.VISIBLE);
                        }
                    }
                });

                bCancel = (Button) dialog.findViewById(R.id.bCancel);
                bCancel.setTypeface(tfLight);
                bCancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });

                bCreate = (Button) dialog.findViewById(R.id.bCreate);
                bCreate.setTypeface(tfLight);
                bCreate.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        checkConnection();

                        String groupName = etGroupName.getText().toString();
                        if (groupName.isEmpty()) {
                            toast.makeText(getApplicationContext(), R.string.missing_group, Toast.LENGTH_SHORT).show();
                            return;
                        } else if (!isValidGroupName(groupName)) {
                            toast.makeText(getApplicationContext(), R.string.group_format, Toast.LENGTH_LONG).show();
                            return;
                        }

                        String temp = data.jedis.hget("group:" + groupName, "password");
                        if (temp != null) {
                            toast.makeText(getApplicationContext(), R.string.group_unavailable, Toast.LENGTH_SHORT).show();
                            return;
                        }

                        String password = "";
                        if (!rbPublic.isChecked()) {
                            password = etPassword.getText().toString();
                            if (password.isEmpty()) {
                                toast.makeText(getApplicationContext(), R.string.missing_password, Toast.LENGTH_SHORT).show();
                                return;
                            } else if (!isValidPassword(password)) {
                                toast.makeText(getApplicationContext(), R.string.password_format, Toast.LENGTH_LONG).show();
                                return;
                            }
                        }

                        Transaction t = data.jedis.multi();
                        t.hset("group:" + groupName, "password", password);
                        t.hincrBy("group:" + groupName, "nUsers", 1);
                        t.exec();

                        data.initGroup(groupName);

                        toast.makeText(getApplicationContext(), "Created " + groupName, Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(MainActivity.this, GroupActivity.class));
                    }
                });

                dialog.show();
            }
        });

        RelativeLayout rlLogout = (RelativeLayout) findViewById(R.id.rlLogout);
        rlLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // The user is currently signed in with a provider. Sign out of that provider.
                identityManager.signOut();
                startActivity(new Intent(MainActivity.this, SignInActivity.class));
                finish();
                return;
            }
        });

        ImageView ivUserImage = (ImageView) findViewById(R.id.ivUserImage);
        ivUserImage.setImageBitmap(identityManager.getUserImage());

        GroupAdapter groupAdapter = new GroupAdapter(this, data.myGroups);

        ListView lvGroup = (ListView) findViewById(R.id.lvGroup);
        lvGroup.setAdapter(groupAdapter);

        if(data.nLogins == 1 && !data.wasShown) {
            data.wasShown = true;

            ibMenu.performClick();

            final Dialog dialog = new Dialog(MainActivity.this);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setContentView(R.layout.dialog_new_user);

            tvTitle2 = (TextView) dialog.findViewById(R.id.tvTitle);
            tvTitle2.setTypeface(tfLight);

            bDismiss = (Button) dialog.findViewById(R.id.bDismiss);
            bDismiss.setTypeface(tfLight);
            bDismiss.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                }
            });

            dialog.show();
        } else if(data.nLogins == 5 && !data.wasShown) {
            data.wasShown = true;

            final Dialog dialog = new Dialog(MainActivity.this);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setContentView(R.layout.dialog_rate);

            tvTitle2 = (TextView) dialog.findViewById(R.id.tvTitle);
            tvTitle2.setTypeface(tfLight);

            bCancel = (Button) dialog.findViewById(R.id.bCancel);
            bCancel.setTypeface(tfLight);
            bCancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                }
            });

            bRate = (Button) dialog.findViewById(R.id.bRate);
            bRate.setTypeface(tfLight);
            bRate.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + MainActivity.this.getPackageName())));
                }
            });

            dialog.show();
        }

        checkConnection();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (!AWSMobileClient.defaultMobileClient().getIdentityManager().isUserSignedIn()) {
            // In the case that the activity is restarted by the OS after the application
            // is killed we must redirect to the splash activity to handle the sign-in flow.
            Intent intent = new Intent(this, SplashActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            return;
        }

        final AWSMobileClient awsMobileClient = AWSMobileClient.defaultMobileClient();

        // pause/resume Mobile Analytics collection
        awsMobileClient.handleOnResume();
    }

    @Override
    protected void onPause() {
        super.onPause();

        // Obtain a reference to the mobile client.
        final AWSMobileClient awsMobileClient = AWSMobileClient.defaultMobileClient();

        // pause/resume Mobile Analytics collection
        awsMobileClient.handleOnPause();
    }

    public static float dp2px(float dp){
        DisplayMetrics metrics = Resources.getSystem().getDisplayMetrics();
        float px = dp * (metrics.densityDpi / 160f);
        return Math.round(px);
    }

    public boolean isValidGroupName(String groupName) {
        String pattern = "^[a-zA-Z0-9!@#$%^&*]{3,16}$";
        return Pattern.matches(pattern, groupName);
    }

    public boolean isValidPassword(String password) {
        String pattern = "^[a-zA-Z0-9!@#$%^&*]{6,18}$";
        return Pattern.matches(pattern, password);
    }

    private boolean isNetworkConnected() {
        ConnectivityManager manager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        return manager.getActiveNetworkInfo() != null;
    }

    public void checkConnection() {
        if(!isNetworkConnected()) {
            final Dialog dialog = new Dialog(MainActivity.this);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setContentView(R.layout.dialog_connect);
            dialog.setCanceledOnTouchOutside(false);

            tvTitle2 = (TextView) dialog.findViewById(R.id.tvTitle);
            tvTitle2.setTypeface(tfLight);

            bRetry = (Button) dialog.findViewById(R.id.bRetry);
            bRetry.setTypeface(tfLight);
            bRetry.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(isNetworkConnected())
                        dialog.dismiss();
                }
            });

            dialog.show();
        }
    }
}
