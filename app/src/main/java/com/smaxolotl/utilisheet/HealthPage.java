package com.smaxolotl.utilisheet;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.SeekBar;
import android.widget.LinearLayout;
import android.widget.Button;
import android.widget.Toast;
import android.text.InputType;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;
import android.widget.EditText;

class HpBar{
	String name;
	TextView title, status;
	SeekBar bar;
	int max, min, curr;
	LinearLayout box;
	
	public HpBar(final Context context, String name, int min, int curr, int max){
		this.name = name;
		this.max = max;
		this.min = min;
		this.curr = curr;
		box = new LinearLayout(context);
		bar = new SeekBar(context);
		bar.setPadding(25, 0, 25, 0);
		title = new TextView(context);
		title.setPadding(25,0,0,0);
		title.setTextSize(20);
		title.setClickable(true);
		title.setOnClickListener(new OnClickListener(){

			public void onClick(View view) {
				intFromPopup(context);				
			}
			
		});
		status = new TextView(context);
		status.setPadding(25,0,0,0);
		status.setTextSize(15);
		setupBar();
		setupBox();
		updateText();
	}
	
	void setupBar(){
		bar.setMax(max - min);
		bar.setProgress(curr);
	}
	
	void setupBox(){
		box.setOrientation(LinearLayout.VERTICAL);
		box.addView(title);
		box.addView(bar);
		box.addView(status);
	}
	
	void updateText(){
		title.setText(name + " HP");
		status.setText((curr + min) + "/" + max);
	}
	
	boolean isSafe(String string){
		if (string != "" && string.trim().length() != 0){
			return true;
		} else{
			return false;
		}
	}
	
	int getIntFromET(EditText et){
		if (isSafe(String.valueOf(et.getText()))){
			try{
				return Integer.valueOf(et.getText().toString());
			} catch (NumberFormatException nfe){
				return 0;
			}
		} else {
			return 0;
		}
	}
	
	void intFromPopup(final Context context){
		
		final LinearLayout box = new LinearLayout(context);
		final EditText minInput = new EditText(context);
		minInput.setInputType(0x00001002);
		final EditText maxInput = new EditText(context);
		maxInput.setInputType(InputType.TYPE_CLASS_NUMBER);
		minInput.setHint("Enter min");
		maxInput.setHint("Enter max");
		minInput.setText(min + "");
		maxInput.setText(max + "");
		box.setOrientation(LinearLayout.HORIZONTAL);
		box.addView(minInput);
		box.addView(maxInput);
		
		new AlertDialog.Builder(context)
		.setTitle("Change " + title.getText().toString())
		.setView(box)
		.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dlg, int which) {
				if (isSafe(String.valueOf(maxInput.getText())) && isSafe(String.valueOf(minInput.getText())) && (getIntFromET(minInput) < getIntFromET(maxInput))) {
					max = getIntFromET(maxInput);
					min = getIntFromET(minInput);
					setupBar();
					curr = bar.getMax();
					bar.setProgress(curr);
					updateText();
		    	}
		    	else {
		    		Toast.makeText(context, "No numbers entered or max less than min", Toast.LENGTH_SHORT).show();
		    	}
			}
		})
		.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dlg, int which) {
			}
		})
		.show();
	}
	
}

public class HealthPage extends Activity implements OnClickListener {
	
	LinearLayout mainLayout, tweakLayout;
	Button tweakDown;
	Button tweakUp;
	Button tweakDone;
	int startNum;
	boolean started = false;
	TextView tweakText;
	SeekBar currBar;
	TextView changeText;
	HpBar mainCharHp;

	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.healthpage);
		
		SharedPreferences prefs = getSharedPreferences("peeps", MODE_PRIVATE);
		
		mainLayout = (LinearLayout)findViewById(R.id.mainLayout);
		tweakLayout = (LinearLayout)findViewById(R.id.tweakLayout);
		tweakDown = (Button)findViewById(R.id.tweakDown);
		tweakDown.setOnClickListener(this);
		tweakUp = (Button)findViewById(R.id.tweakUp);
		tweakUp.setOnClickListener(this);
		tweakDone = (Button)findViewById(R.id.tweakDone);
		tweakDone.setOnClickListener(this);
		tweakText = (TextView)findViewById(R.id.tweakNum);
		changeText = (TextView)findViewById(R.id.changeText);
		
		mainCharHp = new HpBar(this, "My", prefs.getInt("charMinHp", -10), prefs.getInt("charCurrHp", 110), prefs.getInt("charMaxHp", 100));
		
		mainLayout.addView(mainCharHp.box);

		mainCharHp.bar
				.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

					public void onStopTrackingTouch(SeekBar seekBar) {
					}
					
					public void onStartTrackingTouch(SeekBar seekBar) {
						if (!started){
							startNum = seekBar.getProgress();
							started = true;
							tweakLayout.setVisibility(View.VISIBLE);
							currBar = seekBar;
							mainCharHp.title.setEnabled(false);
						}
					}

					public void onProgressChanged(SeekBar seekBar,
							int progress, boolean fromUser) {
						tweakText.setText(Integer.toString(seekBar
								.getProgress() - startNum));
						changeText.setText("New value: " + (seekBar.getProgress()+ mainCharHp.min));

					}
				});
	}
	
	@Override
	public void onStop(){
		super.onStop();
		
		SharedPreferences prefs = getSharedPreferences("peeps", MODE_PRIVATE);
		SharedPreferences.Editor ed = prefs.edit();
		ed.putInt("charMaxHp", mainCharHp.max);
		ed.putInt("charCurrHp", mainCharHp.curr);
		ed.putInt("charMinHp", mainCharHp.min);
		ed.commit();
	}

	public void onClick(View view) {
		if (view == tweakDown && currBar.getProgress() > 0) {
			currBar.setProgress(currBar.getProgress() - 1);
		} else if (view == tweakUp && currBar.getProgress() < currBar.getMax()) {
			currBar.setProgress(currBar.getProgress() + 1);
		} else if (view == tweakDone) {
			mainCharHp.curr = currBar.getProgress();
			mainCharHp.title.setEnabled(true);
			mainCharHp.updateText();
			tweakLayout.setVisibility(View.GONE);
			tweakText.setText("0");
			started = false;
		}		
	}	
}
