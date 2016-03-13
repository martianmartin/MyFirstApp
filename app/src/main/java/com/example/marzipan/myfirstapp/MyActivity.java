package com.example.marzipan.myfirstapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;


public class MyActivity extends AppCompatActivity {

    public final static String EXTRA_MESSAGE = "com.example.marzipan.myfirstapp.MESSAGE";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_my, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /* This method will be triggered when send button is clicked */
    public void sendMessage(View view) {
        Intent intent = new Intent(this, DisplayMessageActivity.class);
        EditText editText = (EditText) findViewById(R.id.edit_message);
        String message = editText.getText().toString();
        intent.putExtra(EXTRA_MESSAGE, message);
        startActivity(intent);
    }

    public void findOpponents(View view) {
        Intent intent = new Intent(this, FindOpponentActivity.class);
        startActivity(intent);
    }

    private void saveLatestSound(int latestInt) {
        // Save button value as the last pressed...
        SharedPreferences sharedPref = this.getSharedPreferences(
                getString(R.string.preference_file_key), this.MODE_PRIVATE);

        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt("latest_sound", latestInt);
        editor.commit();
    }

    /* Audio playback buttons */

    public void playSound_1(View view) { // not sure if view is necessary for this?

        ToneGenerator mToneGenerator = new ToneGenerator(AudioManager.STREAM_DTMF, 100);
        if (mToneGenerator != null) {
            mToneGenerator.startTone(ToneGenerator.TONE_DTMF_1, 1000);
        }

        // Save button value as the last pressed...
        saveLatestSound(1);

    }
    public void playSound_2(View view) {

        ToneGenerator mToneGenerator = new ToneGenerator(AudioManager.STREAM_DTMF, 100);
        if (mToneGenerator != null) {
            mToneGenerator.startTone(ToneGenerator.TONE_DTMF_2, 1000);
        }

        saveLatestSound(2);
    }
    public void playSound_3(View view) {

        ToneGenerator mToneGenerator = new ToneGenerator(AudioManager.STREAM_DTMF, 100);
        if (mToneGenerator != null) {
            mToneGenerator.startTone(ToneGenerator.TONE_DTMF_3, 1000);
        }

        saveLatestSound(3);
    }
    public void playSound_4(View view) {

        ToneGenerator mToneGenerator = new ToneGenerator(AudioManager.STREAM_DTMF, 100);
        if (mToneGenerator != null) {
            mToneGenerator.startTone(ToneGenerator.TONE_DTMF_4, 1000);
        }

        saveLatestSound(4);
    }
    public void playSound_5(View view) {

        ToneGenerator mToneGenerator = new ToneGenerator(AudioManager.STREAM_DTMF, 100);
        if (mToneGenerator != null) {
            mToneGenerator.startTone(ToneGenerator.TONE_DTMF_5, 1000);
        }

        saveLatestSound(5);

    }
}
