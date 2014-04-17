package com.tarks.favorite.page;

import java.io.File;
import java.util.ArrayList;

import android.app.AlertDialog;
import android.content.ClipData;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.text.ClipboardManager;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.HeaderViewListAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

import com.actionbarsherlock.app.SherlockActivity;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.Window;
import com.tarks.favorite.MainActivity;
import com.tarks.favorite.R;
import com.tarks.favorite.connect.AsyncHttpTask;
import com.tarks.favorite.connect.ImageDownloader;
import com.tarks.favorite.fadingactionbar.extras.actionbarsherlock.FadingActionBarHelper;
import com.tarks.favorite.global.Global;
import com.tarks.favorite.global.Globalvariable;
import com.tarks.favorite.page.ProfileActivity.List;
import com.tarks.favorite.start.join;

public class document_read extends SherlockActivity {

	String local_path;
	// Profile
	ImageView profile;
	TextView profile_title;
	TextView profile_des;
	TextView comment_count;
	TextView doc_content;
	// Member srl
	String doc_srl = "0";
	String user_srl = "0";
	String comments = "0";
	int comments_count = 0;
	int previous_count = 1;
	int contextmenu_number = 0;
	int contextmenu_status = 0;
	// Edittext
	EditText comment_edittext;
	// ImageButton
	ImageButton send_button;
	// Button
	Button previous_comments;

	// ListView
	ListView listView;

