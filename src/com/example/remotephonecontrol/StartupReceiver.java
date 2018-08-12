package com.example.remotephonecontrol;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class StartupReceiver extends BroadcastReceiver {

	static final String ACTION = "android.intent.action.BOOT_COMPLETED";

	@Override
	public void onReceive(Context context, Intent arg) {
		if (arg.getAction().equals(ACTION)) {
			Intent intent = new Intent(context, RemoteControlService.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			context.startService(intent);
		}

	}
}
