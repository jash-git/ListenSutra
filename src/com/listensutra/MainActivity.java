package com.listensutra;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

import com.CommonModule;

import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.net.Uri;
import android.os.Bundle;
import android.app.ActionBar.LayoutParams;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

public class MainActivity extends Activity {
	public CommonModule m_CM;
	public ListView m_LVFiles;
	public TextView m_TVInfo;
	public TextView m_TVTitle;
	public TextView m_TVInputTitle;
	public Button m_BttStart;
	public Button m_BttStop;
	public Button m_BttForward;
	public Button m_BttDecrease;
	public Button m_BtnChangePath;
	public MediaPlayer m_MediaPlayer;
	public CheckBox m_CBLoop;
	public CheckBox m_CBSleep;
	public boolean m_blnLANDSCAPE;
	public boolean m_blnCheckLoop=false; 
	public String m_StrMP3Name;
	public Context m_context;
	public String m_StrPresetPath="";
	public String m_StrNowPath="";
	public String m_StrSearch="";
	public ArrayList<String> m_ListAutoPlayList = new ArrayList<String>();
	public int m_ContinuousPlayCount=0;
	
	public AudioManager audioManager;// 音量管理器
	public int maxVolume;// 最大音量
	public int currentVolume;// 当前音量
	public int m_intSleepCount;
	public int m_intSleepmin=10;
	public boolean m_blnOpenSleep;
	public SeekBar m_Volumesetting_seekBar;
	public PopupWindow m_popupWindow=null;;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		m_StrMP3Name="";
		m_blnLANDSCAPE=false;
		m_LVFiles=(ListView)this.findViewById(R.id.lv_Files);
		m_TVInfo=(TextView)this.findViewById(R.id.tv_Info);
		m_TVTitle=(TextView)this.findViewById(R.id.tv_Title);
		m_BttStart=(Button)this.findViewById(R.id.btt_Start);
		m_BttStop=(Button)this.findViewById(R.id.btt_Stop);
		m_BttForward=(Button)this.findViewById(R.id.btt_Forward);
		m_BttDecrease=(Button)this.findViewById(R.id.btt_Decrease);	
		m_BtnChangePath=(Button)this.findViewById(R.id.btn_changePath);	
		m_CBLoop=(CheckBox)this.findViewById(R.id.cb_Loop);
		m_CBSleep=(CheckBox)this.findViewById(R.id.cb_Sleep);
		
		int tid=android.os.Process.myTid();
		m_CM=new CommonModule(this,(Context)this,tid);
		m_context=(Context)this;
		m_CM.RawFile2SD(R.raw.m01,"ListenSutra","南無阿彌陀佛.mp3");
		m_CM.RawFile2SD(R.raw.m02,"ListenSutra","南無觀世音菩薩.mp3");
		m_LVFiles.setOnItemClickListener(new ItemClickListener());
		m_BttStart.setEnabled(false);
		m_BttStop.setEnabled(false);
		m_BttForward.setEnabled(false);
		m_BttDecrease.setEnabled(false);
		m_CBLoop.setEnabled(false);
		m_CBSleep.setEnabled(false);
		m_CBLoop.setOnCheckedChangeListener(new CheckedChangeListener());
		m_CBSleep.setOnCheckedChangeListener(new CheckedChangeListener());
		m_blnOpenSleep=false;
		m_BttStart.setOnClickListener(new ButListener());
		m_BttStop.setOnClickListener(new ButListener());
		m_BttForward.setOnClickListener(new ButListener());
		m_BttDecrease.setOnClickListener(new ButListener());
		m_BtnChangePath.setOnClickListener(new ButListener());

