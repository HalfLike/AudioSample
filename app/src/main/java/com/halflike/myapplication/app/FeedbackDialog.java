package com.halflike.myapplication.app;


import android.app.Activity;
import android.app.Dialog;
import android.graphics.drawable.ColorDrawable;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.*;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.halflike.idea.app.R;
import com.halflike.myapplication.nettool.HttpManager;
import com.halflike.myapplication.nettool.HttpRequest;
import org.apache.http.Header;
import org.apache.http.message.BasicHeader;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by luox on 14/12/14.
 */
public class FeedbackDialog {
    private Activity mActivity;
    private DisplayMetrics dm;
    private Button commitBtn;
    private Button cancelBtn;
    private TextView contentTv;

    public int mDialogHeight = 320;
    public int mDialogWidth = 310;

    public FeedbackDialog(Activity activity) {
        this.mActivity = activity;
        dm = mActivity.getResources().getDisplayMetrics();
    }

    public void showDialog() {
        LayoutInflater inflater = mActivity.getLayoutInflater();
        LinearLayout rootView = (LinearLayout) inflater.inflate(R.layout.layout_feedback, null);
        commitBtn = (Button)rootView.findViewById(R.id.feedback_yes);
        cancelBtn = (Button)rootView.findViewById(R.id.feedback_no);
        contentTv = (TextView)rootView.findViewById(R.id.feedback_content);

        final Dialog dialog = new Dialog(mActivity);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(rootView);
        Window dWindow = dialog.getWindow();
        WindowManager.LayoutParams params = dWindow.getAttributes();
        params.width = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, mDialogWidth, dm);
        params.height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, mDialogHeight, dm);
        dWindow.setGravity(Gravity.CENTER);
        dWindow.setBackgroundDrawable(new ColorDrawable(android.R.color.transparent));
        dWindow.setAttributes(params);
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();

        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        commitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String messageContent = contentTv.getText().toString();
                if ("".equals(messageContent) || messageContent == null) {
                    dialog.dismiss();
                    return;
                }
                HttpRequest request = new HttpRequest("/api/feedback");
                JSONObject jsonBody = new JSONObject();
                try {
                    jsonBody.put("DeviceId", ((MainActivity)mActivity).deviceId);
                    jsonBody.put("CreatedTime", (((MainActivity)mActivity).getDateTime()));
                    jsonBody.put("MessageBody", messageContent);
                    jsonBody.put("MessageType", (((MainActivity)mActivity).MESSAGETYPE_NOT_REPLIED));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                request.setBody(jsonBody.toString());
                Header[] headers = {new BasicHeader("Content-Type", "application/json")};
                request.setHeaders(headers);
                HttpManager.getInstance().post(request, new MainActivity.CommonHttpResponse());
                dialog.dismiss();
            }
        });
    }
}
