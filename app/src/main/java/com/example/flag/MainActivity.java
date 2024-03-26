package com.example.flag;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;


import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import java.util.Set;

public class MainActivity extends AppCompatActivity {
    public static  final String CHOICES = "pref_numberOfChoices";
    public static  final String REGIONS = "pref_regionsToInclude";
    private boolean phoneDivace = true;
    private boolean preferecesChanged = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(preferecesChangedListener);

        int screenSize = getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK;

        if(screenSize == Configuration.SCREENLAYOUT_SIZE_LARGE || screenSize == Configuration.SCREENLAYOUT_SIZE_XLARGE)
            phoneDivace = false;
        if(phoneDivace)
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
    }

    @Override
    protected void onStart() {
        super.onStart();

        if(preferecesChanged){
            MainActivityFragment quizFragment = (MainActivityFragment) getSupportFragmentManager().findFragmentById(R.id.quizFragment);
            assert quizFragment != null;
            quizFragment.updateGuessRows(PreferenceManager.getDefaultSharedPreferences(this));
            quizFragment.updateRegions(PreferenceManager.getDefaultSharedPreferences(this));
            quizFragment.resetQuiz();
            preferecesChanged = false;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        int orientation = getResources().getConfiguration().orientation;
        if(orientation == Configuration.ORIENTATION_PORTRAIT){
            getMenuInflater().inflate(R.menu.main_menu, menu);
            return true;
        }
        else return false;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        Intent preferencesIntent = new Intent(this, SettingsActivity.class);
        startActivity(preferencesIntent);
        return super.onOptionsItemSelected(item);
    }

    private final OnSharedPreferenceChangeListener preferecesChangedListener = (sharedPreferences, key) -> {
        preferecesChanged = true;

        MainActivityFragment quizFragment = (MainActivityFragment) getSupportFragmentManager().findFragmentById(R.id.quizFragment);
        assert key != null;
        if(key.equals(CHOICES)){
            assert quizFragment != null;
            quizFragment.updateGuessRows(sharedPreferences);
            quizFragment.resetQuiz();
        }
        else if(key.equals(REGIONS)) {
            Set<String> regions = sharedPreferences.getStringSet(REGIONS, null);

            if (regions != null && regions.size() > 0) {
                assert quizFragment != null;
                quizFragment.updateRegions(sharedPreferences);
                quizFragment.resetQuiz();
            } else {
                SharedPreferences.Editor editor = sharedPreferences.edit();
                assert regions != null;
                regions.add(getString(R.string.default_region));
                editor.putStringSet(REGIONS, regions);
                editor.apply();

                Toast.makeText(MainActivity.this, R.string.default_region_message, Toast.LENGTH_SHORT).show();
            }
            Toast.makeText(MainActivity.this, R.string.restarting_quiz, Toast.LENGTH_SHORT).show();
        }
    };
}