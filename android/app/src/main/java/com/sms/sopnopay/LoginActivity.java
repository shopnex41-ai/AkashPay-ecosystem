package com.payment.com;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.airbnb.lottie.LottieAnimationView;
import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.blogspot.atifsoftwares.animatoolib.Animatoo;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {
    private static final int PERMISSION_REQUEST = 601;
    private static final int NOTIFICATION_REQUEST = 602;
    private TextView userEmail, deviceKey;
    private CheckBox rememberMeCheckbox;
    private RequestQueue requestQueue;
    private LottieAnimationView lottie;
    private boolean permissionDialogShown = false;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_login);
        userEmail=findViewById(R.id.userEmail); deviceKey=findViewById(R.id.device_key); rememberMeCheckbox=findViewById(R.id.rememberMeCheckbox); lottie=findViewById(R.id.lottie); requestQueue=Volley.newRequestQueue(this);
        findViewById(R.id.loginButton).setOnClickListener(v -> requestPermissionsThenConnect());
        new android.os.Handler().postDelayed(() -> showPermissionIntroIfNeeded(), 350);
    }

    private boolean hasCorePermissions(){ boolean sms=ContextCompat.checkSelfPermission(this,Manifest.permission.RECEIVE_SMS)==PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(this,Manifest.permission.READ_SMS)==PackageManager.PERMISSION_GRANTED; boolean location=ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_COARSE_LOCATION)==PackageManager.PERMISSION_GRANTED; boolean notification=Build.VERSION.SDK_INT<33 || ContextCompat.checkSelfPermission(this,Manifest.permission.POST_NOTIFICATIONS)==PackageManager.PERMISSION_GRANTED; return sms && location && notification; }
    private void showPermissionIntroIfNeeded(){ if(!hasCorePermissions() && !permissionDialogShown){ permissionDialogShown=true; new AlertDialog.Builder(this).setTitle("Welcome to AkashPay").setMessage("To keep payment monitoring active, AkashPay needs SMS access for bKash and Nagad confirmations, notifications for sync alerts, and background connectivity. Other messages are ignored and never uploaded.").setPositiveButton("Continue securely",(d,w)->requestPermissionsThenConnect()).setNegativeButton("Later",null).setCancelable(false).show(); } else if(hasCorePermissions()){ handleLogin(); } }
    private void requestPermissionsThenConnect(){
        if(ContextCompat.checkSelfPermission(this,Manifest.permission.RECEIVE_SMS)!=PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(this,Manifest.permission.READ_SMS)!=PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_COARSE_LOCATION)!=PackageManager.PERMISSION_GRANTED){ ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.RECEIVE_SMS,Manifest.permission.READ_SMS,Manifest.permission.ACCESS_COARSE_LOCATION,Manifest.permission.ACCESS_FINE_LOCATION},PERMISSION_REQUEST); return; }
        if(Build.VERSION.SDK_INT>=33 && ContextCompat.checkSelfPermission(this,Manifest.permission.POST_NOTIFICATIONS)!=PackageManager.PERMISSION_GRANTED){ ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.POST_NOTIFICATIONS},NOTIFICATION_REQUEST); return; }
        handleLogin();
    }

    private void handleLogin(){
        String url="https://akashpay-bot.axura.workers.dev/v1/public/logs/device-connect"; String deviceIp=getAndroidId(this); lottie.setVisibility(View.VISIBLE);
        StringRequest req=new StringRequest(Request.Method.POST,url,response->{lottie.setVisibility(View.GONE); handleVerificationResponse(response);},error->{lottie.setVisibility(View.GONE); Toast.makeText(this,"Cloudflare connection unavailable. Tap Connect to retry.",Toast.LENGTH_LONG).show();}){
            @Override protected Map<String,String> getParams() throws AuthFailureError { Map<String,String> p=new HashMap<>(); p.put("user_email",""); p.put("device_key",""); p.put("device_ip",deviceIp); p.put("deviceName",Build.MANUFACTURER+" "+Build.MODEL); return p; }
            @Override public String getBodyContentType(){return "application/x-www-form-urlencoded; charset=UTF-8";}
        }; requestQueue.add(req);
    }
    private void handleVerificationResponse(String response){ try{ JSONObject o=new JSONObject(response); int status=o.optInt("status",o.optBoolean("ok",false)?1:0); if(status==1 || o.optBoolean("ok",false)){ SharedPreferences p=getSharedPreferences(getString(R.string.app_name),MODE_PRIVATE); String id=o.optString("user_email",o.optString("clientId","")); String key=o.optString("device_key",o.optString("apiKey","")); p.edit().putString("user_email",id).putString("device_key",key).putString("device_ip",getAndroidId(this)).apply(); Functions.createNotification(this,"AkashPay connected","Your Client ID and API key are ready."); startActivity(new Intent(this,MainActivity.class)); finish(); Animatoo.animateSwipeRight(this); } else Toast.makeText(this,"Connection could not be completed. Tap retry.",Toast.LENGTH_SHORT).show(); }catch(JSONException e){Toast.makeText(this,"Invalid server response. Please retry.",Toast.LENGTH_SHORT).show();} }
    public static String getAndroidId(Context context){return Settings.Secure.getString(context.getContentResolver(),Settings.Secure.ANDROID_ID);}
    @Override public void onRequestPermissionsResult(int requestCode,@NonNull String[] permissions,@NonNull int[] results){super.onRequestPermissionsResult(requestCode,permissions,results); if(requestCode==PERMISSION_REQUEST){ if(hasCorePermissions() || (ContextCompat.checkSelfPermission(this,Manifest.permission.RECEIVE_SMS)==PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(this,Manifest.permission.READ_SMS)==PackageManager.PERMISSION_GRANTED)){ requestPermissionsThenConnect(); } else Toast.makeText(this,"SMS permission is required for payment verification.",Toast.LENGTH_LONG).show(); } else if(requestCode==NOTIFICATION_REQUEST){ handleLogin(); }}
    @SuppressLint("MissingSuperCall") @Override public void onBackPressed(){new AlertDialog.Builder(this).setTitle("Exit AkashPay?").setMessage("Background payment monitoring will stop if the app is closed.").setPositiveButton("Exit",(d,w)->finishAffinity()).setNegativeButton("Cancel",null).show();}
}
