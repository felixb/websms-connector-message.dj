/*
 * Copyright (C) 2012 Felix Bechstein
 * 
 * This file is part of WebSMS.
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 3 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; If not, see <http://www.gnu.org/licenses/>.
 */
package de.ub0r.android.websms.connector.messagedj;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import de.ub0r.android.websms.connector.common.ConnectorPreferenceActivity;
import de.ub0r.android.websms.connector.common.Log;
import de.ub0r.android.websms.connector.common.Utils;

/**
 * Preferences.
 * 
 * @author flx
 */
public final class Preferences extends ConnectorPreferenceActivity implements
		OnPreferenceClickListener {
	/** Tag for output. */
	private static final String TAG = ConnectorMessagedj.TAG + "/pref";

	/** Preference key: enabled. */
	static final String PREFS_ENABLED = "enable";
	/** Preference's name: user's app key. */
	static final String PREFS_PASSWORD = "password";

	/** SMS length. */
	static final String PREFS_SMSLENGTH = "sms_length";

	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.addPreferencesFromResource(R.xml.prefs);
		this.findPreference("appkey_help").setOnPreferenceClickListener(this);
		this.findPreference("support").setOnPreferenceClickListener(this);

		new AsyncTask<Void, Void, String>() {

			@Override
			protected void onPreExecute() {
				Preferences.this.updateSmsLength(null);
			};

			@Override
			protected String doInBackground(final Void... params) {
				String s = null;
				try {
					URL u = new URL(ConnectorMessagedj.URL + "?request=chars");
					HttpURLConnection con = (HttpURLConnection) u
							.openConnection();
					s = Utils.stream2str(con.getInputStream());
				} catch (IOException e) {
					Log.e(TAG, "error fetching current sms length", e);
				}
				if (TextUtils.isEmpty(s)) {
					return null;
				} else {
					return s.trim();
				}
			}

			@Override
			protected void onPostExecute(final String result) {
				Log.d(TAG, "got sms length: " + result);
				Preferences.this.updateSmsLength(result);
			}
		}.execute((Void) null);
	}

	/**
	 * Set the current SMS length in UI and preferences.
	 * 
	 * @param length
	 *            current length
	 */
	private void updateSmsLength(final String length) {
		SharedPreferences sp = PreferenceManager
				.getDefaultSharedPreferences(this);
		Preference p = this.findPreference("sms_length");
		if (p != null) {
			String s = length;
			if (s == null) {
				sp.getString(PREFS_SMSLENGTH, null);
			}
			if (s == null) {
				p.setTitle(this.getString(R.string.connector_sms_length)
						+ " ???");
			} else {
				p.setTitle(this.getString(R.string.connector_sms_length) + " "
						+ s);
			}

		}
		if (length != null) {
			sp.edit().putString(PREFS_SMSLENGTH, length).commit();
		}
	}

	@Override
	public boolean onPreferenceClick(final Preference preference) {
		if ("appkey_help".equals(preference.getKey())) {
			AlertDialog.Builder b = new AlertDialog.Builder(this);
			b.setTitle(R.string.connector_appkey_help_title);
			b.setMessage(R.string.connector_appkey_help);
			b.setCancelable(true);
			b.show();
			return true;
		}
		if ("support".equals(preference.getKey())) {
			AlertDialog.Builder b = new AlertDialog.Builder(this);
			b.setTitle(R.string.connector_support);
			b.setMessage(R.string.connector_support_hint);
			b.setCancelable(true);
			b.show();
			return true;
		}
		return false;
	}
}
