package com.listensutra;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.CommonModule;
import com.listensutra.MainActivity.ButListener;
import com.listensutra.MainActivity.ItemClickListener;

import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.res.Configuration;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Toast;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

public class Mp3Activity extends Activity {
	public Handler m_HandlerTimer;
	public Context m_context;
	public ListView m_lv;
	public Button m_btnclose;
	public int m_intStep=0;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_mp3list);
		
		m_context=(Context)this;
		m_lv=(ListView)this.findViewById(R.id.LV_mp3);
		m_btnclose=(Button)this.findViewById(R.id.btnclose);
		m_btnclose.setOnClickListener(
	    		new View.OnClickListener() 
	    		{
	    			@Override
	    			public void onClick(View v)
	    			{
	    				finish();
	    			}
	    		}
	    );
		m_lv.setOnItemClickListener(new ItemClickListener());
		//Timer變數初始化_start
		m_HandlerTimer= new Handler();//建立Timer物件
		//m_intStep=0;//狀態控制變數初始化
		m_HandlerTimer.postDelayed(TimerRun, 100);//啟動Timer
		//Timer變數初始化_end
		
	}
	public boolean onKeyDown(int keyCode, KeyEvent event)
	{
		switch(keyCode)
		{
			case KeyEvent.KEYCODE_BACK://返回
				this.finish();//按下返回鍵，結束目前Activity
				return true;//不作任何動作
		}
		return super.onKeyDown(keyCode, event);
	}
	private final Runnable TimerRun = new Runnable()
	{
		public void run()
		{
			switch(m_intStep)
			{
			case 0://顯示動畫和準備下載JSON
				ShowWaitAnimation();//顯示等待動畫
				DownLoadFile("ListenSutra","http://jash-idea.myweb.hinet.net/listensutra/list.json","list.json");
				m_intStep=1;
				m_HandlerTimer.postDelayed(TimerRun, 100);//啟動Timer
				break;
			case 1:
				if(m_blnDownLoadFinish==true)//如果下載完成
				{
					readJSON("ListenSutra","list.json");
					parseJSON();
					
					ArrayAdapter<String> ad = new ArrayAdapter<String>(m_context,R.layout.mytextview, m_ListDownLoadFileName);//自訂listview字型大小，透過layout
					m_lv.setAdapter(ad);
					
					HideWaitAnimation();//停止等待動畫
					m_intStep=10;//不存在的case
				}
				else//如果未下載完成則繼續維持再狀態1等待
				{
					m_HandlerTimer.postDelayed(TimerRun, 100);//啟動Timer
				}
				break;
			case 2:
				if(m_blnDownLoadFinish==true)//如果下載完成
				{
					HideWaitAnimation();//停止等待動畫
					m_intStep=10;//不存在的case
				}
				else//如果未下載完成則繼續維持再狀態1等待
				{
					m_HandlerTimer.postDelayed(TimerRun, 100);//啟動Timer
				}
				break;				
			}
		}
	};
	//////////////////////////////////
	//等待時動畫_start
	public ProgressDialog m_progressDialog=null;
	//public static int progress_dialog=0x7f070002;
	public Thread m_ThreadAnimation;
	public void ShowWaitAnimation(){
		m_ThreadAnimation=new Thread
		( 
				new Runnable()
				{
					public void run()
					{
						android.os.Process.setThreadPriority(0);					
						Looper.prepare();
						//jash.liao modify codes at 2015/03/09 
						///*
						m_progressDialog = ProgressDialog.show(m_context, "", "",false, false);
						m_progressDialog.setContentView(R.layout.progress_loader);
						Window window = m_progressDialog.getWindow();//為了置中
						window.setLayout(95, LayoutParams.WRAP_CONTENT);//為了置中
						//*/						
						//jash.liao modify codes at 2015/03/09 
						Looper.loop();
					}
				}
		);
		m_ThreadAnimation.start();
	}
	public void HideWaitAnimation(){
		m_progressDialog.dismiss();
		m_progressDialog=null;
		m_ThreadAnimation.interrupt();
		m_ThreadAnimation=null;
	
	}	
	//等待時動畫_end
	///////////////////////////////////
	//Http DownLoad_start
	public boolean m_blnDownLoadFinish=false;
	public String m_StrDownLoadFileName;
	public String m_StrDownLoadUrlPath;
	public String m_StrFloderPath;
	Thread m_threadDownload;
	public void DownLoadFile(String StrDir,String StrUrl,String StrFileName)
	{
		m_blnDownLoadFinish=false;
		File sd=Environment.getExternalStorageDirectory(); 
		m_StrFloderPath=sd.getPath()+"/"+StrDir+"/";
		m_StrDownLoadFileName=m_StrFloderPath+StrFileName;
		m_StrDownLoadUrlPath=StrUrl;
    	//Toast toast = Toast.makeText(m_context, m_StrFloderPath+"\n"+StrUrl+"\n"+StrFileName, Toast.LENGTH_SHORT);  
        //toast.show();
		m_threadDownload =new Thread(mDownload);
		m_threadDownload.start();			
	}
	public Runnable mDownload = new Runnable() 
	{
		@Override
		public void run()
		{			
			int BUFFER = 1024;
			long lngFileSize;
			long lngDownloadSize;
			lngFileSize=0;
			lngDownloadSize=0;
			DefaultHttpClient httpClient = new DefaultHttpClient();
			HttpGet httpGet = new HttpGet(m_StrDownLoadUrlPath);
			try 
			{
				HttpResponse response = httpClient.execute(httpGet);
				InputStream in = response.getEntity().getContent();
				lngFileSize=response.getEntity().getContentLength();
				FileOutputStream out;
			
				out = new FileOutputStream(new File(m_StrDownLoadFileName));

				byte[] b = new byte[BUFFER];
				int len = 0;
				lngDownloadSize=0;
				while((len=in.read(b))!= -1)
				{
					lngDownloadSize+=len;
					out.write(b,0,len);
				}
				in.close();
				out.close();
			}
			catch(ClientProtocolException e)
			{
				e.printStackTrace();
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
			finally
			{
				//httpGet.releaseConnection();
				httpClient.getConnectionManager().shutdown();
			}
			m_blnDownLoadFinish=true;
			//m_threadDownload.interrupt();
			//m_threadDownload=null;
		}
	};
	//Http DownLoad_end
	///////////////////////////////////
	//json_start
	//Read JSON_start
	public String m_StrJSONData;
	public void readJSON(String StrDir,String StrFileName)
	{
		File sd=Environment.getExternalStorageDirectory(); 
		String StrJSONFilePath=sd.getPath()+"/"+StrDir+"/"+StrFileName;
		//m_MA.m_TextView1.append(StrJSONFilePath+"\n");		
		try
		{
			
			BufferedReader in = new BufferedReader(new FileReader(StrJSONFilePath));
			String s;
			StringBuilder sb = new StringBuilder();
			
			while ((s = in.readLine()) != null)
			{
				sb.append(s);//sb.append(s + "\n");
			}
			in.close();
			m_StrJSONData=sb.toString();
			//m_MA.m_TextView1.append(m_StrJSONData+"\n");	
			//m_StrJSONData=m_StrJSONData.substring(8, (m_StrJSONData.length()-1));
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}		
	}
	//Read JSON_end
	//json_sop_function_Start
	public static String JsonStringtoData(String StrData)
	{
		String StrReturn="NULL";
		String StrBuf="";
		try {
			JSONObject jsonObject = new JSONObject(StrData);
			StrReturn= "id:"+jsonObject.getString("id")+"\n";
			StrReturn+= "name:"+jsonObject.getString("name")+"\n";
			
			StrBuf=jsonObject.getString("data");
			JSONArray jsonArray = new JSONArray(StrBuf);
			for(int i=0;i<jsonArray.length();i++)
			{
				JSONObject jsonObject1 = jsonArray.getJSONObject(i);
				StrReturn+= "\tscore:"+jsonObject1.getString("score")+"\n";
				StrReturn+= "\tid:"+jsonObject1.getString("id")+"\n";
				StrReturn+= "\tname:"+jsonObject1.getString("name")+"\n";
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return StrReturn;
	}
	public static String DatatoJsonString()
	{
		/*
		 結果
		 {"id":10,"name":"math","data":[{"score":4.8,"id":1,"name":"Android"},{"score":3.2,"id":2,"name":"Ben"}]}
		 */
		String StrReturn="NULL";
		JSONObject jsonReturnObj = new JSONObject();
		JSONObject jsonObject1 = new JSONObject();
		JSONObject jsonObject2 = new JSONObject();
		JSONArray jsonArray = new JSONArray();
        try {
        	jsonReturnObj.put("name", "math");
        	jsonReturnObj.put("id", "10");
        	jsonObject1.put("score", "4.8");
        	jsonObject1.put("id", "1");
        	jsonObject1.put("name", "Android");
        	jsonArray.put(jsonObject1);
        	jsonObject2.put("score", "3.2");
        	jsonObject2.put("id", "2");
        	jsonObject2.put("name", "Ben");
        	jsonArray.put(jsonObject2);
        	jsonReturnObj.put("data", jsonArray);
        	StrReturn=jsonReturnObj.toString();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
		return StrReturn;
		
	}
	//json_sop_function_End	
	//parse JSON_start
	public ArrayList<String> m_ListDownLoadFileName = new ArrayList<String>();
	public ArrayList<String> m_ListDownLoadUrlPath = new ArrayList<String>();
	public void parseJSON()
	{
		m_ListDownLoadFileName.clear();
		m_ListDownLoadUrlPath.clear();
		try
		{
			JSONArray jsonArray = new JSONArray(m_StrJSONData);
			for(int i=0;i<jsonArray.length();i++)
			{
				JSONObject jsonObject1 = jsonArray.getJSONObject(i);
				m_ListDownLoadFileName.add(jsonObject1.getString("name"));
				m_ListDownLoadUrlPath.add(jsonObject1.getString("url"));
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	//parse JSON_end
	//json_end
	///////////////////////////////////
	class ItemClickListener implements OnItemClickListener//ListView是件反映類別
	{
		public void onItemClick(AdapterView<?>v, View iv, int pos, long id)
		{
			
			ShowWaitAnimation();//顯示等待動畫
			m_threadDownload=null;
			m_blnDownLoadFinish=false;
			m_intStep=2;
			
			DownLoadFile("ListenSutra",m_ListDownLoadUrlPath.get(pos),m_ListDownLoadFileName.get(pos));  
			
			m_HandlerTimer.postDelayed(TimerRun, 100);//啟動Timer
		}
	}
}
