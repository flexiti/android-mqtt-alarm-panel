/*
 * <!--
 *   ~ Copyright (c) 2017. ThanksMister LLC
 *   ~
 *   ~ Licensed under the Apache License, Version 2.0 (the "License");
 *   ~ you may not use this file except in compliance with the License. 
 *   ~ You may obtain a copy of the License at
 *   ~
 *   ~ http://www.apache.org/licenses/LICENSE-2.0
 *   ~
 *   ~ Unless required by applicable law or agreed to in writing, software distributed 
 *   ~ under the License is distributed on an "AS IS" BASIS, 
 *   ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 *   ~ See the License for the specific language governing permissions and 
 *   ~ limitations under the License.
 *   -->
 */

package com.thanksmister.iot.mqtt.alarmpanel;

import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;

import com.thanksmister.iot.mqtt.alarmpanel.data.stores.StoreManager;
import com.thanksmister.iot.mqtt.alarmpanel.network.model.Daily;
import com.thanksmister.iot.mqtt.alarmpanel.ui.Configuration;
import com.thanksmister.iot.mqtt.alarmpanel.ui.views.AlarmDisableView;
import com.thanksmister.iot.mqtt.alarmpanel.ui.views.ArmOptionsView;
import com.thanksmister.iot.mqtt.alarmpanel.ui.views.ExtendedForecastView;

import butterknife.ButterKnife;

abstract public class BaseActivity extends AppCompatActivity {

    private StoreManager storeManager;
    private Configuration configuration;
    private AlertDialog progressDialog;
    private AlertDialog dialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
    }
    
    @Override
    protected void onDestroy() {

        super.onDestroy();

        ButterKnife.unbind(this);

        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
            dialog = null;
        }

        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
            progressDialog = null;
        }
    }
    
    public StoreManager getStoreManager() {
        if (storeManager == null) {
            BaseApplication baseApplication = BaseApplication.getInstance();
            storeManager = new StoreManager(getApplicationContext(), getContentResolver(), baseApplication.getAppSharedPreferences());
        }
        return storeManager;
    }
    
    public Configuration getConfiguration() {
        if (configuration == null) {
            BaseApplication baseApplication = BaseApplication.getInstance();
            configuration = new Configuration(getApplicationContext(), baseApplication.getAppSharedPreferences());
        }
        return configuration;
    }

    public void showProgressDialog(String message, boolean modal) {
        if (progressDialog != null) {
            return;
        }

        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View dialogView = inflater.inflate(R.layout.dialog_progress, null, false);
        TextView progressDialogMessage = (TextView) dialogView.findViewById(R.id.progressDialogMessage);
        progressDialogMessage.setText(message);

        progressDialog = new AlertDialog.Builder(this)
                .setCancelable(modal)
                .setView(dialogView)
                .show();
    }

    public void showProgressDialog() {
        if (progressDialog != null) {
            return;
        }

        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View dialogView = inflater.inflate(R.layout.dialog_progress_no_text, null, false);
        progressDialog = new AlertDialog.Builder(this)
                .setCancelable(false)
                .setView(dialogView)
                .show();
    }

    public void hideProgressDialog() {
        if (progressDialog != null) {
            progressDialog.dismiss();
            progressDialog = null;
        }
    }
    
    public void hideDialog() {
        if (dialog != null) {
            dialog.dismiss();
            dialog = null;
        }
    }
    
    public void showAlertDialog(String message, DialogInterface.OnClickListener onClickListener) {
        hideDialog();
        dialog = new AlertDialog.Builder(this)
                .setMessage(Html.fromHtml(message))
                .setPositiveButton(android.R.string.ok, onClickListener)
                .show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home ) {
            return true;
        } 
        return false;
    }
    
    public void showArmOptionsDialog(ArmOptionsView.ViewListener armListener) {
        hideDialog();
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.dialog_alarm_options, null, false);
        final ArmOptionsView optionsView = (ArmOptionsView) view.findViewById(R.id.armOptionsView);
        optionsView.setListener(armListener);
        dialog = new AlertDialog.Builder(BaseActivity.this)
                .setCancelable(true)
                .setView(view)
                .show();
    }
    
    public void showAlarmDisableDialog(AlarmDisableView.ViewListener alarmCodeListener, int code) {
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.dialog_alarm_disable, null, false);
        final AlarmDisableView alarmCodeView = view.findViewById(R.id.alarmDisableView);
        alarmCodeView.setListener(alarmCodeListener);
        alarmCodeView.setCode(code);
        alarmCodeView.startCountDown(configuration.getPendingTime());
        dialog = new AlertDialog.Builder(BaseActivity.this)
                .setCancelable(true)
                .setView(view)
                .show();
    }

    public void showExtendedForecastDialog(Daily daily) {
        hideDialog();
        Rect displayRectangle = new Rect();
        Window window = getWindow();
        window.getDecorView().getWindowVisibleDisplayFrame(displayRectangle);
        
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.dialog_extended_forecast, null, false);
        view.setMinimumWidth((int)(displayRectangle.width() * 0.7f));
        //view.setMinimumHeight((int)(displayRectangle.height() * 0.7f));
        final ExtendedForecastView  extendedForecastView = (ExtendedForecastView) view.findViewById(R.id.extendedForecastView);
        extendedForecastView.setExtendedForecast(daily, getConfiguration().getWeatherUnits());
        dialog = new AlertDialog.Builder(BaseActivity.this)
                .setCancelable(true)
                .setView(view)
                .show();
    }
}