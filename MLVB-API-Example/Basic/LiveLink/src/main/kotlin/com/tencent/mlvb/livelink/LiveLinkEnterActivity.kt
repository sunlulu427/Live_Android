package com.tencent.mlvb.livelink

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import com.tencent.mlvb.common.MLVBBaseActivity

/**
 * Co-anchoring Entrance View
 * - Enter as an anchor [LiveLinkAnchorActivity]
 * - Enter as audience [LiveLinkAudienceActivity]
 */
class LiveLinkEnterActivity : MLVBBaseActivity() {

    companion object {
        private const val STEP_INPUT_USERID = 0
        private const val STEP_INPUT_ROLE = 1
        private const val STEP_INPUT_STREAM = 2

        private const val ROLE_UNKNOWN = -1
        private const val ROLE_ANCHOR = 0
        private const val ROLE_AUDIENCE = 1
    }

    private lateinit var mLayoutStreamId: LinearLayout
    private lateinit var mEditStreamId: EditText
    private lateinit var mLayoutUserId: LinearLayout
    private lateinit var mEditUserId: EditText
    private lateinit var mLayoutSelectRole: LinearLayout
    private lateinit var mButtonRoleAnchor: Button
    private lateinit var mButtonRoleAudience: Button
    private lateinit var mButtonNext: Button

    private var mUserId = ""
    private var mStreamId = ""
    private var mStateInput = STEP_INPUT_USERID
    private var mRoleSelected = ROLE_UNKNOWN

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.livelink_activity_live_link_enter)
        initView()
    }

    private fun initView() {
        mLayoutUserId = findViewById(R.id.ll_user_id)
        mEditUserId = findViewById(R.id.et_user_id)
        mLayoutStreamId = findViewById(R.id.ll_stream_id)
        mEditStreamId = findViewById(R.id.et_stream_id)
        initSelectRoleLayout()
        initNextButton()
    }

    private fun initSelectRoleLayout() {
        mLayoutSelectRole = findViewById(R.id.ll_role)
        mButtonRoleAnchor = findViewById(R.id.bt_anchor)
        mButtonRoleAudience = findViewById(R.id.bt_audience)

        mRoleSelected = ROLE_ANCHOR
        mButtonRoleAnchor.isSelected = true

        mButtonRoleAnchor.setOnClickListener {
            mRoleSelected = ROLE_ANCHOR
            mButtonRoleAnchor.isSelected = true
            mButtonRoleAudience.isSelected = false
        }

        mButtonRoleAudience.setOnClickListener {
            mRoleSelected = ROLE_AUDIENCE
            mButtonRoleAnchor.isSelected = false
            mButtonRoleAudience.isSelected = true
        }
    }

    private fun initNextButton() {
        mButtonNext = findViewById(R.id.btn_next)
        mButtonNext.setOnClickListener {
            when (mStateInput) {
                STEP_INPUT_USERID -> {
                    mUserId = mEditUserId.text.toString()
                    if (mUserId.trim().isEmpty()) {
                        showToast(R.string.livelink_please_input_userid)
                        return@setOnClickListener
                    }
                    showUserIdStep(false)
                    showRoleStep(true)
                    showStreamIdStep(false)
                    mStateInput = STEP_INPUT_ROLE
                }
                STEP_INPUT_ROLE -> {
                    if (mRoleSelected == ROLE_UNKNOWN) {
                        showToast(R.string.livelink_please_input_userid)
                        return@setOnClickListener
                    }
                    showUserIdStep(false)
                    showRoleStep(false)
                    showStreamIdStep(true)
                    mButtonNext.setText(
                        if (mRoleSelected == ROLE_ANCHOR) R.string.livelink_start_pusher
                        else R.string.livelink_start_play
                    )
                    mStateInput = STEP_INPUT_STREAM
                }
                STEP_INPUT_STREAM -> {
                    mStreamId = mEditStreamId.text.toString()
                    if (mStreamId.trim().isEmpty()) {
                        showToast(R.string.livelink_please_input_streamid)
                        return@setOnClickListener
                    }

                    val targetClass = if (mRoleSelected == ROLE_ANCHOR) {
                        LiveLinkAnchorActivity::class.java
                    } else {
                        LiveLinkAudienceActivity::class.java
                    }

                    val intent = Intent(this@LiveLinkEnterActivity, targetClass).apply {
                        putExtra("USER_ID", mUserId)
                        putExtra("STREAM_ID", mStreamId)
                    }
                    startActivity(intent)
                    finish()
                }
            }
        }
    }

    private fun showUserIdStep(visible: Boolean) {
        mLayoutUserId.visibility = if (visible) View.VISIBLE else View.GONE
    }

    private fun showRoleStep(visible: Boolean) {
        mLayoutSelectRole.visibility = if (visible) View.VISIBLE else View.GONE
    }

    private fun showStreamIdStep(visible: Boolean) {
        mLayoutStreamId.visibility = if (visible) View.VISIBLE else View.GONE
    }

    private fun showToast(resId: Int) {
        Toast.makeText(this, getString(resId), Toast.LENGTH_SHORT).show()
    }

    override fun onDestroy() {
        super.onDestroy()
        mUserId = ""
        mRoleSelected = ROLE_UNKNOWN
        mStreamId = ""
        mStateInput = STEP_INPUT_USERID
    }

    override fun onPermissionGranted() {
        // No specific action needed when permission is granted
    }
}