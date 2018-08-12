package com.example.remotephonecontrol;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import android.app.Notification;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.IBinder;
import android.os.Vibrator;

public class RemoteControlService extends Service {

	private final static int ERROR = -2;
	private final static int UNKNOWN = -1;
	private final static int SET_NORMAL = 0;
	private final static int SET_SILENCE = 1;
	private final static int VIBRATE = 2;
	private final static int GET_VOLUMN_STATE = 3;

	private final static String CHECK_CMD = "checkstate";
	private final static String SILENCE_CMD = "setsilence";
	private final static String NORMAL_CMD = "setnormal";
	private final static String VIBRATE_CMD = "vibrate";

	private final static String HOST = "138.128.197.157";
	private final static int PORT = 18888;

	private Socket socket;
	private BufferedReader reader;
	private PrintWriter writer;

	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		
		new Thread(new Runnable(){
			@Override
			public void run() {
				try {
					socket = new Socket(HOST, PORT);
					reader = new BufferedReader(new InputStreamReader(
							socket.getInputStream()));
					writer = new PrintWriter(socket.getOutputStream());
				} catch (IOException e) {
					sendNotification("连接远程服务器", "连接失败");
					return;
				}
				
				sendNotification("远程控制服务", "已启动");
				
				int cmd;
				while (true) {
					cmd = getCmdFromSocket();
					switch (cmd) {
					case SET_NORMAL:
						setNormal();
						sendResultToServer(0);
						break;
					case SET_SILENCE:
						setSilence();
						sendResultToServer(0);
						break;
					case VIBRATE:
						sendResultToServer(dealVibrate());
						break;
					case GET_VOLUMN_STATE:
						sendResultToServer(getVolumeState());
						break;
					case ERROR:
						sendNotification("接收请求", "命令读取错误");
						sendResultToServer(-1);
					default:
						sendNotification("接收请求", "命令有误：" + cmd);
						sendResultToServer(-1);
					}
				}
			}
		}).start();

		return super.onStartCommand(intent, flags, startId);
	}

	private int dealVibrate() {
		try {
			String str = null;
			str = reader.readLine();

			int n = Integer.parseInt(str);
			
			str = reader.readLine();
			String[] times = str.split(",");
			
			if(times.length != n){
				return ERROR;
			}

			long[] array = new long[n];
			for(int i=0;i<n;++i){
				array[i] = Integer.parseInt(times[i]);
			}
			
			vibrate(array);
		} catch (IOException e) {
			return ERROR;
		}
		return 0;
	}

	private void sendResultToServer(int i) {
		writer.println(""+i);
		writer.flush();
	}

	// 从socket读取远程服务器发送过来的请求，并返回命令类型
	private int getCmdFromSocket() {
		String cmd = null;
		try {
			cmd = reader.readLine();
		} catch (IOException e) {
			return ERROR;
		}

		int ret = -1;
		if (cmd.equals(CHECK_CMD)) // 检测音量状态
			ret = GET_VOLUMN_STATE;
		else if (cmd.equals(SILENCE_CMD)) // 设置静音
			ret = SET_SILENCE;
		else if (cmd.equals(NORMAL_CMD)) // 恢复音量
			ret = SET_NORMAL;
		else if (cmd.equals(VIBRATE_CMD)) // 震动
			ret = VIBRATE;
		else
			// 其他无法识别的指令
			ret = UNKNOWN;
		return ret;
	}

	// 恢复手机声音
	private void setNormal() {
		AudioManager audio = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
		audio.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
	}

	// 设置手机静音
	private void setSilence() {
		AudioManager audio = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
		audio.setRingerMode(AudioManager.RINGER_MODE_SILENT);
	}

	// 根据给定的频率震动
	private void vibrate(long[] array) {
		Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
		vibrator.vibrate(array, -1);
	}

	// 获取手机音量状态，1表示有声音，0表示静音状态
	private int getVolumeState() {
		AudioManager audio = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
		return audio.getRingerMode() == AudioManager.RINGER_MODE_NORMAL ? 1 : 0;
	}

	// 弹出消息推送通知
	@SuppressWarnings("deprecation")
	private void sendNotification(String title, String content) {
		Notification notification = new Notification(R.drawable.ic_launcher,
				title, System.currentTimeMillis());
		notification.setLatestEventInfo(this, title, content, null);
		notification.defaults = Notification.DEFAULT_ALL;
		notification.flags = Notification.FLAG_SHOW_LIGHTS;
		notification.flags |= Notification.FLAG_AUTO_CANCEL;
		startForeground(1, notification);
	}

}
