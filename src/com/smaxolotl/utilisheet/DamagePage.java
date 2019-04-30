package com.smaxolotl.utilisheet;

import java.util.Random;
import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Toast;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;

class Attack {
	String name;
	int dieNum, dieSize, baseDam, arrayId, critMulti;
	int powerDamMod; // Damage added when power attack box is checked
	CheckBox hitBox, critBox;
	TextView hitText, damText;
	LinearLayout row;

	public Attack(Context context, String name, int numOfDice, int sizeOfDice,
			int baseDamMod, int powerAttackBonus, int critMulti, boolean powerAttack) {
		this.name = name;
		dieNum = numOfDice;
		dieSize = sizeOfDice;
		baseDam = baseDamMod;
		powerDamMod = powerAttackBonus;
		this.critMulti = critMulti;
		row = new LinearLayout(context);
		hitBox = new CheckBox(context);
		critBox = new CheckBox(context);
		hitText = new TextView(context);
		hitText.setClickable(true);
		damText = new TextView(context);
		updateText(powerAttack);
		createRow();
	}

	public int rollDamage(boolean powerAttack) {
		if (hitBox.isChecked() == false) {
			return 0;
		} else {
			int total = DamagePage.rollDice(dieNum, dieSize);
			total += getDamageMod(powerAttack);
			if (total < 1){
				total = 1;
			}
			return critBox.isChecked() ? total * critMulti : total;
		}
	}

	int getDamageMod(boolean powerAttack) {
		int total = baseDam;
		if (powerAttack) {
			total += powerDamMod;
		}
		return total;
	}

	public String getDamText(boolean powerAttack) {
		String prefix = "";
		String suffix = "";
		if (critBox.isChecked() && critMulti > 1){
			prefix = Integer.toString(critMulti) + "x(";
			suffix = ")";
		}
		if (dieNum == 0){
			return (getDamageMod(powerAttack) > 0) ? prefix + "+" + Integer.toString(getDamageMod(powerAttack)) + suffix : prefix + Integer.toString(getDamageMod(powerAttack)) + suffix;
		} else if (getDamageMod(powerAttack) == 0){
			return prefix + Integer.toString(dieNum) + "d" + Integer.toString(dieSize) + suffix;
		} else {		
			return (getDamageMod(powerAttack) > 0) ? prefix + Integer.toString(dieNum) + "d" + Integer.toString(dieSize)
				+ " +" + Integer.toString(getDamageMod(powerAttack)) + suffix : prefix + Integer.toString(dieNum) + "d" + Integer.toString(dieSize)
				+ Integer.toString(getDamageMod(powerAttack)) + suffix;
		}
	}

	// Should tweak the weighting at some point so that it looks better
	void createRow() {
		row.setOrientation(LinearLayout.HORIZONTAL);
		row.setGravity(Gravity.CENTER_HORIZONTAL);
		row.addView(hitBox, new LinearLayout.LayoutParams(0, -2, 0.25f));
		row.addView(hitText, new LinearLayout.LayoutParams(0, -2, 0.25f));
		row.addView(damText, new LinearLayout.LayoutParams(0, -2, 0.25f));
		row.addView(critBox, new LinearLayout.LayoutParams(0, -2, 0.25f));
	}

	public void updateText(boolean powerAttack) {
		hitText.setText(name);
		damText.setText(getDamText(powerAttack));
	}

	public int getMin(boolean powerAttack) {
		if (hitBox.isChecked() == false) {
			return 0;
		} else {
			int total = dieNum;
			total += getDamageMod(powerAttack);
			if (total < 1){
				total = 1;
			}
			return critBox.isChecked() ? total * critMulti : total;
		}
	}

	public int getMax(boolean powerAttack) {
		if (hitBox.isChecked() == false) {
			return 0;
		} else {
			int total = dieNum * dieSize;
			total += getDamageMod(powerAttack);
			if (total < 1){
				total = 1;
			}
			return critBox.isChecked() ? total * critMulti : total;
		}
	}
}

public class DamagePage extends Activity {

	static CheckBox powerAttack;
	Button rollDam, changeGo, changeCancel, attackAdd;
	TextView damText, aveText;
	LinearLayout mainLayout, changeLayout;
	EditText changeName, changeDieNum, changeDieSize, changeDamageMod,
			changePowerBonus, changeCritMulti;
	int target = -1;
	int numOfAttacks = 0;
	
	final static Random rand = new Random();

