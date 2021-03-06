package com.odong.pomodoro;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.Html;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.ToggleButton;
import com.odong.pomodoro.store.Storage;
import com.odong.pomodoro.utils.TaskQueue;

public class MainActivity extends Activity {
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_actions, menu);
        return super.onCreateOptionsMenu(menu);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_add_task:
                startActivity(new Intent(this, TaskAddActivity.class));
                break;
            case R.id.action_settings:
                if (((ToggleButton) findViewById(R.id.btn_main_switcher)).isChecked()) {
                    Constants.alert(MainActivity.this, getString(R.string.lbl_task_on_running));
                } else {
                    startActivity(new Intent(this, SettingsActivity.class));
                }
                break;
            case R.id.action_help:
                onMessage(R.string.help_title, R.string.help_body, R.drawable.ic_action_about);
                break;
            case R.id.action_about:
                onMessage(R.string.about_title, R.string.about_body, R.drawable.ic_action_help);
                break;

            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);

        Storage.set(this);

        tvClock = (TextView) findViewById(R.id.tv_main_clock);
        tvNextEvent = (TextView) findViewById(R.id.tv_main_next_event);

        initSwitcher();

    }

    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setMessage(R.string.dlg_exit_message)
                .setCancelable(false)
                .setPositiveButton(R.string.btn_yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        MainActivity.this.finish();
                    }
                })
                .setNegativeButton(R.string.btn_no, null)
                .show();

    }

    @Override
    protected void onResume() {
        refreshStatus();
        super.onResume();
    }


    private void initSwitcher() {
        final CountDownTimer timer = new CountDownTimer(30 * 1000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                tvClock.setText("" + millisUntilFinished / 1000);
            }

            @Override
            public void onFinish() {
                tvClock.setText(R.string.lbl_done);
            }
        };
        ((ToggleButton) findViewById(R.id.btn_main_switcher)).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    timer.start();
                } else {
                    timer.cancel();
                }
            }
        });
    }

    private void onMessage(int title, int body, int icon) {
        Intent intent = new Intent(this, MessageActivity.class);
        intent.putExtra("title", title);
        intent.putExtra("body", body);
        intent.putExtra("icon", icon);
        startActivity(intent);
    }

    private void setTextViewText(int id, int message, Object... args) {
        TextView status = (TextView) findViewById(id);
        String text = String.format(getResources().getString(message), args);
        status.setText(Html.fromHtml(text));
    }

    private void refreshStatus() {
        SharedPreferences sp = getSharedPreferences(Constants.STORAGE_SETTINGS_NAME,0);
//        sp.getInt(Constants.KEY_TASK_COUNTER, getResources().getInteger(R.integer.sp_default_settings_tasks));
        TaskQueue.set(
                sp.getInt(Constants.KEY_TASK_COUNTER, getResources().getInteger(R.integer.sp_default_settings_tasks)),
                sp.getInt(Constants.KEY_TASK_TIMER, getResources().getInteger(R.integer.sp_default_settings_timer)),
                sp.getInt(Constants.KEY_TASK_SHORT_BREAK, getResources().getInteger(R.integer.sp_default_settings_short_break)),
                sp.getInt(Constants.KEY_TASK_LONGER_BREAK, getResources().getInteger(R.integer.sp_default_settings_longer_break))
        );

        TaskQueue tq = TaskQueue.getInstance();

        setTextViewText(R.id.tv_main_settings, R.string.lbl_current_settings,
                tq.getSize(),
                tq.getTimer(),
                tq.getShortBreak(),
                tq.getLongerBreak()
        );
    }



    private void onRefresh() {
        //setTextViewText(R.id.tv_main_next_event, R.string.lbl_next_event, getString(R.string.lbl_task));
        setTextViewText(R.id.tv_main_clock, R.string.lbl_clock, 0, 42, 56);
    }

    private TextView tvNextEvent;
    private TextView tvClock;

}
