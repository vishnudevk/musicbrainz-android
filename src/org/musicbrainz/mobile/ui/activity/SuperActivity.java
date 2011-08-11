/*
 * Copyright (C) 2010 Jamie McDonald
 * 
 * This file is part of MusicBrainz Mobile (Android).
 * 
 * MusicBrainz Mobile (Android) is free software: you can redistribute 
 * it and/or modify it under the terms of the GNU General Public 
 * License as published by the Free Software Foundation, either 
 * version 3 of the License, or (at your option) any later version.
 * 
 * MusicBrainz Mobile (Android) is distributed in the hope that it 
 * will be useful, but WITHOUT ANY WARRANTY; without even the implied 
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 * See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with MusicBrainz Mobile (Android). If not, see 
 * <http://www.gnu.org/licenses/>.
 */

package org.musicbrainz.mobile.ui.activity;

import org.musicbrainz.mobile.R;
import org.musicbrainz.mobile.util.Config;
import org.musicbrainz.mobile.util.Secrets;
import org.musicbrainz.mobile.util.SimpleEncrypt;
import org.musicbrainz.mobile.ws.WebServiceUser;

import com.markupartist.android.widget.ActionBar;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

/**
 * Abstract class to represent items common to multiple Activity classes. The
 * menu which is inflated depends on user login status.
 * 
 * @author Jamie McDonald - jdamcd@gmail.com
 */
public abstract class SuperActivity extends Activity {

	protected boolean loggedIn = false;
	
	/*
	 * Get login status from shared preferences file and set local status.
	 */
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		if (getUsername() != null) {
			loggedIn = true;
		}
	}
	
	protected String getUsername() {
		SharedPreferences prefs = getSharedPreferences("user", MODE_PRIVATE);
		return prefs.getString("username", null); 
	}

	/**
	 * Create a webservice user from previously authenticated
	 * credentials stored in preferences.
	 * 
	 * @return Webservice user.
	 */
	protected WebServiceUser getUser() {
		SharedPreferences prefs = getSharedPreferences("user", MODE_PRIVATE);
		String username = prefs.getString("username", null);
		String obscuredPassword = prefs.getString("password", null);
		String password = SimpleEncrypt.decrypt(new Secrets().getKey(), obscuredPassword);
		return new WebServiceUser(username, password, getClientVersion());
	}
	
	public String getClientVersion() {
		String version = "Unknown";
		try {
			 version = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		return version;
	}
	
	protected ActionBar setupActionBarWithHome() {
        ActionBar actionBar = (ActionBar) findViewById(R.id.actionbar);
        getMenuInflater().inflate(R.menu.actionbar, actionBar.asMenu());
        actionBar.findAction(R.id.actionbar_item_home).setIntent(HomeActivity.createIntent(this));
        actionBar.setDisplayShowHomeEnabled(true);
        return actionBar;
	}
    
    public boolean onCreateOptionsMenu(Menu menu) {

    	MenuInflater inflater = getMenuInflater();
    	inflater.inflate(R.menu.general, menu);
    
    	return true;
    }
    
    public boolean onOptionsItemSelected(MenuItem item){
    	super.onOptionsItemSelected(item);
    	
        switch(item.getItemId()) {
        case R.id.menu_about:
        	Intent aboutIntent = new Intent(this, AboutActivity.class);
			startActivity(aboutIntent);
        	return true;
        case R.id.menu_donate:
        	Intent donateIntent = new Intent(this, DonateActivity.class);
			startActivity(donateIntent);
        	return true;
        case R.id.menu_feedback:
        	sendFeedback();
        	return true;
        }
        return false;
    }
    
    private void sendFeedback() {
    	try {
    		startActivity(createFeedbackIntent());
    	} catch (ActivityNotFoundException e){
    		Toast.makeText(this, R.string.toast_feedback_fail, Toast.LENGTH_LONG).show();
    	}
    }
    
    private Intent createFeedbackIntent() {
    	Uri uri = Uri.parse("mailto:" + Config.FEEDBACK_EMAIL);
    	Intent feedback = new Intent(Intent.ACTION_SENDTO , uri);
    	feedback.putExtra(Intent.EXTRA_SUBJECT, "[MBAndroid] Feedback");
    	return feedback;
    }
    
}