		m_MediaPlayer= new MediaPlayer();
		m_MediaPlayer.setOnCompletionListener(new OnCompletionListener(){
            @Override
            public void onCompletion(MediaPlayer mp) {
            	if(m_blnCheckLoop==true)//2015.08.24_01
            	{
            		mp.seekTo(0);
            		mp.start();
            	}
            	else
            	{
                	mp.reset();
                	String [] name={m_StrMP3Name};
                	String [] value={"0"};
                	m_CM.SharedPreferences_Write(name,value,1);
                	m_BttStart.setEnabled(false);
                	m_CBLoop.setEnabled(false);
                	m_CBSleep.setEnabled(false);
                	m_BttStop.setEnabled(false);
                	m_BttForward.setEnabled(false);
                	m_BttDecrease.setEnabled(false);
                	
                	if(!m_StrPresetPath.equals(m_StrNowPath))
                	{
                		ContinuousPlay();
                	}
            	}
            }
         });
		
		audioManager = (AudioManager) getSystemService(this.AUDIO_SERVICE);//音量管理器
	    maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);// 获得最大音量
	    currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);// 获得当前音量
	    m_Volumesetting_seekBar=(SeekBar)this.findViewById(R.id.seekBar1);
	    m_Volumesetting_seekBar.setMax(maxVolume);
	    m_Volumesetting_seekBar.setProgress(currentVolume);
	    m_Volumesetting_seekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener()
	    {
	        @Override
	        public void onStopTrackingTouch(SeekBar seekBar)
	        {
	                                     
	        }
	                                 
	        @Override
	        public void onStartTrackingTouch(SeekBar seekBar)
	        {
	                                     
	        }
	                                 
	        @Override
	        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser)
	        {
	            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, progress, AudioManager.FLAG_ALLOW_RINGER_MODES);
	        }
	    });
	}
	@Override
	protected void onRestart() {//從Mp3Activity回來要更新ListView
	    super.onRestart();  // Always call the superclass method first
		ArrayAdapter<String> ad = new ArrayAdapter<String>(this,R.layout.mytextview, m_CM.GetFileList("ListenSutra",".mp3"));//自訂listview字型大小，透過layout
		m_LVFiles.setAdapter(ad);	    
	    // Activity being restarted from stopped state    
	}	
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		//螢幕旋轉不執行onCreate
		/*
		//對應的設定
        <activity
            android:name="com.listensutra.MainActivity"
            android:label="@string/app_name"
            android:configChanges="orientation|keyboardHidden|screenSize">
		 */
	    // TODO Auto-generated method stub
	    super.onConfigurationChanged(newConfig);
	    if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
	    	m_blnLANDSCAPE=true;
	    }
	    else {
	    	m_blnLANDSCAPE=false;
	    }
	}
	public boolean onKeyDown(int keyCode, KeyEvent event)
	{
		switch(keyCode)
		{
			case KeyEvent.KEYCODE_BACK://返回
				m_CM.CloseAlertDialog();//出現詢問是否關閉程式視窗 2015/03/17 finish();//關閉自己
				return true;//不作任何動作
		}
		return super.onKeyDown(keyCode, event);
	}
    protected void onDestroy(){ 
		//真正作用區
		//當呼叫刪除自己之後，CM內的Timer才會被刪除 by jash at 2014/09/04
    	m_MediaPlayer.release();
        super.onDestroy();
        //Kill myself
        android.os.Process.killProcess(android.os.Process.myPid());
    }
    @Override
    protected void onStop()
    {
    	// TODO Auto-generated method stub
    	//背景執行前最後出發的事件
    	super.onStop();
    	m_CM.cancelNotification();
    	m_CM.ShowNotification("聽  佛","顯示畫面");
    }    
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {//Menu選單載入
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{//Menu選單反應事件
		int item_id = item.getItemId();
		switch (item_id)
		{
			case R.id.ShowVersion:
				m_CM.ShowMessageDialog("版本資訊：","程式系統名稱"+m_CM.m_StrPackageName+"\n"+"版本號碼："+m_CM.m_StrVersionName,"關閉");
				break;
			case R.id.setSleepTime:
				// get prompts.xml view
				LayoutInflater li = LayoutInflater.from(this);
				View promptsView = li.inflate(R.layout.prompts, null);
				
				m_TVInputTitle=(TextView)promptsView.findViewById(R.id.textView1);
				m_TVInputTitle.setText("輸入睡眠時間（分鐘）: ");				
				
				
				AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
						this);

				// set prompts.xml to alertdialog builder
				alertDialogBuilder.setView(promptsView);

				final EditText userInput = (EditText) promptsView
						.findViewById(R.id.editTextDialogUserInput);

				// set dialog message
				alertDialogBuilder
						.setCancelable(false)
						.setPositiveButton("OK",
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int id) {
										// get user input and set it to result
										// edit text
										try
										{
											m_intSleepmin=Integer.parseInt(userInput.getText().toString());
											if(m_intSleepmin<=0)
											{
												m_intSleepmin=10;
											}
										}
										catch (NumberFormatException e) 
										{
											m_intSleepmin=10;
										 }
									}
								})
						.setNegativeButton("Cancel",
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int id) {
										dialog.cancel();
									}
								});

				// create alert dialog
				AlertDialog alertDialog = alertDialogBuilder.create();

				// show it
				alertDialog.show();				
				break;
			case R.id.DownLoadMP3:
				/*//2015.08.24_01
				String resault=m_CM.pingHost("8.8.8.8");
				if(resault.equals("success"))
				*/
				if(m_CM.isInternetOn()==true)//2015.08.24_01
				{
					Intent intent = new Intent();
					intent.setClass(this, Mp3Activity.class);
					startActivity(intent);
				}
				else
				{
					m_CM.ShowMessageDialog("偵測無網路","目前偵測無網路，請確認行動上網或wifi是否正常。","關閉");
				}
				break;
			case R.id.Search://檔案搜尋選單
				// get prompts.xml view
				LayoutInflater li_s = LayoutInflater.from(this);
				View promptsView_s = li_s.inflate(R.layout.prompts, null);
				
				m_TVInputTitle=(TextView)promptsView_s.findViewById(R.id.textView1);
				m_TVInputTitle.setText("輸入搜尋檔名 : ");				
				
				
				AlertDialog.Builder alertDialogBuilder_s = new AlertDialog.Builder(
						this);

				// set prompts.xml to alertdialog builder
				alertDialogBuilder_s.setView(promptsView_s);

				final EditText userInput_s = (EditText) promptsView_s
						.findViewById(R.id.editTextDialogUserInput);

				// set dialog message
				alertDialogBuilder_s
						.setCancelable(false)
						.setPositiveButton("OK",
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int id) {
										// get user input and set it to result
										// edit text
										m_StrSearch = userInput_s.getText().toString();
										//m_CM.ShowMessageDialog("搜尋資訊：",m_StrSearch,"關閉");
										for(int pos=0;pos<m_ListAutoPlayList.size();pos++)
										{
											m_StrMP3Name=m_ListAutoPlayList.get(pos);
											if(m_StrMP3Name.contains(m_StrSearch))
											{
												m_ContinuousPlayCount=pos;
												
												//----TextView tmp = (TextView) iv;
												m_CM.ShowMessage(m_CM.GetSDFolderPath(m_StrNowPath)+m_StrMP3Name);
												String Strdata;
												Strdata=m_CM.GetSDFolderPath(m_StrNowPath)+m_StrMP3Name;
												//----m_StrMP3Name=(String)tmp.getText();
												//Strdata.toLowerCase();//全部轉小寫
												if(Strdata.contains(".mp3"));
												{
													try 
													{
														FileInputStream fileStream;
														fileStream = new FileInputStream(Strdata);
														m_MediaPlayer.reset();
														m_MediaPlayer.setDataSource(fileStream.getFD());
														m_MediaPlayer.prepare();
														m_TVInfo.setText("準備播放："+m_StrMP3Name);
														String [] name={m_StrMP3Name};
														String [] value=m_CM.SharedPreferences_Read(name,1);
														if(value[0]=="null_data")
														{
															value[0]="0";
														}
														else
														{
															m_MediaPlayer.seekTo(Integer.valueOf(value[0])*1000);
														}
														m_TVInfo.append("\n\t\t曲目長度："+(m_MediaPlayer.getDuration()/1000)+"秒");
														if(m_blnLANDSCAPE)
														{
															m_TVInfo.append("\t\t播放第    ："+(m_MediaPlayer.getCurrentPosition()/1000)+"秒");
														}
														else
														{
															m_TVInfo.append("\n\t\t播放第    ："+(m_MediaPlayer.getCurrentPosition()/1000)+"秒");
														}
														m_BttStart.setEnabled(true);
														m_CBLoop.setEnabled(true);
														m_CBSleep.setEnabled(true);
														m_BttStop.setEnabled(false);
														m_BttForward.setEnabled(true);
														m_BttDecrease.setEnabled(true);
													} 
													catch (FileNotFoundException e) 
													{
														// TODO Auto-generated catch block
														e.printStackTrace();
													} catch (IllegalArgumentException e) {
														// TODO Auto-generated catch block
														e.printStackTrace();
													} catch (IllegalStateException e) {
														// TODO Auto-generated catch block
														e.printStackTrace();
													} catch (IOException e) {
														// TODO Auto-generated catch block
														e.printStackTrace();
													}
												}
												break;
											}	
										}										
									}
								})
						.setNegativeButton("Cancel",
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int id) {
										dialog.cancel();
									}
								});

				// create alert dialog
				AlertDialog alertDialog_s = alertDialogBuilder_s.create();

				// show it
				alertDialog_s.show();				
				break;
		}
	    return true;
	}
	
	public void ContinuousPlay()
	{
		m_ContinuousPlayCount++;
		if(m_ListAutoPlayList.size()<=m_ContinuousPlayCount)//防止超過範圍
		{
			m_ContinuousPlayCount=0;
		}
		m_CM.ShowMessage(m_CM.GetSDFolderPath(m_StrNowPath)+m_ListAutoPlayList.get(m_ContinuousPlayCount));
		String Strdata;
		Strdata=m_CM.GetSDFolderPath(m_StrNowPath)+m_ListAutoPlayList.get(m_ContinuousPlayCount);
		if(Strdata.contains(".mp3"));
		{
			try
			{
				FileInputStream fileStream = new FileInputStream(Strdata);
				m_MediaPlayer.reset();
				m_MediaPlayer.setDataSource(fileStream.getFD());
				m_MediaPlayer.prepare();
				m_TVInfo.setText("準備播放："+m_ListAutoPlayList.get(m_ContinuousPlayCount));
				m_StrMP3Name=m_ListAutoPlayList.get(m_ContinuousPlayCount);
				String [] name={m_StrMP3Name};
				String [] value=m_CM.SharedPreferences_Read(name,1);
				if(value[0]=="null_data")
				{
					value[0]="0";
				}
				else
				{
					m_MediaPlayer.seekTo(Integer.valueOf(value[0])*1000);
				}
				m_TVInfo.append("\n\t\t曲目長度："+(m_MediaPlayer.getDuration()/1000)+"秒");
				if(m_blnLANDSCAPE)
				{
					m_TVInfo.append("\t\t播放第    ："+(m_MediaPlayer.getCurrentPosition()/1000)+"秒");
				}
				else
				{
					m_TVInfo.append("\n\t\t播放第    ："+(m_MediaPlayer.getCurrentPosition()/1000)+"秒");
				}
		        m_BttStart.setEnabled(true);
		        m_CBLoop.setEnabled(true);
		        m_CBSleep.setEnabled(true);
		        m_BttStop.setEnabled(false);
		        m_BttForward.setEnabled(true);
		        m_BttDecrease.setEnabled(true);
		        
		        m_BttStart.callOnClick();//執行播放
			}
			catch (IllegalArgumentException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			catch (SecurityException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			catch (IllegalStateException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			catch (IOException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}	
	}	
	class ButListener implements OnClickListener
	{
		public void onClick(View v)
		{
			if(v==m_BtnChangePath)
			{
				if(m_popupWindow==null)
				{
					LayoutInflater layoutInflater = LayoutInflater.from(m_context);// 
					View popupView = layoutInflater.inflate(R.layout.popupwindow, null);
					
					ListView LV_popu = (ListView) popupView.findViewById(R.id.lv_popu);
					LV_popu.setOnItemClickListener(new LVpopu());
					ArrayAdapter<String> ad = new ArrayAdapter<String>(m_context,R.layout.mytextview, m_CM.GetFolderList("ListenSutra")){

				        @Override
				        public View getView(int position, View convertView,ViewGroup parent) {
				            View view =super.getView(position, convertView, parent);

				            TextView textView=(TextView) view.findViewById(android.R.id.text1);

				            /*YOUR CHOICE OF COLOR*/
				            textView.setTextColor(Color.WHITE);

				            return view;
				        }
				    };//自訂listview字型大小，透過layout
					
					LV_popu.setAdapter(ad);	
					
					m_popupWindow = new PopupWindow(popupView,LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
					m_popupWindow.setBackgroundDrawable(new ColorDrawable(android.graphics.Color.BLUE));
					m_popupWindow.showAsDropDown(m_BtnChangePath);
					m_popupWindow.setFocusable(true);
					m_popupWindow.update();
				}
				else
				{
					m_popupWindow.dismiss();
					m_popupWindow=null;
					
					LayoutInflater layoutInflater = LayoutInflater.from(m_context);// 
					View popupView = layoutInflater.inflate(R.layout.popupwindow, null);
					
					ListView LV_popu = (ListView) popupView.findViewById(R.id.lv_popu);
					LV_popu.setOnItemClickListener(new LVpopu());
					ArrayAdapter<String> ad = new ArrayAdapter<String>(m_context,R.layout.mytextview, m_CM.GetFolderList("ListenSutra")){

				        @Override
				        public View getView(int position, View convertView,ViewGroup parent) {
				            View view =super.getView(position, convertView, parent);

				            TextView textView=(TextView) view.findViewById(android.R.id.text1);

				            /*YOUR CHOICE OF COLOR*/
				            textView.setTextColor(Color.WHITE);

				            return view;
				        }
				    };//自訂listview字型大小，透過layout
					
				    LV_popu.setAdapter(ad);
					
					m_popupWindow = new PopupWindow(popupView,LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
					m_popupWindow.setBackgroundDrawable(new ColorDrawable(android.graphics.Color.BLUE));
					m_popupWindow.showAsDropDown(m_BtnChangePath);
					m_popupWindow.setFocusable(true);
					m_popupWindow.update();					
				}

			}
			if(v==m_BttStart)
			{
				if(!m_MediaPlayer.isPlaying())
				{
					if(m_CBLoop.isChecked())//2015.08.12_01 修正明明上次勾選，反悔選下首卻會失效的問題
					{
						m_blnCheckLoop=true;//2015.08.24_01//m_MediaPlayer.setLooping(true);
					}
					if(m_CBSleep.isChecked())//2015.08.12_01 修正明明上次勾選，反悔選下首卻會失效的問題
					{
						m_intSleepCount=m_intSleepmin*60*1000/100;
						m_blnOpenSleep=true;
					}
					m_MediaPlayer.start();
					m_TVInfo.setText("開始播放："+m_StrMP3Name);
					m_TVInfo.append("\n\t\t曲目長度："+(m_MediaPlayer.getDuration()/1000)+"秒");
					if(m_blnLANDSCAPE)
					{
						m_TVInfo.append("\t\t播放第    ："+(m_MediaPlayer.getCurrentPosition()/1000)+"秒");
					}
					else
					{
						m_TVInfo.append("\n\t\t播放第    ："+(m_MediaPlayer.getCurrentPosition()/1000)+"秒");
					}
					m_CM.SetTimerStepState(1);
					m_BttStart.setEnabled(false);
					m_CBLoop.setEnabled(false);
					m_CBSleep.setEnabled(false);
	        		m_BttStop.setEnabled(true);
	        		m_BttForward.setEnabled(false);
	        		m_BttDecrease.setEnabled(false);
				}
			}
			if(v==m_BttStop)
			{
				if(m_MediaPlayer.isPlaying())
				{
					m_MediaPlayer.pause();
					m_CM.SetTimerStepState(100);
					m_TVInfo.setText("暫停播放："+m_StrMP3Name);
					m_TVInfo.append("\n\t\t曲目長度："+(m_MediaPlayer.getDuration()/1000)+"秒");
					if(m_blnLANDSCAPE)
					{
						m_TVInfo.append("\t\t播放第    ："+(m_MediaPlayer.getCurrentPosition()/1000)+"秒");
					}
					else
					{
						m_TVInfo.append("\n\t\t播放第    ："+(m_MediaPlayer.getCurrentPosition()/1000)+"秒");
					}
					String [] name={m_StrMP3Name};
					String [] value={""+(m_MediaPlayer.getCurrentPosition()/1000)};
					m_CM.SharedPreferences_Write(name,value,1);
					m_CM.SaveSharedPreferencesXML(m_CM.m_StrPackageName,"ListenSutra");
					//m_CM.SaveSharedPreferencesXML("com.SmallFactory.ProjectTwoPicture","ListenSutra");
					m_BttStart.setEnabled(true);
	        		m_CBLoop.setEnabled(true);
	        		m_CBSleep.setEnabled(true);
	        		m_BttStop.setEnabled(false);
	        		m_BttForward.setEnabled(true);
	        		m_BttDecrease.setEnabled(true);
				}				
			}
			if(v==m_BttForward)
			{
				if((m_MediaPlayer.getCurrentPosition()+10000)<m_MediaPlayer.getDuration())
				{
					m_MediaPlayer.seekTo(m_MediaPlayer.getCurrentPosition()+10000);
				}
				m_TVInfo.setText("開始播放："+m_StrMP3Name);
				m_TVInfo.append("\n\t\t曲目長度："+(m_MediaPlayer.getDuration()/1000)+"秒");
				if(m_blnLANDSCAPE)
				{
					m_TVInfo.append("\t\t播放第    ："+(m_MediaPlayer.getCurrentPosition()/1000)+"秒");
				}
				else
				{
					m_TVInfo.append("\n\t\t播放第    ："+(m_MediaPlayer.getCurrentPosition()/1000)+"秒");
				}
				String [] name={m_StrMP3Name};
				String [] value={""+(m_MediaPlayer.getCurrentPosition()/1000)};
				m_CM.SharedPreferences_Write(name,value,1);
			}
			if(v==m_BttDecrease)
			{
				if((m_MediaPlayer.getCurrentPosition()-10000)>0)
				{
					m_MediaPlayer.seekTo(m_MediaPlayer.getCurrentPosition()-10000);
				}
				else
				{
					m_MediaPlayer.seekTo(0);
				}
				m_TVInfo.setText("開始播放："+m_StrMP3Name);
				m_TVInfo.append("\n\t\t曲目長度："+(m_MediaPlayer.getDuration()/1000)+"秒");
				if(m_blnLANDSCAPE)
				{
					m_TVInfo.append("\t\t播放第    ："+(m_MediaPlayer.getCurrentPosition()/1000)+"秒");
				}
				else
				{
					m_TVInfo.append("\n\t\t播放第    ："+(m_MediaPlayer.getCurrentPosition()/1000)+"秒");
				}
				String [] name={m_StrMP3Name};
				String [] value={""+(m_MediaPlayer.getCurrentPosition()/1000)};
				m_CM.SharedPreferences_Write(name,value,1);				
			}			
		}
	}
	class CheckedChangeListener implements OnCheckedChangeListener
	{
		public void onCheckedChanged(CompoundButton cb, boolean isChecked)
		{
			if(cb==m_CBLoop)
			{
				if(isChecked == true)
				{
					m_blnCheckLoop=true;//2015.08.24_01//m_MediaPlayer.setLooping(true);
				}
				else if(isChecked == false)
				{
					m_blnCheckLoop=false;//2015.08.24_01//m_MediaPlayer.setLooping(false);
				}
			}
			if(cb==m_CBSleep)
			{
				if(isChecked == true)
				{
					m_intSleepCount=m_intSleepmin*60*1000/100;
					m_blnOpenSleep=true;
				}
				else if(isChecked == false)
				{
					m_blnOpenSleep=false;
				}				
			}
		}
	} 
	class LVpopu implements OnItemClickListener
	{
		public void onItemClick(AdapterView<?>v, View iv, int pos, long id)
		{
			TextView tmp = (TextView) iv;
			m_popupWindow.dismiss();
			m_popupWindow=null;
			if(!tmp.getText().equals("ListenSutra"))
			{
				m_ListAutoPlayList.clear();
				String []buf=m_CM.GetFileList("ListenSutra"+tmp.getText(),".mp3");
				if(buf.length>0)
				{
					for(int i=0;i<buf.length;i++)
					{
						m_ListAutoPlayList.add(buf[i]);
					}
				}
				ArrayAdapter<String> ad = new ArrayAdapter<String>(m_context,R.layout.mytextview, m_CM.GetFileList("ListenSutra"+tmp.getText(),".mp3"));//自訂listview字型大小，透過layout
				m_LVFiles.setAdapter(ad);
				m_CM.ShowMessage(m_CM.GetSDFolderPath("ListenSutra"+tmp.getText()));
				m_TVTitle.setText("檔案清單：（"+m_CM.GetSDFolderPath("ListenSutra"+tmp.getText())+"）");
				m_StrNowPath="ListenSutra"+tmp.getText();
			}
			else
			{
				ArrayAdapter<String> ad = new ArrayAdapter<String>(m_context,R.layout.mytextview, m_CM.GetFileList("ListenSutra",".mp3"));//自訂listview字型大小，透過layout
				m_LVFiles.setAdapter(ad);
				m_CM.ShowMessage(m_CM.GetSDFolderPath("ListenSutra"));
				m_TVTitle.setText("檔案清單：（"+m_CM.GetSDFolderPath("ListenSutra")+"）");
				m_StrNowPath="ListenSutra";
			}
		}
	}
	class ItemClickListener implements OnItemClickListener//ListView是件反映類別
	{
		public void onItemClick(AdapterView<?>v, View iv, int pos, long id)
		{
			m_ContinuousPlayCount=pos;
			
			TextView tmp = (TextView) iv;
			m_CM.ShowMessage(m_CM.GetSDFolderPath(m_StrNowPath)+tmp.getText());
			String Strdata;
			Strdata=m_CM.GetSDFolderPath(m_StrNowPath)+tmp.getText();
			m_StrMP3Name=(String)tmp.getText();
			//Strdata.toLowerCase();//全部轉小寫
			if(Strdata.contains(".mp3"));
			{
				try
				{
					FileInputStream fileStream = new FileInputStream(Strdata);
					m_MediaPlayer.reset();
					m_MediaPlayer.setDataSource(fileStream.getFD());
					m_MediaPlayer.prepare();
					m_TVInfo.setText("準備播放："+tmp.getText());
					String [] name={m_StrMP3Name};
					String [] value=m_CM.SharedPreferences_Read(name,1);
					if(value[0]=="null_data")
					{
						value[0]="0";
					}
					else
					{
						m_MediaPlayer.seekTo(Integer.valueOf(value[0])*1000);
					}
					m_TVInfo.append("\n\t\t曲目長度："+(m_MediaPlayer.getDuration()/1000)+"秒");
					if(m_blnLANDSCAPE)
					{
						m_TVInfo.append("\t\t播放第    ："+(m_MediaPlayer.getCurrentPosition()/1000)+"秒");
					}
					else
					{
						m_TVInfo.append("\n\t\t播放第    ："+(m_MediaPlayer.getCurrentPosition()/1000)+"秒");
					}
	        		m_BttStart.setEnabled(true);
	        		m_CBLoop.setEnabled(true);
	        		m_CBSleep.setEnabled(true);
	        		m_BttStop.setEnabled(false);
	        		m_BttForward.setEnabled(true);
	        		m_BttDecrease.setEnabled(true);
				}
				catch (IllegalArgumentException e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				catch (SecurityException e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				catch (IllegalStateException e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				catch (IOException e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}


			}
		}
	}
}
