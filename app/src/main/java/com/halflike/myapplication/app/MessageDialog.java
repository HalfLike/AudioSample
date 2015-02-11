package com.halflike.myapplication.app;


import android.app.Activity;
import android.app.Dialog;
import android.graphics.drawable.ColorDrawable;
import android.text.method.ScrollingMovementMethod;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.*;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.halflike.idea.app.R;
import com.halflike.myapplication.logger.Logger;
import com.halflike.myapplication.nettool.HttpManager;
import com.halflike.myapplication.nettool.HttpRequest;
import com.halflike.myapplication.nettool.HttpResponse;
import org.apache.http.Header;
import org.apache.http.message.BasicHeader;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by luox on 14/12/14.
 */
public class MessageDialog {
    private Activity mActivity;
    private DisplayMetrics dm;
    private Button closeBtn;
    private TextView contentTv;

    public int mDialogHeight = 320;
    public int mDialogWidth = 310;

    public MessageDialog(Activity activity) {
        this.mActivity = activity;
        dm = mActivity.getResources().getDisplayMetrics();
    }

    public void showDialog() {
        LayoutInflater inflater = mActivity.getLayoutInflater();
        LinearLayout rootView = (LinearLayout) inflater.inflate(R.layout.layout_message, null);
        closeBtn = (Button)rootView.findViewById(R.id.btn);
        contentTv = (TextView)rootView.findViewById(R.id.content);
        contentTv.setText(((MainActivity)mActivity).systemMessage);
        ((MainActivity)mActivity).systemMessage = "";
        ((MainActivity)mActivity).hasMessage = false;

        final Dialog dialog = new Dialog(mActivity);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(rootView);
        Window dWindow = dialog.getWindow();
        WindowManager.LayoutParams params = dWindow.getAttributes();
        params.width = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, mDialogWidth, dm);
        params.height = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, mDialogHeight, dm);
        dWindow.setGravity(Gravity.CENTER);
        dWindow.setBackgroundDrawable(new ColorDrawable(android.R.color.transparent));
        dWindow.setAttributes(params);
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();

        TextView content = (TextView) rootView.findViewById(R.id.content);
        content.setMovementMethod(ScrollingMovementMethod.getInstance());
        closeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity)mActivity).querySystemMessage();
                dialog.dismiss();
            }
        });
    }


}