	// Create an ArrayList to hold all the attacks
	ArrayList<Attack> attackList = new ArrayList<Attack>();

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.damage);
		
		SharedPreferences prefs = getSharedPreferences("attacks", MODE_PRIVATE);
		numOfAttacks = prefs.getInt("numOfAttacks", 0);
		
		
		
		attackAdd = (Button) findViewById(R.id.attackAdd);
		attackAdd.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				changeToChange(-1);
			}
		});

		powerAttack = (CheckBox) findViewById(R.id.powerAttack);
		rollDam = (Button) findViewById(R.id.rollButton);
		damText = (TextView) findViewById(R.id.damTotal);
		aveText = (TextView) findViewById(R.id.aveText);
		mainLayout = (LinearLayout) findViewById(R.id.mainLayout);
		changeLayout = (LinearLayout) findViewById(R.id.changeLayout);
		changeName = (EditText) findViewById(R.id.changeName);
		changeDieNum = (EditText) findViewById(R.id.changeDieNum);
		changeDieSize = (EditText) findViewById(R.id.changeDieSize);
		changeDamageMod = (EditText) findViewById(R.id.changeDamageMod);
		changePowerBonus = (EditText) findViewById(R.id.changePowerBonus);
		changeCritMulti = (EditText) findViewById(R.id.changeCritMulti);
		changeGo = (Button) findViewById(R.id.changeGo);
		changeCancel = (Button) findViewById(R.id.changeCancel);
		
		if (numOfAttacks != 0){
			for (int i = 1; i <= numOfAttacks; i++){
				attackList.add(new Attack(this, prefs.getString("name" + i, "Bite"), prefs.getInt("numOfDice" + i, 1), prefs.getInt("sizeOfDice" + i, 4), prefs.getInt("baseDamMod" + i, -2), prefs.getInt("powerAttackBonus" + i, 2), prefs.getInt("critMulti" + i, 2), false));
				mainLayout.addView(
					attackList.get(attackList.size() - 1).row,
					attackList.size());
				attackList.get(attackList.size() - 1).hitBox
					.setOnCheckedChangeListener(new OnCheckedChangeListener() {
						public void onCheckedChanged(
							CompoundButton box, boolean checked) {
							aveTextUpdate();
						}
					});
				attackList.get(attackList.size() - 1).critBox
					.setOnCheckedChangeListener(new OnCheckedChangeListener() {
						public void onCheckedChanged(
							CompoundButton box, boolean checked) {
							aveTextUpdate();
							updateText();
						}
					});

				attackList.get(attackList.size() - 1).hitText
					.setOnClickListener(new OnClickListener() {
						public void onClick(View v) {
							attackDialog(attackList.get(attackList
														.size() - 1));
						}
					});
			}
			idUpdate(attackList);
		}
		
		powerAttack.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			public void onCheckedChanged(CompoundButton box, boolean checked) {
				// Refresh damage display when it's changed
				updateText();
			}
		});

		changeGo.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if (isSafe()) {
					if (target == -1) {
						// Add new attack to the attack ArrayList
						attackList.add(new Attack(getApplicationContext(),
								changeName.getText().toString(), Integer
										.valueOf(changeDieNum.getText()
												.toString()), Integer
										.valueOf(changeDieSize.getText()
												.toString()), Integer
										.valueOf(changeDamageMod.getText()
												.toString()), Integer
										.valueOf(changePowerBonus.getText()
												.toString()), Integer
										.valueOf(changeCritMulti.getText()
												.toString()), powerAttack.isChecked()));
						mainLayout.addView(
								attackList.get(attackList.size() - 1).row,
								attackList.size());
						attackList.get(attackList.size() - 1).hitBox
								.setOnCheckedChangeListener(new OnCheckedChangeListener() {
									public void onCheckedChanged(
											CompoundButton box, boolean checked) {
										aveTextUpdate();
									}
								});
						attackList.get(attackList.size() - 1).critBox
								.setOnCheckedChangeListener(new OnCheckedChangeListener() {
									public void onCheckedChanged(
											CompoundButton box, boolean checked) {
										aveTextUpdate();
										updateText();
									}
								});

						attackList.get(attackList.size() - 1).hitText
								.setOnClickListener(new OnClickListener() {
									public void onClick(View v) {
										attackDialog(attackList.get(attackList
												.size() - 1));
									}
								});
					} else {
						attackList.get(target).name = changeName.getText()
								.toString();
						attackList.get(target).dieNum = Integer
								.valueOf(changeDieNum.getText().toString());
						attackList.get(target).dieSize = Integer
								.valueOf(changeDieSize.getText().toString());
						attackList.get(target).baseDam = Integer
								.valueOf(changeDamageMod.getText().toString());
						attackList.get(target).powerDamMod = Integer
								.valueOf(changePowerBonus.getText().toString());
						attackList.get(target).critMulti = Integer
								.valueOf(changeCritMulti.getText().toString());
					}

					// Switch back to the attack display layout
					changeLayout.setVisibility(View.GONE);
					mainLayout.setVisibility(View.VISIBLE);
					updateText();
					idUpdate(attackList);
					
					//clear fields
					changeName.setText("");
					changeDieNum.setText("");
					changeDieSize.setText("");
					changeDamageMod.setText("");
					changePowerBonus.setText("");
					changeCritMulti.setText("");
				} else {
					// Prevent app crashing when a field isn't filled out
					complain("Fill out all fields");
				}
			}
		});

		// Do nothing and switch back to the attack display layout
		changeCancel.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				changeLayout.setVisibility(View.GONE);
				mainLayout.setVisibility(View.VISIBLE);
			}
		});

		rollDam.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				int totalDam = 0;

				for (int i = 0; i < attackList.size(); i++) {
					totalDam += attackList.get(i).rollDamage(powerAttack.isChecked());
				}

				damText.setText("Damage = " + totalDam);
				complain("" + totalDam);
			}
		});

		// This is the end of onCreate, hence the text update
		updateText();
	}
	
	@Override
	public void onStop(){
		super.onStop();
		SharedPreferences prefs = getSharedPreferences("attacks", MODE_PRIVATE);
		SharedPreferences.Editor ed = prefs.edit();
		
		ed.putInt("numOfAttacks", attackList.size());
		ed.clear();
		
		for (int i = 1; i <= (attackList.size()); i++){
			ed.putString("name" + i, attackList.get(i-1).name);
			ed.putInt("numOfDice" + i, attackList.get(i-1).dieNum);
			ed.putInt("sizeOfDice" + i, attackList.get(i-1).dieSize);
			ed.putInt("baseDamMod" + i, attackList.get(i-1).baseDam);
			ed.putInt("powerAttackBonus" + i, attackList.get(i-1).powerDamMod);
			ed.putInt("critMulti" + i, attackList.get(i-1).critMulti);
		}
		ed.commit();
	}
	
	@Override
	public void onBackPressed(){
		if (changeLayout.getVisibility() == View.VISIBLE){
			changeLayout.setVisibility(View.GONE);
			mainLayout.setVisibility(View.VISIBLE);
			return;
		} else {
			this.finish();
			return;
		}
	}

	public void changeAttack(Attack attack) {
		changeName.setText(attack.name);
		changeDieNum.setText(String.valueOf(attack.dieNum));
		changeDieSize.setText(String.valueOf(attack.dieSize));
		changeDamageMod.setText(String.valueOf(attack.baseDam));
		changePowerBonus.setText(String.valueOf(attack.powerDamMod));
		changeCritMulti.setText(String.valueOf(attack.critMulti));
		changeToChange(attack.arrayId);
	}

	public void removeAttack(Attack attack) {
		mainLayout.removeView(attack.row);
		attackList.remove(attack.arrayId);
		attackList.trimToSize();
		updateText();
		idUpdate(attackList);
	}

	// Ask what wants to be done with attack
	public void attackDialog(final Attack attack) {
		target = attack.arrayId;
		updateText();
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(
				"What would you like to do with " + attack.name + " attack?")
				.setCancelable(false)
				.setPositiveButton("Change",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								changeAttack(attack);
							}
						})
				.setNeutralButton("Remove",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								removeAttack(attack);
							}
						})
				.setNegativeButton("Cancel",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								dialog.cancel();
							}
						});
		AlertDialog alert = builder.create();
		alert.show();
	}

	public void idUpdate(ArrayList<Attack> list) {
		for (int i = 0; i < list.size(); i++) {
			list.get(i).arrayId = i;
			list.get(i).hitText.setId(i);
			//This makes it work otherwise target kept getting changed to the last attack added
			list.get(i).hitText.setOnClickListener(new OnClickListener(){
				public void onClick(View v){
					target = v.getId();
					attackDialog(attackList.get(target));
				}
			});
		}
		updateText();
	}

	public void aveTextUpdate() {
		int minDam = 0;
		//double totalAve = 0;
		int maxDam = 0;

		for (int i = 0; i < attackList.size(); i++) {
			minDam += attackList.get(i).getMin(powerAttack.isChecked());
		}

		for (int i = 0; i < attackList.size(); i++) {
			maxDam += attackList.get(i).getMax(powerAttack.isChecked());
		}

		aveText.setText(minDam + " <--> " + maxDam);
	}

	// Gives result of rolling dice, input is like standard notation no. of
	// dice and size of dice (2d6 is rolled as rollDice(2,6))
	static public int rollDice(int numDie, int dieSize) {
		int total = 0;
		for (int i = 0; i < numDie; i++) {
			total += rand.nextInt(dieSize) + 1;
		}
		return total;
	}

	// Made this to avoid having errors about incorrect context within
	// OnClickListeners
	// 'getApplicationContext()' instead of 'this' may have fixed it but this
	// makes
	// toasts nice and short to make so I'm keeping it
	public void complain(String say) {
		Toast.makeText(this, say, Toast.LENGTH_SHORT).show();
	}

	// Checks to make sure all the fields are filled out
	public boolean isSafe() {
		if (changeName.length() == 0) {
			return false;
		} else if (changeDieNum.length() == 0) {
			return false;
		} else if (changeDieSize.length() == 0) {
			return false;
		} else if (changeDamageMod.length() == 0) {
			return false;
		} else if (changePowerBonus.length() == 0) {
			return false;
		} else if (changeCritMulti.length() == 0) {
			return false;
		} else {
			return true;
		}
	}

	public void changeToChange(int id) {
		mainLayout.setVisibility(View.GONE);
		changeLayout.setVisibility(View.VISIBLE);
		target = id;
	}

	public void updateText() {
		for (int i = 0; i < attackList.size(); i++) {
			attackList.get(i).updateText(powerAttack.isChecked());
		}
		aveTextUpdate();
	}
}
