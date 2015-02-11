package com.halflike.myapplication.app;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.SharedPreferences;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;
import com.five.adwoad.AdListener;
import com.five.adwoad.AdwoAdView;
import com.five.adwoad.ErrorCode;
import com.halflike.idea.app.R;
import com.halflike.myapplication.logger.Logger;
import com.halflike.myapplication.nettool.HttpManager;
import com.halflike.myapplication.nettool.HttpRequest;
import com.halflike.myapplication.nettool.HttpResponse;
import org.apache.http.Header;
import org.apache.http.message.BasicHeader;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends Activity {

    public final int MESSAGETYPE_NOT_REPLIED = 1;
    public final int MESSAGETYPE_REPLIED = 2;
    public final int MESSAGETYPE_NOT_RECEIVED = 11;
    public final int MESSAGETYPE_RECEIVED = 12;
    public Menu menu;
    public boolean hasMessage = false;
    public String systemMessage = "";
    public DecibelSample mDB;
    public String deviceId;

    private Button mSampleBtn;
    private TextView mRealTimeTV;
    private TextView mAverageTV;
    private TextView mMaxTV;
    private boolean isSampling = false;

    public static RelativeLayout layout;
    static AdwoAdView adView = null;
    String Adwo_PID = "5fe4c07c79b74bcb9191dfb5fe2ff6cc";
    //String Adwo_PID = "2b8dbd92edd74a97b3ba6b0189bef125";
    LayoutParams params = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_layout);

        mSampleBtn = (Button) findViewById(R.id.sampleBtn);
        mRealTimeTV = (TextView) findViewById(R.id.realTimeDb);
        mAverageTV = (TextView) findViewById(R.id.averageDb);
        mMaxTV = (TextView) findViewById(R.id.maxDb);
        mSampleBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isSampling) {
                    isSampling = false;
                    mSampleBtn.setText(getString(R.string.db_start_sample));
                    if (mDB.isSampling()) {
                        mDB.stopSample();
                        mAverageTV.setText(floatToString(mDB.getMeanDb()));
                        mMaxTV.setText(floatToString(mDB.getMaxDb()));
                    }
                } else {
                    isSampling = true;
                    mSampleBtn.setText(getString(R.string.db_stop_sample));
                    mDB.startSample();
                }
            }
        });
        mDB = DecibelSample.getInstance();
        mDB.rateOneSecond = 10;
        mDB.setLisener(new DecibelSample.GetDbValueLisener() {
            @Override
            public void getDbValue(final float db) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mRealTimeTV.setText(floatToString(db));
                    }
                });
            }
        });

        //anwo广告接入
        layout = (RelativeLayout) findViewById(R.id.layout);
        params = new LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
		//当不设置广告条充满屏幕宽时建议放置在父容器中间
        params.addRule(RelativeLayout.CENTER_HORIZONTAL);
		//设置广告条充满屏幕宽
        AdwoAdView.setBannerMatchScreenWidth(true);
		//设置自定义广告条宽高
		//AdwoAdView.setDesirableBannerHeight(20);
        // 实例化广告对象
        adView = new AdwoAdView(this, Adwo_PID,false, 40);
		//设置无动画效果
		//adView.setAnimationType(0);
        // 设置广告监听回调
        adView.setListener(new AnwoAdListener());
        // 把广告条加入界面布局
        layout.addView(adView, params);

        TelephonyManager tm = (TelephonyManager) this.getSystemService(TELEPHONY_SERVICE);
        deviceId = tm.getDeviceId() + "-";
        WifiManager wifi = (WifiManager) getSystemService(this.WIFI_SERVICE);
        WifiInfo info = wifi.getConnectionInfo();
        deviceId += info.getMacAddress();
        uploadUserinfo();

        querySystemMessage();
    }

    private void uploadUserinfo() {
        HttpRequest request = new HttpRequest("/api/post_userinfo");
        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("DeviceId", deviceId);
            jsonBody.put("UseTimes", getUseTimes());
            jsonBody.put("LastUseTime", getDateTime());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        request.setBody(jsonBody.toString());
        Header[] headers = {new BasicHeader("Content-Type", "application/json")};
        request.setHeaders(headers);
        HttpManager.getInstance().post(request, new CommonHttpResponse());
    }

    public void querySystemMessage() {
        HttpRequest request = new HttpRequest("/api/get_message");
        request.put("DeviceId", deviceId);
        HttpManager.getInstance().get(request, new QueryHttpResponse());
    }

    public final String floatToString(float number) {
        DecimalFormat decimalFormat=new DecimalFormat("0.0");
        return decimalFormat.format(number);
    }

    public String getDateTime() {
        Date date = new Date();
        SimpleDateFormat format=new SimpleDateFormat("yyyy-MM-dd HH:mm");
        return format.format(date);
    }

    public int getUseTimes() {
        SharedPreferences data = this.getSharedPreferences("audio_sample",
                Activity.MODE_PRIVATE);
        String useTimes = data.getString("UseTimes", "0");
        int times = Integer.valueOf(useTimes) + 1;
        data.edit().putString("UseTimes", String.valueOf(times)).commit();
        return times;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        this.menu = menu;
        invalidMenu();
        return true;
    }

    public void invalidMenu() {
        if (hasMessage == true) {
            menu.findItem(R.id.action_message).setIcon(R.drawable.ic_icon_messages);
        } else {
            menu.findItem(R.id.action_message).setIcon(R.drawable.icon_messages);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_feedback) {
            new FeedbackDialog(this).showDialog();
        } else if (id == R.id.action_message) {
            this.getWindow().invalidatePanelMenu(Window.FEATURE_OPTIONS_PANEL);
            new MessageDialog(this).showDialog();
        }
        return super.onOptionsItemSelected(item);
    }

    static class CommonHttpResponse extends HttpResponse {
        @Override
        public void onSuccess(JSONObject json) {
            Logger.d(json.toString());
        }

        @Override
        public void onFailure(int statusCode, String msg) {
            Logger.d(statusCode + ". " + msg);
        }
    }

    class QueryHttpResponse extends HttpResponse {
        @Override
        public void onSuccess(JSONObject json) {
            Logger.d(json.toString());
            try {
                systemMessage = json.getString("MessageBody");
                hasMessage = true;
                invalidMenu();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onFailure(int statusCode, String msg) {
            Logger.d(statusCode + ". " + msg);
        }
    }

    class AnwoAdListener implements AdListener {

        @Override
        public void onReceiveAd(AdwoAdView adwoAdView) {
            Logger.d("called");
        }

        @Override
        public void onFailedToReceiveAd(AdwoAdView adwoAdView, ErrorCode errorCode) {
            Logger.d("called");
            if (errorCode != null) {
                Logger.d(errorCode.getErrorCode() + errorCode.getErrorString());
            }
        }

        @Override
        public void onPresentScreen() {
            Logger.d("called");
        }

        @Override
        public void onDismissScreen() {
            Logger.d("called");
        }
    }

}
