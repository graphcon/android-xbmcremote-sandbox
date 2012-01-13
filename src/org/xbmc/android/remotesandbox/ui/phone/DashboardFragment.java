/*
 *      Copyright (C) 2005-2015 Team XBMC
 *      http://xbmc.org
 *
 *  This Program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2, or (at your option)
 *  any later version.
 *
 *  This Program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with XBMC Remote; see the file license.  If not, write to
 *  the Free Software Foundation, 675 Mass Ave, Cambridge, MA 02139, USA.
 *  http://www.gnu.org/copyleft/gpl.html
 *
 */

package org.xbmc.android.remotesandbox.ui.phone;

import java.util.ArrayList;

import org.json.JSONObject;
import org.xbmc.android.jsonrpc.NotificationManager;
import org.xbmc.android.jsonrpc.NotificationManager.NotificationObserver;
import org.xbmc.android.jsonrpc.notification.AbstractEvent;
import org.xbmc.android.remotesandbox.R;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * The phone dashboard only contains the already-known menu which contains
 * various links to sections and other helpful stuff.
 * 
 * @author freezy <freezy@xbmc.org>
 */
public class DashboardFragment extends Fragment {
	
	private final static String TAG = DashboardFragment.class.getSimpleName();
	
	private static final int HOME_ACTION_REMOTE = 0;
	private static final int HOME_ACTION_MUSIC = 1;
	private static final int HOME_ACTION_VIDEOS = 2;
	
	private NotificationObserver mPlayerObserver = null;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		final View root = inflater.inflate(R.layout.fragment_dashboard, null);
		final GridView grid = (GridView)root.findViewById(R.id.dashboard_grid);
		setupChronometer();
		setupDashboardItems(grid);
		return root;
	}
	
	/**
	 * Adds all our menu items to the grid.
	 * 
	 * @param menuGrid
	 */
	private void setupDashboardItems(GridView menuGrid) {
		
		final ArrayList<HomeItem> homeItems = new ArrayList<HomeItem>();
		final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
		
		if (prefs.getBoolean("setting_show_home_music", true)) {
			homeItems.add(new HomeItem(HOME_ACTION_MUSIC, R.drawable.home_ic_music, "Music", "Listen to"));
		}
		
		menuGrid.setAdapter(new HomeAdapter(getActivity(), homeItems));
		menuGrid.setOnItemClickListener(mHomeMenuOnClickListener);
		menuGrid.setSelected(true);
		menuGrid.setSelection(0);
	}
	
	private void setupChronometer() {
		final NotificationManager nm = NotificationManager.getInstance(getActivity().getApplicationContext());
		mPlayerObserver = new NotificationObserver() {
			@Override
			public void handleNotification(JSONObject data) {
				Log.i(TAG, "Received notification: " + data.toString());
				AbstractEvent e = nm.parse(data);
				Log.i(TAG, "Parsed event: " + e);
			}
		};
		nm.registerObserver(mPlayerObserver);
	}
	
	/**
	 * Defines what happens when the user taps on one of the dashboard items.
	 */
	private final OnItemClickListener mHomeMenuOnClickListener = new OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> listView, View v, int position, long ID) {
			HomeItem item = (HomeItem) listView.getAdapter().getItem(position);
			final Intent intent = new Intent();
			switch (item.ID) {
				case HOME_ACTION_REMOTE:
				case HOME_ACTION_MUSIC:
				case HOME_ACTION_VIDEOS:
					final NotificationManager nm = NotificationManager.getInstance(getActivity().getApplicationContext());
					nm.unregisterObserver(mPlayerObserver);
					//intent.setClass(v.getContext(), MusicPagerActivity.class);
					break;
				default:
					return;
			}
			//startActivity(intent);
		}
	};

	/**
	 * The list adapter for the list containing the dashboard items.
	 * 
	 * @author freezy <freezy@xbmc.org>
	 */
	private class HomeAdapter extends ArrayAdapter<HomeItem> {
		private Activity mActivity;
		HomeAdapter(Activity activity, ArrayList<HomeItem> items) {
			super(activity, R.layout.list_item_dashboard, items);
			mActivity = activity;
		}
		public View getView(int position, View convertView, ViewGroup parent) {
			View row;
			
			if (convertView == null) {
				LayoutInflater inflater = mActivity.getLayoutInflater();
				row = inflater.inflate(R.layout.list_item_dashboard, null);
			} else {
				row = convertView;
			}
			
			HomeItem item = this.getItem(position);
			final TextView supertitle = (TextView)row.findViewById(R.id.dashboard_supertitle);
			final TextView title = (TextView)row.findViewById(R.id.dashboard_title);
			final ImageView icon = (ImageView)row.findViewById(R.id.dashboard_icon);

			title.setText(item.title);
			supertitle.setText(item.subtitle);
			icon.setImageResource(item.icon);
			
			return row;
		}
	}
	
	/**
	 * An item of the dashboard.
	 * 
	 * @author freezy <freezy@xbmc.org>
	 */
	private class HomeItem {
		public final int ID, icon;
		public final String title, subtitle;
		
		public HomeItem(int ID, int icon, String title, String subtitle) {
			this.ID = ID;
			this.icon = icon;
			this.title = title;
			this.subtitle = subtitle;
		}
	}

}