	// List
	ArrayList<List> m_orders = new ArrayList<List>();
	// Define ListAdapter
	ListAdapter m_adapter;
	// ClipBoard
	CharSequence clipboard_content;
	int you_doc_status;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Can use progress
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);

		setContentView(R.layout.doclistview);
		// 액션바백버튼가져오기
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		// Get Intent
		Intent intent = getIntent();// 인텐트 받아오고
		doc_srl = intent.getStringExtra("doc_srl");
		Log.i("Doc srl", doc_srl + "");
		try {
			local_path = getCacheDir().toString() + "/member/";
		} catch (Exception e) {
		}

	loadView();
	}
	
	
	public void loadView(){
		// Set List Adapter
		listView = (ListView) findViewById(R.id.listView1);
		
		 listView.setOnItemLongClickListener( new OnItemLongClickListener(){

			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view,
					int position, long id) {
				// TODO Auto-generated method stub
				// TODO Auto-generated method stub
				if (parent.getAdapter() instanceof HeaderViewListAdapter) {
					if (((HeaderViewListAdapter) parent.getAdapter())
							.getWrappedAdapter() instanceof ListAdapter) {
						ListAdapter ca = (ListAdapter) ((HeaderViewListAdapter) parent
								.getAdapter()).getWrappedAdapter();
			
try{
						List ls = (List) ca.getItem(position -1 );

						Log.i("LongClick", "Clicked");
						clipboard_content = ls.getDes();
						contextmenu_number = ls.getTag();
						contextmenu_status = ls.getStatus();
						//parent.showContextMenu();
					
}catch (Exception e){	
}
Log.i("LongClick", "Clicked");
					}
				}
	
			
				return false;
				
			}

				
		});
			registerForContextMenu(listView);
		// Header, Footer 생성 및 등록
		View header = getLayoutInflater().inflate(R.layout.doclist_header,
				null, false);
		profile = (ImageView) header.findViewById(R.id.img);
		profile_title = (TextView) header.findViewById(R.id.title);
		profile_des = (TextView) header.findViewById(R.id.description);
		doc_content = (TextView) header.findViewById(R.id.content);
		comment_count = (TextView) header.findViewById(R.id.comment_count);

		// profile_edit.setOnClickListener(l)

		doc_content.setOnLongClickListener(new OnLongClickListener() {

			@Override
			public boolean onLongClick(View v) {
				// TODO Auto-generated method stub

				clipboard_content = doc_content.getText().toString();
                contextmenu_number = 0;
			//	registerForContextMenu(doc_content);
				v.showContextMenu();
				return true;
			}
		});
		profile_title.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				GoPage(user_srl);
			}
		});

		profile.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				GoPage(user_srl);
			}
		});

		listView.addHeaderView(header);

		// Comment
		comment_edittext = (EditText) findViewById(R.id.comment_edittext);
		send_button = (ImageButton) findViewById(R.id.send);

		send_button.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (Globalvariable.okbutton == true) {
					// Set ok button disable
					Globalvariable.okbutton = false;
					Global.ButtonEnable(1);
					String comment_value = comment_edittext.getText()
							.toString();

					if (!comment_value.matches("")) {
						CommentPostAct();
					}

				}
			}
		});
		previous_comments = (Button) findViewById(R.id.previous_comments_button);
		previous_comments.setVisibility(View.GONE);
		previous_comments.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				previous_count = previous_count + 1;
				getCommentsList(getStartComment(comments_count),
						getStartComment(comments_count) == 0 ? comments_count
								- (previous_count - 1) * 10 : 10);
				if (getStartComment(comments_count) == 0)
					previous_comments.setVisibility(View.GONE);
				listView.smoothScrollToPosition(0);
			//	Log.i("Count", getStartComment(comments_count) + "");
			}

		});
		m_adapter = new ListAdapter(this, R.layout.comment_list, m_orders);
		listView.setAdapter(m_adapter);
		getDoc();
		//
	}
	
	public void refreshAct(){
		 comments_count = 0;
		 previous_count = 1;
		 m_adapter.clear();
		 getDoc();
		 
		
	} 

	public void getDoc() {
		// Start Progressbar
		setSupportProgressBarIndeterminateVisibility(true);

		ArrayList<String> Paramname = new ArrayList<String>();
		Paramname.add("authcode");
		Paramname.add("kind");
		Paramname.add("user_srl");
		Paramname.add("user_srl_auth");
		Paramname.add("doc_srl");
		Paramname.add("doc_info");

		ArrayList<String> Paramvalue = new ArrayList<String>();
		Paramvalue.add("642979");
		Paramvalue.add("1");
		Paramvalue.add(Global.getSetting("user_srl",
				Global.getSetting("user_srl", "0")));
		Paramvalue.add(Global.getSetting("user_srl_auth",
				Global.getSetting("user_srl_auth", "null")));
		Paramvalue.add(doc_srl);
		Paramvalue
				.add("page_srl//user_srl//name//title//content//date//status//privacy//comments//recommend//negative");

		new AsyncHttpTask(this, getString(R.string.server_path)
				+ "board/documents_app_read.php", mHandler, Paramname,
				Paramvalue, null, 1, 0);
	}

	public void getCommentsList(int start_comment, int number) {
		// Start Progressbar
		setSupportProgressBarIndeterminateVisibility(true);

		ArrayList<String> Paramname = new ArrayList<String>();
		Paramname.add("authcode");
		Paramname.add("user_srl");
		Paramname.add("user_srl_auth");
		Paramname.add("doc_srl");
		Paramname.add("start_comment");
		Paramname.add("comment_number");
		Paramname.add("comment_info");

		ArrayList<String> Paramvalue = new ArrayList<String>();
		Paramvalue.add("642979");
		Paramvalue.add(Global.getSetting("user_srl",
				Global.getSetting("user_srl", "0")));
		Paramvalue.add(Global.getSetting("user_srl_auth",
				Global.getSetting("user_srl_auth", "null")));
		Paramvalue.add(doc_srl);
		Paramvalue.add(String.valueOf(start_comment));
		Paramvalue.add(String.valueOf(number));
		Paramvalue.add("srl//user_srl//name//content//date//status//privacy");

		new AsyncHttpTask(this, getString(R.string.server_path)
				+ "board/comment_app_read.php", mHandler, Paramname,
				Paramvalue, null, 5, 0);
	}

	public void setList(int moreload, int srl, String user_srl,
			String name, String contents, String date, int status) {

		// Get Profile
		// getMemberInfo(user_srl);
		List p1 = new List(user_srl, name, contents, date, srl, status);
		if (moreload == -1)
			m_orders.add(p1);
		else
			m_orders.add(moreload, p1);

		// ListView listview = (ListView) findViewById(R.id.listView1);

	}

	public void setMoreList(int doc_srl, String user_srl, String name,
			String contents, String date) {

		// Get Profile
		// getMemberInfo(user_srl);
		List p1 = new List(user_srl, name, contents, date, 0, 0);
		m_orders.add(0, p1);

		// ListView listview = (ListView) findViewById(R.id.listView1);

	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		Log.i("ContextMenu", "Contextmenu" + v.getId());

		menu.setHeaderIcon(android.R.drawable.btn_star);
		// menu.setHeaderTitle("공지사항");
		   if(contextmenu_number == 0) {
		menu.add(Menu.NONE, 1, Menu.NONE, getString(R.string.copy));
		   }
		   if(contextmenu_number != 0) {
			   menu.add(Menu.NONE, 1, Menu.NONE, getString(R.string.copy));
			if(contextmenu_status == 4){
			   menu.add(Menu.NONE, 2, Menu.NONE, getString(R.string.delete));
			}
				   }
		super.onCreateContextMenu(menu, v, menuInfo);

	}
	
	

	@Override
	public boolean onContextItemSelected(android.view.MenuItem item) {

		switch (item.getItemId()) {
		case 1:
			ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
//			ClipData clip = ClipData.newPlainText("content", clipboard_content);
//			clipboard.setPrimaryClip(clip);
			clipboard.setText(clipboard_content);
			break;

		case 2:
			//Delete
			CommentStatusUpdate(String.valueOf(contextmenu_number), "5", 7);
			break;
		default:
			break;
		}

		return super.onContextItemSelected(item);
	}

	public int getStartComment(int comment) {
		comment = comment - previous_count * 10;
		if (comment < 0)
			comment = 0;

		return comment;
	}

	public void seePreviousComments(int comments) {
		if (comments - previous_count * 10 > 0) {
			previous_comments.setVisibility(View.VISIBLE);
		}
	}

	public void setCommentsCount(int count) {
		comments_count = count;
		comment_count.setText(String.valueOf(count));
	}

	public void getMemberInfo(String user_srl) {
		if (Global.getCurrentTimeStamp()
				- Integer.parseInt(Global.getUser(user_srl, "profile_update")) > 15000 || Global.CheckFileState(local_path + user_srl +  ".jpg")  == false && Global.getUser(user_srl, "profile_pic").matches("Y")) {
			ArrayList<String> Paramname = new ArrayList<String>();
			Paramname.add("authcode");
			Paramname.add("user_srl");
			Paramname.add("user_srl_auth");
			Paramname.add("profile_user_srl");
			Paramname.add("member_info");

			ArrayList<String> Paramvalue = new ArrayList<String>();
			Paramvalue.add("642979");
			Paramvalue.add(Global.getSetting("user_srl",
					Global.getSetting("user_srl", "0")));
			Paramvalue.add(Global.getSetting("user_srl_auth",
					Global.getSetting("user_srl_auth", "null")));
			Paramvalue.add(String.valueOf(user_srl));
			Paramvalue.add("profile_pic//profile_update");

			new AsyncHttpTask(this, getString(R.string.server_path)
					+ "member/profile_info.php", mHandler, Paramname,
					Paramvalue, null, 2, Integer.parseInt(user_srl));
		
		}
	}

	public void GoPage(String user_srl) {
		Intent intent = new Intent(document_read.this, ProfileActivity.class);
		intent.putExtra("member_srl", user_srl);
		startActivity(intent);
	}

	public void ProfileUserImageDownload(String user_srl) {
		// Start Progressbar
		setSupportProgressBarIndeterminateVisibility(true);
		new ImageDownloader(this, getString(R.string.server_path)
				+ "files/profile/" + user_srl + ".jpg", mHandler, 3,
				Integer.parseInt(user_srl));
	}

	public void CommentPostAct() {
		setSupportProgressBarIndeterminateVisibility(true);
		comment_edittext.setEnabled(false);
		
		ArrayList<String> Paramname = new ArrayList<String>();
		Paramname.add("authcode");
		Paramname.add("kind");
		Paramname.add("doc_srl");
		Paramname.add("user_srl");
		Paramname.add("user_srl_auth");
		Paramname.add("content");
		Paramname.add("permission");
		Paramname.add("privacy");

		ArrayList<String> Paramvalue = new ArrayList<String>();
		Paramvalue.add("642979");
		Paramvalue.add("1");
		Paramvalue.add(doc_srl);
		Paramvalue.add(Global.getSetting("user_srl",
				Global.getSetting("user_srl", "0")));
		Paramvalue.add(Global.getSetting("user_srl_auth",
				Global.getSetting("user_srl_auth", "null")));
		Paramvalue.add(Global.setValue(comment_edittext.getText().toString()));
		Paramvalue.add("3");
		Paramvalue.add("0");

		new AsyncHttpTask(this, getString(R.string.server_path)
				+ "board/comment_app_write.php", mHandler, Paramname,
				Paramvalue, null, 4, 0);
	}
	
	public void StatusUpdate(String status, int handler) {
		// IF Sucessfull no timeout
					setSupportProgressBarIndeterminateVisibility(true);
		ArrayList<String> Paramname = new ArrayList<String>();
		Paramname.add("authcode");
		Paramname.add("kind");
		Paramname.add("doc_srl");
		Paramname.add("user_srl");
		Paramname.add("user_srl_auth");
		Paramname.add("status");

		ArrayList<String> Paramvalue = new ArrayList<String>();
		Paramvalue.add("642979");
		Paramvalue.add("0");
		Paramvalue.add(doc_srl);
		Paramvalue.add(Global.getSetting("user_srl",
				Global.getSetting("user_srl", "0")));
		Paramvalue.add(Global.getSetting("user_srl_auth",
				Global.getSetting("user_srl_auth", "null")));
		Paramvalue.add(status);
	

		new AsyncHttpTask(this, getString(R.string.server_path)
				+ "board/documents_app_write.php", mHandler, Paramname, Paramvalue,
				null, handler,0);
	}
	
	public void CommentStatusUpdate(String srl, String status, int handler) {
		// IF Sucessfull no timeout
					setSupportProgressBarIndeterminateVisibility(true);
		ArrayList<String> Paramname = new ArrayList<String>();
		Paramname.add("authcode");
		Paramname.add("kind");
		Paramname.add("comment_srl");
		Paramname.add("user_srl");
		Paramname.add("user_srl_auth");
		Paramname.add("status");

		ArrayList<String> Paramvalue = new ArrayList<String>();
		Paramvalue.add("642979");
		Paramvalue.add("0");
		Paramvalue.add(srl);
		Paramvalue.add(Global.getSetting("user_srl",
				Global.getSetting("user_srl", "0")));
		Paramvalue.add(Global.getSetting("user_srl_auth",
				Global.getSetting("user_srl_auth", "null")));
		Paramvalue.add(status);
	

		new AsyncHttpTask(this, getString(R.string.server_path)
				+ "board/comment_app_write.php", mHandler, Paramname, Paramvalue,
				null, handler,0);
	}
	
	public void DeleteAlert() {
		// Alert
		AlertDialog.Builder builder = new AlertDialog.Builder(document_read.this);
		builder.setMessage(getString(R.string.delete_des)).setTitle(
				getString(R.string.delete));
		builder.setPositiveButton(getString(R.string.yes),
				new DialogInterface.OnClickListener() {

					public void onClick(DialogInterface dialog, int which) {
						if (Globalvariable.okbutton == true) {
							// Set ok button disable
							StatusUpdate("5" ,6);
						}
					}
				});
		builder.setNegativeButton(getString(R.string.no),null);
		builder.show();

	}

	private class ListAdapter extends ArrayAdapter<List> {

		private ArrayList<List> items;

		public ListAdapter(Context context, int textViewResourceId,
				ArrayList<List> items) {
			super(context, textViewResourceId, items);
			this.items = items;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View v = convertView;
			if (v == null) {
				LayoutInflater vi = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				v = vi.inflate(R.layout.comment_list, null);
			}
			final List p = items.get(position);
			if (p != null) {
				TextView tt = (TextView) v.findViewById(R.id.title);
				TextView bt = (TextView) v.findViewById(R.id.description);
				TextView date = (TextView) v.findViewById(R.id.date);
				ImageView image = (ImageView) v.findViewById(R.id.img);
				if (tt != null) {
					tt.setText(p.getTitle());
				}
				if (bt != null) {
					bt.setText(Global.getValue(p.getDes()));
				}
				if (date != null) {
					date.setText(p.getDate());
				}
				if (image != null) {

					boolean state = Global.CheckFileState(local_path
							+ "thumbnail/" + p.getUserSrl() + ".jpg");
					
					if(state){
					image.setImageDrawable(Drawable.createFromPath(local_path
							+ "thumbnail/" + p.getUserSrl() + ".jpg"));
					}else{
						image.setImageResource(R.drawable.person);
					}
					image.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View v) {
							Intent intent = new Intent(document_read.this,
									ProfileActivity.class);
							intent.putExtra("member_srl", p.getUserSrl());
							startActivity(intent);
						}
					});
				}
			}
			return v;
		}
	}

	class List {

		private String user_srl;
		private String Title;
		private String Description;
		private String Date;
		private int Tag;
		private int status;

		public List(String _user_Srl, String _Title, String _Description,
				String _Date, int _Tag, int _Status) {
			this.user_srl = _user_Srl;
			this.Title = _Title;
			this.Description = _Description;
			this.Date = _Date;
			this.Tag = _Tag;
			this.status = _Status;
		}

		public String getUserSrl() {
			return user_srl;
		}

		public String getTitle() {
			return Title;
		}

		public String getDes() {
			return Description;
		}

		public String getDate() {
			return Date;
		}

		public int getTag() {
			return Tag;
		}

		public int getStatus() {
			return status;
		}

	}

	protected Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {
			setSupportProgressBarIndeterminateVisibility(false);

			
			if (msg.what == -1) {
				Global.ConnectionError(document_read.this);
			}

			if (msg.what == 1) {

				try {
					// page_srl//user_srl//name//title//content//date//status//privacy//comments//recommend//negative
					String[] array = msg.obj.toString().split("/LINE/.");
				//	Global.dumpArray(array);
					String page_srl = array[0];
					user_srl = array[1];
					String name = array[2];
					String title = array[3];
					String content = array[4];
					String date = array[5];
					String status = array[6];
					String privacy = array[7];
				      comments = array[8];
					String recommend = array[9];
					String negative = array[10];
					you_doc_status = Integer.parseInt(array[11]);

					
					//SetTitled
					if(title.matches("null")){
						getSupportActionBar().setTitle(name);
					}else{
						
					}
				

					getMemberInfo(user_srl);

					// Profiile
					
					boolean state = Global.CheckFileState(local_path
							+ "thumbnail/" +user_srl + ".jpg");
					
					if(state){
					profile.setImageDrawable(Drawable.createFromPath(local_path
							+ "thumbnail/" +user_srl + ".jpg"));
					}else{
						profile.setImageResource(R.drawable.person);
					}

					profile_title.setText(name);
					profile_des.setText(Global.formatTimeString(Long
							.parseLong(date)));
					setCommentsCount(Integer.parseInt(comments));
					doc_content.setText(Global.getValue(content));
					// Set comment
					getCommentsList(getStartComment(comments_count), 10);
					//Load menu again
					invalidateOptionsMenu();

				} catch (Exception e) {

				}
			}

			if (msg.what == 2) {

				try {

					String[] array = msg.obj.toString().split("/LINE/.");
				//	Global.dumpArray(array);
					String profile_pic = array[0];
					String profile_update = array[1];

					String user_srl = String.valueOf(msg.arg1);
			
					Global.SaveUserSetting(user_srl,String.valueOf(Global.getCurrentTimeStamp()), profile_pic);

					if (profile_pic.matches("Y")) {
						// Global.SaveUserSetting(user_srl, profile_update);
						ProfileUserImageDownload(user_srl);
						// Log.i("test", "Let s profile image download");

					}
					if (profile_pic.matches("N")) {
						File file = new File(local_path + user_srl + ".jpg");
						file.delete();
						File file_thum = new File(local_path + "thumbnail/"
								+ user_srl + ".jpg");
						file_thum.delete();
					}
				} catch (Exception e) {
					// MemberInfoError();

				}
			}

			if (msg.what == 3) {
				// Save File cache
				try {
					Log.i("Save", msg.arg1 + "");
					Global.SaveBitmapToFileCache((Bitmap) msg.obj, local_path,
							msg.arg1 + ".jpg");
					Global.createThumbnail((Bitmap) msg.obj, local_path
							+ "thumbnail/", msg.arg1 + ".jpg");

					m_adapter.notifyDataSetChanged();

					// Set Profile
					// profile.setImageDrawable(Drawable.createFromPath(local_path
					// + member_srl + ".jpg"));
					// Refresh();
				} catch (Exception e) {
				}
			}

			if (msg.what == 4) {
				String result = msg.obj.toString();
				comment_edittext.setEnabled(true);
				if (result.matches("comment_write_succeed")) {
				//	setCommentsCount(comments_count + 1);
					previous_count = 1;
					m_adapter.clear();
					comment_edittext.setText(null);
					getDoc();
					listView.setTranscriptMode(ListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
				} else {
					Global.ConnectionError(document_read.this);
				}
				// Log.i("Result","로그 정상 작동");
				Log.i("Result", msg.obj.toString());

			}

			if (msg.what == 5) {
				try {
					int moreload = -1;
					Log.i("Cmt", msg.obj.toString());
					String[] cmt = msg.obj.toString().split("/CMT/.");

					for (int i = 0; i < cmt.length; i++) {
						String[] array = cmt[i].split("/LINE/.");
						Global.dumpArray(array);
						// srl//user_srl//name//content//date//status/privacy
						String srl = array[0];
						String user_srl = array[1];
						String name = array[2];
						String content = array[3];
						String date = array[4];
						String status = array[5];
						String privacy = array[6];
						String you_status = array[7];
						Log.i("user", user_srl);

						if (previous_count > 1)
							moreload = i;
						getMemberInfo(user_srl);
						setList(moreload, Integer.parseInt(srl), user_srl,
								name, content,
								Global.formatTimeString(Long.parseLong(date)), Integer.parseInt(you_status));
						m_adapter.notifyDataSetChanged();
					}
					seePreviousComments(comments_count);
				} catch (Exception e) {

				}
			}
			
			//DeleteAct
			if (msg.what == 6) {
				Log.i("result", msg.obj.toString());
				String result = msg.obj.toString();
				if (result.matches("document_update_succeed")) {
					Global.toast(getString(R.string.deleted));
					UpdateFinishAct();
				}else{
					Global.toast(getString(R.string.error_des));
				}

			}
			
			//DeleteCommentAct
			if (msg.what == 7) {
				Log.i("result", msg.obj.toString());
				String result = msg.obj.toString();
				if (result.matches("comment_update_succeed")) {
					Global.toast(getString(R.string.deleted));
					refreshAct();
				}else{
					Global.toast(getString(R.string.error_des));
				}

			}

		}
	};
	
	public void UpdateFinishAct(){
		  Intent intent = new Intent();
		   this.setResult(RESULT_OK,intent);
		finish();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		MenuItem item;

		menu.add(0, 0, 0, getString(R.string.refresh))
				.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
		if(you_doc_status >= 4)
		menu.add(0, 1, 0, getString(R.string.delete)).setShowAsAction(
				MenuItem.SHOW_AS_ACTION_NEVER);
	//	menu.add(0, 2, 0, getString(R.string.private_content)).setShowAsAction(
	//			MenuItem.SHOW_AS_ACTION_NEVER);
		

		// item = menu.add(0, 1, 0, R.string.Main_MenuAddBookmark);
		// item.setIcon(R.drawable.ic_menu_add_bookmark);

		return true;
	}


	// 빽백키 상단액션바
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			onBackPressed();
			return true;
		case 0:
			//Refresh
			if (Globalvariable.okbutton == true) {
				// Set ok button disable
				refreshAct();
			}
			
			return true;
		case 1:
			//Delete
			//Refresh
		DeleteAlert();

			return true;
		default:
			return super.onOptionsItemSelected(item);
		}

	}

}