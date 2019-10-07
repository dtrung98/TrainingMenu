package com.zalo.trainingmenu.newsfeed3d.photo3d;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;

import androidx.annotation.Nullable;

import com.ldt.menulayout.ui.permission.PermissionActivity;
import com.zalo.trainingmenu.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class ShaderSetActivity extends PermissionActivity {
    private static final String TAG = "Photo3DActivity";
    public static final String VERTEX_SHADER = "vertex_shader";
    public static final String FRAGMENT_SHADER = "fragment_shader";

    public static final String ACTION_SHADER_SET = "shader_set";

    @BindView(R.id.vertex_edit_text)
    EditText mVertexEditText;

    @BindView(R.id.fragment_edit_text)
    EditText mFragmentEditText;

    @OnClick(R.id.button)
    void goToPhoto3D(){
        String vertex = mVertexEditText.getText().toString();
        String fragment = mFragmentEditText.getText().toString();
        Intent intent = new Intent(this,Photo3DActivity.class);
        intent.setAction(ACTION_SHADER_SET);
        intent.putExtra(VERTEX_SHADER,vertex);
        intent.putExtra(FRAGMENT_SHADER,fragment);
        startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.shader_set_layout);
        ButterKnife.bind(this);
       mVertexEditText.setText(ShaderInstance.vertexShader);
       mFragmentEditText.setText(ShaderInstance.INSTANCE.getFragmentShader());
    }

    @OnClick(R.id.back_button)
    void back() {
        finish();
    }
}
