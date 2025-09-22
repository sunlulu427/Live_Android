package com.tencent.mlvb.livelink;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;
import com.tencent.mlvb.common.MLVBBaseActivity;

/**
 * Co-anchoring Entrance View
 * - Enter as an anchor [LiveLinkAnchorActivity]
 * - Enter as audience [LiveLinkAudienceActivity]
 */
public class LiveLinkEnterActivity extends MLVBBaseActivity {

    private static final int STEP_INPUT_USERID = 0;
    private static final int STEP_INPUT_ROLE = 1;
    private static final int STEP_INPUT_STREAM = 2;

    private static final int ROLE_UNKNOWN = -1;
    private static final int ROLE_ANCHOR = 0;
    private static final int ROLE_AUDIENCE = 1;

    private LinearLayout mLayoutStreamId;
    private EditText mEditStreamId;
    private LinearLayout mLayoutUserId;
    private EditText mEditUserId;
    private LinearLayout mLayoutSelectRole;
    private Button mButtonRoleAnchor;
    private Button mButtonRoleAudience;
    private Button mButtonNext;

    private String mUserId = "";
    private String mStreamId = "";
    private int mStateInput = STEP_INPUT_USERID;
    private int mRoleSelected = ROLE_UNKNOWN;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.livelink_activity_live_link_enter);
        initView();
    }

    private void initView() {
        mLayoutUserId = findViewById(R.id.ll_user_id);
        mEditUserId = findViewById(R.id.et_user_id);
        mLayoutStreamId = findViewById(R.id.ll_stream_id);
        mEditStreamId = findViewById(R.id.et_stream_id);
        initSelectRoleLayout();
        initNextButton();
    }

    private void initSelectRoleLayout() {
        mLayoutSelectRole = findViewById(R.id.ll_role);
        mButtonRoleAnchor = findViewById(R.id.bt_anchor);
        mButtonRoleAudience = findViewById(R.id.bt_audience);

        mRoleSelected = ROLE_ANCHOR;
        mButtonRoleAnchor.setSelected(true);

        mButtonRoleAnchor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mRoleSelected = ROLE_ANCHOR;
                mButtonRoleAnchor.setSelected(true);
                mButtonRoleAudience.setSelected(false);
            }
        });

        mButtonRoleAudience.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mRoleSelected = ROLE_AUDIENCE;
                mButtonRoleAnchor.setSelected(false);
                mButtonRoleAudience.setSelected(true);
            }
        });
    }

    private void initNextButton() {
        mButtonNext = findViewById(R.id.btn_next);
        mButtonNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (mStateInput) {
                    case STEP_INPUT_USERID:
                        mUserId = mEditUserId.getText().toString();
                        if (mUserId.trim().isEmpty()) {
                            showToast(R.string.livelink_please_input_userid);
                            return;
                        }
                        showUserIdStep(false);
                        showRoleStep(true);
                        showStreamIdStep(false);
                        mStateInput = STEP_INPUT_ROLE;
                        break;
                    case STEP_INPUT_ROLE:
                        if (mRoleSelected == ROLE_UNKNOWN) {
                            showToast(R.string.livelink_please_input_userid);
                            return;
                        }
                        showUserIdStep(false);
                        showRoleStep(false);
                        showStreamIdStep(true);
                        mButtonNext.setText(
                            mRoleSelected == ROLE_ANCHOR ? R.string.livelink_start_pusher : R.string.livelink_start_play
                        );
                        mStateInput = STEP_INPUT_STREAM;
                        break;
                    case STEP_INPUT_STREAM:
                        mStreamId = mEditStreamId.getText().toString();
                        if (mStreamId.trim().isEmpty()) {
                            showToast(R.string.livelink_please_input_streamid);
                            return;
                        }

                        Class<?> targetClass = mRoleSelected == ROLE_ANCHOR ?
                            LiveLinkAnchorActivity.class : LiveLinkAudienceActivity.class;

                        Intent intent = new Intent(LiveLinkEnterActivity.this, targetClass);
                        intent.putExtra("USER_ID", mUserId);
                        intent.putExtra("STREAM_ID", mStreamId);
                        startActivity(intent);
                        finish();
                        break;
                }
            }
        });
    }

    private void showUserIdStep(boolean visible) {
        mLayoutUserId.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    private void showRoleStep(boolean visible) {
        mLayoutSelectRole.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    private void showStreamIdStep(boolean visible) {
        mLayoutStreamId.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    private void showToast(int resId) {
        Toast.makeText(this, getString(resId), Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mUserId = "";
        mRoleSelected = ROLE_UNKNOWN;
        mStreamId = "";
        mStateInput = STEP_INPUT_USERID;
    }

    @Override
    public void onPermissionGranted() {
        // No specific action needed when permission is granted
    }
}