package net.kdt.pojavlaunch;

import android.os.Bundle;
import android.widget.ImageButton;

public class SettingsActivity extends BaseActivity {
    private ImageButton mReturnButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        bindViews();

        mReturnButton.setOnClickListener(v -> {
            finish();
            overridePendingTransition(R.anim.cut_into, R.anim.cut_out);
        });
    }

    private void bindViews(){
        mReturnButton = findViewById(R.id.settings_return_button);
    }
}
