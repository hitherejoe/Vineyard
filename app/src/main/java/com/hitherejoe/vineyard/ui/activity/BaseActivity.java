package com.hitherejoe.vineyard.ui.activity;

import android.app.Activity;
import android.app.FragmentManager;
import android.view.MenuItem;

import com.hitherejoe.vineyard.VineyardApplication;
import com.hitherejoe.vineyard.injection.component.ApplicationComponent;

public class BaseActivity extends Activity {

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                FragmentManager fm = getFragmentManager();
                if (fm.getBackStackEntryCount() > 0) {
                    fm.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                } else {
                    finish();
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    protected ApplicationComponent applicationComponent() {
        return VineyardApplication.get(this).getComponent();
    }

}
