package com.payment.com;

import android.animation.ObjectAnimator;
import android.app.AlertDialog;
import android.content.Context;
import android.Manifest;
import android.content.pm.PackageManager;
import android.content.DialogInterface;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.net.Uri;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.Settings;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    private static final String API = "https://akashpay-bot.axura.workers.dev";
    private RequestQueue queue;
    private android.content.SharedPreferences prefs;
    private LinearLayout content;
    private TextView connectionStatus, clientValue, apiValue, logSummary;
    private String clientId, apiKey, deviceId;

    private int dp(float value) { return (int) (value * getResources().getDisplayMetrics().density + 0.5f); }
    private TextView label(String text, float size, int color) { TextView v = new TextView(this); v.setText(text); v.setTextSize(size); v.setTextColor(color); v.setTypeface(Typeface.create("sans-serif",Typeface.NORMAL)); v.setPadding(dp(2), dp(2), dp(2), dp(2)); return v; }
    private GradientDrawable bg(int color, float radius) { GradientDrawable d = new GradientDrawable(); d.setColor(color); d.setCornerRadius(dp(radius)); return d; }
    private LinearLayout card() { LinearLayout v = new LinearLayout(this); v.setOrientation(LinearLayout.VERTICAL); v.setPadding(dp(18),dp(16),dp(18),dp(16)); v.setBackground(bg(Color.rgb(16,29,49), 20)); LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(-1,-2); p.setMargins(0,dp(8),0,dp(8)); v.setLayoutParams(p); return v; }
    private Button action(String text) { Button b = new Button(this); b.setText(text); b.setTextColor(Color.WHITE); b.setTextSize(14); b.setAllCaps(false); b.setTypeface(Typeface.create("sans-serif",Typeface.NORMAL), Typeface.BOLD); b.setBackgroundResource(R.drawable.premium_button); b.setPadding(dp(14),0,dp(14),0); LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(0,dp(54),1); p.setMargins(dp(5),dp(5),dp(5),dp(5)); b.setLayoutParams(p); return b; }
    private Button copyButton(String text, String value) { Button b=action(text); b.setTextSize(12); b.setOnClickListener(v->{ ((ClipboardManager)getSystemService(CLIPBOARD_SERVICE)).setPrimaryClip(ClipData.newPlainText(text,value)); Toast.makeText(this,text+" copied",Toast.LENGTH_SHORT).show(); }); return b; }
    private void pulse(View v) { AlphaAnimation a = new AlphaAnimation(0.72f,1f); a.setDuration(1100); a.setRepeatMode(AlphaAnimation.REVERSE); a.setRepeatCount(AlphaAnimation.INFINITE); v.startAnimation(a); }

    @Override public void onCreate(Bundle state) { super.onCreate(state); setContentView(R.layout.activity_main); queue= Volley.newRequestQueue(this); prefs=getSharedPreferences(getString(R.string.app_name), MODE_PRIVATE); clientId=prefs.getString("user_email",""); apiKey=prefs.getString("device_key",""); deviceId=prefs.getString("device_ip",""); buildUi(); refreshAll(); showPermissionsIfNeeded(); showBatteryOptimizationPrompt(); }

    private void showPermissionsIfNeeded(){ boolean sms=ContextCompat.checkSelfPermission(this,Manifest.permission.RECEIVE_SMS)==PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(this,Manifest.permission.READ_SMS)==PackageManager.PERMISSION_GRANTED; boolean notify=android.os.Build.VERSION.SDK_INT<33 || ContextCompat.checkSelfPermission(this,Manifest.permission.POST_NOTIFICATIONS)==PackageManager.PERMISSION_GRANTED; if(!sms || !notify){ new AlertDialog.Builder(this).setTitle("Keep AkashPay active").setMessage("Allow SMS access for bKash and Nagad payment confirmations and notifications for sync alerts. Other messages are ignored.").setPositiveButton("Allow securely",(d,w)->ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.RECEIVE_SMS,Manifest.permission.READ_SMS,Manifest.permission.POST_NOTIFICATIONS},701)).setNegativeButton("Later",null).show(); } }
    private void showBatteryOptimizationPrompt(){ if(android.os.Build.VERSION.SDK_INT>=23){ PowerManager pm=(PowerManager)getSystemService(POWER_SERVICE); if(pm!=null && !pm.isIgnoringBatteryOptimizations(getPackageName())){ new AlertDialog.Builder(this).setTitle("Allow background monitoring").setMessage("Battery optimization can stop SMS monitoring after a few minutes. Allow AkashPay to run reliably in the background.").setPositiveButton("Open battery settings",(d,w)->{ try{ startActivity(new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS, Uri.parse("package:"+getPackageName()))); }catch(Exception e){ startActivity(new Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)); } }).setNegativeButton("Later",null).show(); } } }

    private void buildUi() {
        LinearLayout root = new LinearLayout(this); root.setOrientation(LinearLayout.VERTICAL); root.setPadding(dp(16),dp(18),dp(16),dp(10)); root.setBackground(bg(Color.rgb(7,17,31),0));
        LinearLayout header = new LinearLayout(this); header.setGravity(Gravity.CENTER_VERTICAL); ImageView logo=new ImageView(this); logo.setImageResource(R.drawable.akashpay_logo); logo.setScaleType(ImageView.ScaleType.CENTER_CROP); logo.setBackground(bg(Color.rgb(34,211,238),16)); header.addView(logo,new LinearLayout.LayoutParams(dp(52),dp(52))); LinearLayout titleBox=new LinearLayout(this); titleBox.setOrientation(LinearLayout.VERTICAL); titleBox.setPadding(dp(12),0,0,0); TextView title=label("AkashPay",25,Color.WHITE); title.setTypeface(Typeface.DEFAULT,Typeface.BOLD); titleBox.addView(title); titleBox.addView(label("Native merchant control",12,Color.rgb(155,176,201))); header.addView(titleBox,new LinearLayout.LayoutParams(0,-2,1)); root.addView(header);
        connectionStatus=label("●  Checking Cloudflare connection…",13,Color.rgb(94,234,212)); connectionStatus.setPadding(0,dp(12),0,dp(6)); root.addView(connectionStatus);
        ScrollView scroll=new ScrollView(this); scroll.setFillViewport(true); content=new LinearLayout(this); content.setOrientation(LinearLayout.VERTICAL);
        LinearLayout identity=card(); identity.addView(label("CLIENT ID",11,Color.rgb(155,176,201))); clientValue=label(clientId.isEmpty()?"Not connected":clientId,16,Color.WHITE); clientValue.setTypeface(Typeface.DEFAULT,Typeface.BOLD); identity.addView(clientValue); identity.addView(label("API KEY",11,Color.rgb(155,176,201))); apiValue=label(apiKey.isEmpty()?"Not available":"••••••••••••"+apiKey.substring(Math.max(0,apiKey.length()-6)),14,Color.rgb(94,234,212)); identity.addView(apiValue); LinearLayout copyRow=new LinearLayout(this); copyRow.setOrientation(LinearLayout.HORIZONTAL); copyRow.addView(copyButton("Copy Client ID",clientId)); copyRow.addView(copyButton("Copy API key",apiKey)); identity.addView(copyRow); content.addView(identity);
        LinearLayout actions=new LinearLayout(this); actions.setOrientation(LinearLayout.HORIZONTAL); Button sync=action("Sync now"); Button numbers=action("Gateway numbers"); actions.addView(sync); actions.addView(numbers); content.addView(actions); pulse(sync);
        LinearLayout actions2=new LinearLayout(this); actions2.setOrientation(LinearLayout.HORIZONTAL); Button logs=action("SMS logs"); Button orders=action("Orders"); actions2.addView(logs); actions2.addView(orders); content.addView(actions2);
        LinearLayout statusCard=card(); statusCard.addView(label("LIVE ACTIVITY",11,Color.rgb(155,176,201))); logSummary=label("Loading accepted bKash and Nagad logs…",14,Color.WHITE); logSummary.setPadding(0,dp(8),0,dp(4)); statusCard.addView(logSummary); content.addView(statusCard);
        TextView hint=label("Only bKash and Nagad payment confirmations are forwarded. Other SMS remain private on this device.",12,Color.rgb(155,176,201)); hint.setPadding(dp(4),dp(10),dp(4),dp(20)); content.addView(hint); scroll.addView(content); root.addView(scroll,new LinearLayout.LayoutParams(-1,0,1)); setContentView(root);
        sync.setOnClickListener(v->refreshAll()); numbers.setOnClickListener(v->showNumbersDialog()); logs.setOnClickListener(v->showLogs()); orders.setOnClickListener(v->showOrders());
    }

    private Map<String,String> headers() { Map<String,String> h=new HashMap<>(); h.put("X-API-Key",apiKey); h.put("X-Device-ID",deviceId); return h; }
    private void get(String path, final VolleyCallback callback) { StringRequest r=new StringRequest(Request.Method.GET,API+path,resp->callback.done(resp),err->{connectionStatus.setText("●  Cloudflare connection unavailable"); connectionStatus.setTextColor(Color.rgb(248,113,113));}){ @Override public Map<String,String> getHeaders(){return headers();} }; queue.add(r); }
    private void patchNumbers(String bkash,String nagad) { JSONObject b=new JSONObject(); try { JSONObject n=new JSONObject(); n.put("bkash",bkash); n.put("nagad",nagad); b.put("numbers",n); }catch(Exception ignored){} StringRequest r=new StringRequest(Request.Method.PATCH,API+"/v1/public/settings/"+clientId+"/numbers",resp->{Toast.makeText(this,"Gateway numbers saved",Toast.LENGTH_SHORT).show();},err->Toast.makeText(this,"Could not save numbers",Toast.LENGTH_SHORT).show()){ @Override public byte[] getBody(){return b.toString().getBytes();} @Override public String getBodyContentType(){return "application/json; charset=utf-8";} @Override public Map<String,String> getHeaders(){return headers();} }; queue.add(r); }
    private void refreshAll(){ if(clientId.isEmpty()||apiKey.isEmpty()){connectionStatus.setText("●  Connect this device to provision Client ID"); connectionStatus.setTextColor(Color.rgb(251,191,36)); return;} get("/v1/health",r->{connectionStatus.setText("●  Cloudflare D1 connected"); connectionStatus.setTextColor(Color.rgb(94,234,212));}); get("/v1/public/logs/"+clientId,r->{try{JSONObject o=new JSONObject(r); int c=o.optJSONObject("logs") == null?0:o.optJSONObject("logs").length(); logSummary.setText(c+" accepted payment SMS log(s) available\nLast sync: just now");}catch(Exception e){logSummary.setText("SMS logs ready");}}); }
    private void showNumbersDialog(){ final EditText bk=new EditText(this); bk.setHint("bKash number"); bk.setInputType(InputType.TYPE_CLASS_PHONE); final EditText ng=new EditText(this); ng.setHint("Nagad number"); ng.setInputType(InputType.TYPE_CLASS_PHONE); LinearLayout box=new LinearLayout(this); box.setOrientation(LinearLayout.VERTICAL); box.setPadding(dp(20),dp(4),dp(20),0); box.addView(bk); box.addView(ng); get("/v1/public/settings/"+clientId+"/numbers",r->{try{JSONObject n=new JSONObject(r).optJSONObject("numbers"); if(n!=null){bk.setText(n.optString("bkash"));ng.setText(n.optString("nagad"));}}catch(Exception ignored){}}); new AlertDialog.Builder(this).setTitle("Gateway numbers").setView(box).setPositiveButton("Save",(d,w)->patchNumbers(bk.getText().toString().trim(),ng.getText().toString().trim())).setNegativeButton("Cancel",null).show(); }
    private void showLogs(){ get("/v1/public/logs/"+clientId,r->showTextDialog("SMS logs",r)); }
    private void showOrders(){ get("/v1/public/orders/"+clientId,r->showTextDialog("Orders",r)); }
    private void showTextDialog(String title,String text){ TextView t=label(text,13,Color.DKGRAY); t.setPadding(dp(16),dp(10),dp(16),dp(10)); new AlertDialog.Builder(this).setTitle(title).setView(t).setPositiveButton("Close",null).show(); }
    public void updateNetworkStatus(boolean connected){ if(!connected){ connectionStatus.setText("●  Device is offline; retry queue is active"); connectionStatus.setTextColor(Color.rgb(251,191,36)); } else if(!clientId.isEmpty()){ connectionStatus.setText("●  Network available; syncing with Cloudflare"); connectionStatus.setTextColor(Color.rgb(94,234,212)); } }
    private interface VolleyCallback { void done(String response); }

    @Override public void onBackPressed(){ new AlertDialog.Builder(this).setTitle("Exit AkashPay?").setMessage("Background SMS monitoring will continue if the service is enabled.").setPositiveButton("Exit",(d,w)->finishAffinity()).setNegativeButton("Cancel",null).show(); }
}
