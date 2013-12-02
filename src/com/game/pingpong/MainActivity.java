package com.game.pingpong;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

import org.andengine.engine.camera.SmoothCamera;
import org.andengine.engine.camera.hud.HUD;
import org.andengine.engine.handler.timer.ITimerCallback;
import org.andengine.engine.handler.timer.TimerHandler;
import org.andengine.engine.options.EngineOptions;
import org.andengine.engine.options.ScreenOrientation;
import org.andengine.engine.options.resolutionpolicy.RatioResolutionPolicy;
import org.andengine.entity.Entity;
import org.andengine.entity.modifier.AlphaModifier;
import org.andengine.entity.modifier.MoveModifier;
import org.andengine.entity.modifier.ParallelEntityModifier;
import org.andengine.entity.modifier.ScaleModifier;
import org.andengine.entity.modifier.SequenceEntityModifier;
import org.andengine.entity.primitive.Ellipse;
import org.andengine.entity.primitive.Mesh.DrawMode;
import org.andengine.entity.primitive.Rectangle;
import org.andengine.entity.primitive.Vector2;
import org.andengine.entity.scene.IOnSceneTouchListener;
import org.andengine.entity.scene.Scene;
import org.andengine.entity.scene.background.Background;
import org.andengine.entity.sprite.Sprite;
import org.andengine.entity.text.Text;
import org.andengine.entity.util.FPSLogger;
import org.andengine.input.sensor.acceleration.AccelerationData;
import org.andengine.input.sensor.acceleration.IAccelerationListener;
import org.andengine.input.touch.TouchEvent;
import org.andengine.opengl.font.Font;
import org.andengine.opengl.font.FontFactory;
import org.andengine.opengl.texture.ITexture;
import org.andengine.opengl.texture.TextureOptions;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlasTextureRegionFactory;
import org.andengine.opengl.texture.region.ITextureRegion;
import org.andengine.opengl.vbo.VertexBufferObjectManager;
import org.andengine.ui.activity.SimpleBaseGameActivity;
import org.andengine.util.color.Color;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.util.Log;
import android.widget.Toast;

public class MainActivity extends SimpleBaseGameActivity implements IOnSceneTouchListener, IAccelerationListener {

	SimpleBaseGameActivity activity;
	
	Random ran = new Random();
	
	private SmoothCamera camera;
	private int CH = 1280, CW = 720;
	private Scene gameScene;

	private boolean paused = true;
	int score = 0, maxScore = 0;
	int delta = 3;
	
	float coef = 2f;
	
	float speed = 0; // speed
	float force = 5; // force
	float gravity = 0.2f; // gravity
	float rb = 150, rg = 30, rr = 70, r = rb; // ball distance: rb - begin, rg - ground, rr - racket, r - current
			
	Ellipse racket, racket2, ball; Sprite ballSprite;
	int rR = 300, xR = CW / 2, yR = CH / 2;
	int xB = CW / 2, yB = CH / 2;
	float dx = 0, dy = 0, dz = 0, ax = 0, ay = 0, az = 0, tx = 0, ty = 0;
	Color woodColor = new Color(1, 0.95f, 0.87f);
	
	Rectangle handle, handle2, hitHandle, howToEntity;
	int wh = 100;
	
	ArrayList<Rectangle> hudButtons = new ArrayList<Rectangle>();
	ArrayList<Rectangle> hudIndicators = new ArrayList<Rectangle>();
	int cam = 1;
	Font font, fontSmall;
	Text curScoreText, scoreText, hsGoldText, hsSilverText, hsBronzeText, howToText; 
	boolean howToTextVisible = false;
	Entity arrows;
	boolean vibro = true;
	
	private BitmapTextureAtlas ballTA;
	private ITextureRegion ballTR;
	
	VertexBufferObjectManager vbom;
	
	final Color red = new Color(1, 0, 0), green = new Color(0, 1, 0), blue = new Color(0, 0, 1), cyan = new Color(0, 1, 1);
	final float on = 1f, off = 0.3f;
	int space = 3;
	
	float textAlpha = 0.7f;
	
	int g = 0, s = 0, b = 0; // HIGHSCORES
	private SharedPreferences sp;
	private SharedPreferences.Editor spe;
	
	Color gColor = new Color(0.4f, 0.4f, 0.4f), sColor = new Color(0.4f, 0.4f, 0.4f), bColor = new Color(0.4f, 0.4f, 0.4f), curScoreColor = new Color(1.0f, 1.0f, 1.0f);

	@Override
	public EngineOptions onCreateEngineOptions() {
		camera = new SmoothCamera(0, 0, CW, CH, 300, 300, 0.7f);
		EngineOptions eo = new EngineOptions(true, ScreenOrientation.PORTRAIT_FIXED, new RatioResolutionPolicy(CW, CH), camera);;
		eo.getTouchOptions().setNeedsMultiTouch(true);
		sp = getPreferences(Context.MODE_PRIVATE);
		spe = sp.edit();
		activity = this;
		return eo;
	}

	@Override
	protected void onCreateResources() {	
		ballTA = new BitmapTextureAtlas(this.getTextureManager(), 256, 256);
		ballTR = BitmapTextureAtlasTextureRegionFactory.createFromAsset(ballTA, this, "ball.png", 0, 0);
		ballTA.load();
		
		font = FontFactory.createFromAsset(this.getFontManager(), this.getTextureManager(), 256, 256, this.getAssets(),
			    "font.ttf", 46, true, android.graphics.Color.WHITE);
			  font.load();
		
		fontSmall = FontFactory.createFromAsset(this.getFontManager(), this.getTextureManager(), 256, 256, this.getAssets(),
			    "font.ttf", 29, true, android.graphics.Color.WHITE);
		fontSmall.load();
	}

	private Rectangle createHUDIndicator(HUD h, final int num, final int n, final int nc) { 
		float alpha = on;
		Color c = blue;
		switch (num) {
			case 0: c = red; break;
			case 1: c = cyan; break;
		}
		
		if (num > n - nc) alpha = off;
		
		Rectangle rect = new Rectangle(space + num * CW / n, 20, CW / n - 2 * space, 20, vbom);//new Rectangle(20 + num * CW / n, 20, CW / n - 40, 20, vbom);
		rect.setColor(c);
		rect.setAlpha(alpha);
		return rect;
	}
	
	private Rectangle createHUDButton(HUD h, final int num, final int n, final int nc) {	
		Rectangle button = new Rectangle(space + num * CW / n, 40, CW / n - 2 * space, 90, vbom) {
			@Override
			public boolean onAreaTouched(TouchEvent pSceneTouchEvent, float X, float Y) {
				if (pSceneTouchEvent.isActionUp()) {
					switch(num) {
					
					case 0: // reset
						resetGame(); 
						break;

					case 1: // vibro
						vibro = !vibro;
						vibrate(20);
						if (hudIndicators.get(num).getAlpha() == on)  {
							hudIndicators.get(num).setAlpha(off);
							}
						else 
							hudIndicators.get(num).setAlpha(on);
						break;

					case 2:
						cam = 1;
						for (int i = n - nc; i < n; i ++) hudIndicators.get(i).setAlpha(off);
						hudIndicators.get(num).setAlpha(on);
						break;

					case 3:
						cam = 2;
						for (int i = n - nc; i < n; i ++) hudIndicators.get(i).setAlpha(off);
						hudIndicators.get(num).setAlpha(on);
						break;

					case 4:
						cam = 3;
						for (int i = n - nc; i < n; i ++) hudIndicators.get(i).setAlpha(off);
						hudIndicators.get(num).setAlpha(on);
						break;

					}
				}
				return true;
			}
		};
		button.setColor(0, 0, 0);
		button.setAlpha(0.0f);
		h.registerTouchArea(button);
		
		String s = "";
		switch (num) {

		case 0: // reset
			s = "START";
			break;

		case 1:
			s = "VIBRO";// vibro
			break;

		case 2:
			s = "CAM 1";
			break;

		case 3:
			s = "CAM 2";
			break;

		case 4:
			s = "CAM 3";
			break;

		}
		Text t = new Text(0, 0, font, s, "XXXXX".length(), vbom);
		t.setScale(0.5f);
		t.setAlpha(textAlpha);
		button.attachChild(t);
		
		return button;
	}
	
	private HUD createHUD() {
		HUD hud = new HUD();		
		
		int ncams = 3;
		int nbuttons = 5;
		curScoreText = new Text(20, 150, font, "", "SCORE: XXXXXXXXXX".length(), vbom);
		curScoreText.setColor(curScoreColor);
		hud.attachChild(curScoreText);
		
		int yb = 210, dy = 50; float sc = 0.8f;
		hsGoldText = new Text(20, yb, font, "", "XXXXXXXXXX".length(), vbom);
		hsGoldText.setColor(Color.WHITE);
		hsGoldText.setAlpha(textAlpha);
		hsGoldText.setScale(sc);
		hud.attachChild(hsGoldText);

		hsSilverText = new Text(20, yb + dy, font, "", "XXXXXXXXXX".length(), vbom);
		hsSilverText.setColor(Color.WHITE);
		hsSilverText.setAlpha(textAlpha);
		hsSilverText.setScale(sc);
		hud.attachChild(hsSilverText);

		hsBronzeText = new Text(20, yb + dy * 2, font, "", "XXXXXXXXXX".length(), vbom);
		hsBronzeText.setColor(Color.WHITE);
		hsBronzeText.setAlpha(textAlpha);
		hsBronzeText.setScale(sc);
		hud.attachChild(hsBronzeText);
		
		updateScores();
		
		Rectangle r;
		for (int i = 0; i < nbuttons; i ++) {
			r = createHUDIndicator(hud, i, nbuttons, ncams);
			hudIndicators.add(r);
			hud.attachChild(r);
		}
		for (int i = 0; i < nbuttons; i ++) {
			r = createHUDButton(hud, i, nbuttons, ncams);
			hudButtons.add(r);
			hud.attachChild(r);
		}
		
		howToEntity = new Rectangle(20, 600, CW - 20, 450, vbom);
		howToEntity.setColor(0.0f, 0.0f, 0.0f);
		String s = "1. Place your device parallel\n" +
				"    to the ground\n\n" +
				"2. Click the START button on the top\n\n" +
				"3. Tilt the device to control the\n" +
				"    racket angle\n\n" +
				"4. Shake your device to beat the\n" +
				"    ball when it falls\n" +
				"    on the racket\n\n" +
				"5. Correct the ball trajectory\n" +
				"    by pressing the screen\n" +
				"    in the according point";
		howToText = new Text(10, 25, fontSmall, s, s.length(), vbom);
		howToEntity.attachChild(howToText);
		howToEntity.setAlpha(0.7f);
		howToEntity.setVisible(false);
		hud.attachChild(howToEntity);
		Rectangle howToR = new Rectangle(0, CH - 100, 300, 100, vbom) {
			@Override
			public boolean onAreaTouched(TouchEvent pSceneTouchEvent, float X, float Y) {
				if (pSceneTouchEvent.isActionUp()) {
					switchHowTo();
				} 
				return true;
			}};
			howToR.setAlpha(0f);
			Text t = new Text(0, 0, font, "HOW TO PLAY", "XXXXXXXXXXX".length(), vbom);
			t.setScale(0.5f);
			t.setAlpha(textAlpha);
			howToR.attachChild(t);
			hud.registerTouchArea(howToR);
			hud.attachChild(howToR);

			Rectangle resetHS = new Rectangle(300, CH - 100, 300, 100, vbom) {
				@Override
				public boolean onAreaTouched(TouchEvent pSceneTouchEvent, float X, float Y) {
					if (pSceneTouchEvent.isActionUp()) {
						activity.runOnUiThread(new Runnable() {
							  public void run() {
						AlertDialog.Builder ad = new AlertDialog.Builder(activity);
				        ad.setTitle("CLEAR HIGHSCORES");
				        ad.setMessage("Your highscores will be erased if you confirm.");
				        ad.setPositiveButton("OK", new DialogInterface.OnClickListener() {
				            public void onClick(DialogInterface dialog, int which) {
				            	spe.putInt("gold", 0);
								spe.putInt("silver", 0);
								spe.putInt("bronze", 0);
								spe.commit();
								updateScores();
				          } });
				        ad.show();
							  }
						});
					} 
					return true;
				}};
				resetHS.setAlpha(0f);
				t = new Text(0, 0, font, "CLEAR HIGHSCORES", "XXXXXXXXXXXXXXXX".length(), vbom);
				t.setScale(0.5f);
				t.setAlpha(textAlpha);
				resetHS.attachChild(t);
				hud.registerTouchArea(resetHS);
				hud.attachChild(resetHS);

			return hud;
	}
	
	protected void switchHowTo() {
		if (!howToEntity.isVisible()) {
			howToEntity.setVisible(true);
		} else { 
			howToEntity.setVisible(false);	
			}
	}

	@Override
	protected Scene onCreateScene() {
		mEngine.registerUpdateHandler(new FPSLogger());
		vbom = this.getVertexBufferObjectManager();
		gameScene = new Scene();
		gameScene.setOnSceneTouchListener(this);
		enableAccelerationSensor(this);
		mEngine.enableVibrator(this);

		
		
		gameScene.setBackground(new Background(0.0f, 0.0f, 0.0f));
		
		handle2 = new Rectangle((CW - wh) / 2, CH / 2, wh, CH / 2, vbom);
		handle2.setColor(woodColor);
		gameScene.attachChild(handle2);
		
		racket2 = new Ellipse(xR, yR, rR, rR, vbom);
		racket2.setDrawMode(DrawMode.TRIANGLE_FAN);
		racket2.setColor(woodColor);
		gameScene.attachChild(racket2);
		
		handle = new Rectangle((CW - wh) / 2, CH / 2, wh, CH / 2, vbom);
		hitHandle = new Rectangle((CW - wh - rr) / 2, (CH - rr) / 2, wh + 2 * rr, CH / 2 * 2 * rr, vbom);
		handle.setColor(0.776f, 0.713f, 0.6f);
		gameScene.attachChild(handle);
		
		racket = new Ellipse(xR, yR, rR, rR, vbom);
		racket.setDrawMode(DrawMode.TRIANGLE_FAN);
		racket.setColor(0.5f, 0.0f, 0.25f);
		gameScene.attachChild(racket);
		
		ballSprite = new Sprite(- r, - r, 2 * r, 2 * r, ballTR, vbom);
		ball = new Ellipse(xB, yB, r, r, vbom);
		ball.setColor(Color.WHITE);
		ball.setAlpha(0f);
		ball.attachChild(ballSprite);
		gameScene.attachChild(ball);
		
		scoreText = new Text(CW / 2, CH / 2, font, "", "XXXXXX".length(), vbom);
		gameScene.attachChild(scoreText);
		
		camera.setChaseEntity(ball);
		
		gameScene.registerUpdateHandler(new TimerHandler(1f / 60.0f, true, new ITimerCallback() {
            @Override
            public void onTimePassed(final TimerHandler pTimerHandler) {
            	if (paused) return;

            	// <>
            	r += speed;
            	speed -= gravity;

            	// drawing
            	float scale = r / rb;
            	ball.setScale(scale);
            	ball.setPosition(ball.getX() + dx + tx, ball.getY() + dy + ty);
            	if (scale > 1 && scale < 1.5f) ballSprite.setAlpha(2 - scale); else if (scale >= 1.5f) ballSprite.setAlpha(0.5f); else ballSprite.setAlpha(1f);
            	switch (cam) {
            	case 1: camera.setZoomFactor(rb / r); break;
            	case 2: camera.setZoomFactorDirect(rb / r); break;
            	case 3: camera.setZoomFactorDirect(1); break;
            	}
            	
            	if (r <= rr) { // bouncing?
            		// on racket
            		if (Math.sqrt((ball.getX() - racket.getX()) * (ball.getX() - racket.getX()) + (ball.getY() - racket.getY()) * (ball.getY() - racket.getY())) <= rR 
            				|| handle.contains(ball.getX(), ball.getY())) {
            			if (force > 1) 
            				vibrate(20);
            			
            			dx = ax * 2;
            			dy = ay * 2;
            			dz = az;
            			
            			if (dz < 10) dz = 0; else dz = az - 9;
            			force = force / 2 + dz;
            			speed = force;
            			
            			// score
            			int ds = calcScore();
            			score += ds;
            			scoreText.setColor(1, 1, 1);
            			scoreText.setScale(1);
            			scoreText.setAlpha(1);
            			if (ds > 0) 
            				scoreText.setText("+" + ds); 
            			else 
            				scoreText.setText(ds + "");
            			scoreText.setPosition(ball.getX() + 100, ball.getY() - 100);       
            			scoreText.registerEntityModifier(new ParallelEntityModifier(
            					new AlphaModifier(0.5f, 1, 0), 
            					new ScaleModifier(0.5f, 1, (float)ds / 50), 
            					new MoveModifier(0.5f, ball.getX() + 100, ball.getX() + 100 + dx * 20, ball.getY() - 100, ball.getY() - 100 + dy * 20)));
            			curScoreText.setText("SCORE: " + score);
            			
            			// fell from racket
            			} else if (Math.sqrt((ball.getX() - racket.getX()) * (ball.getX() - racket.getX()) + (ball.getY() - racket.getY()) * (ball.getY() - racket.getY())) <= rR + r) {
            				vibrate(40);
                			dx = (ball.getX() - racket.getX()) / 20;
                			dy = (ball.getY() - racket.getY()) / 20;
                		
                		// fell from handle
                		} else if (hitHandle.contains(ball.getX(), ball.getY())) {
                			vibrate(40);
                			dx *= 1.5f; 
                			dy *= 1.5f;
                		}
            		} 
            	
            	// fallen on the ground
            	if (r <= rg) {
            		// new hs animation
            		if (score >= b) {
            			//curScoreText.setColor(bColor);
            			//if (score >= s) curScoreText.setColor(sColor);
            			//if (score >= g) curScoreText.setColor(gColor);
            			curScoreText.registerEntityModifier(
            					new ParallelEntityModifier(
            							new SequenceEntityModifier(
            									new MoveModifier(0.5f, curScoreText.getX(), curScoreText.getX() + 300, curScoreText.getY(), curScoreText.getY()), 
            									new MoveModifier(0.5f, curScoreText.getX() + 300, curScoreText.getX(), curScoreText.getY(), curScoreText.getY())),
            							new SequenceEntityModifier(new ScaleModifier(0.5f, 1, 3), new ScaleModifier(0.5f, 3, 1)))
            					);
            			}
            		paused = true;
            		}
                }}));
		
		camera.setHUD(createHUD());//reset();
		
		return gameScene;
	}

	
	void vibrate(int v) {
		if (vibro) mEngine.vibrate(v);
	}
	
	int calcScore() {
		if (force < 2) return 0;
		return (int)((force * force * force + (rR - Math.sqrt((ball.getX() - xR) * (ball.getX() - xR) + (ball.getY() - yR) * (ball.getY() - yR))) / 3) / 10);
	}

	@Override
	public boolean onSceneTouchEvent(Scene pScene, TouchEvent te) {
		if (howToEntity.isVisible()) switchHowTo();
		if(te.isActionMove()) {
			//log("!");
			tx = te.getX() - ball.getX();
			ty = te.getY() - ball.getY();
			Vector2 v = new Vector2(tx, ty);
			v.nor();
			tx = v.x * 5;
			ty = v.y * 5;
		} else if (te.isActionUp()) {
			//log("x");
			tx = 0;
			ty = 0;
		}
		return true;
	}

	private void resetGame() {
	
		updateScores();
		
		ball.setPosition(xB, yB);
		r = rb;
		speed = 0;
		score = 0;
		curScoreText.setText("SCORE: " + score);
		curScoreText.setColor(curScoreColor);
		dx = 0; 
		dy = 0;
		camera.setCenterDirect(CW / 2, CH / 2);
		camera.setZoomFactorDirect(1f);
		paused = false;
	}
	
	boolean updateScores() {
		// get
		g = sp.getInt("gold", 0);
		s = sp.getInt("silver", 0);
		b = sp.getInt("bronze", 0);
		ArrayList<Integer> list = new ArrayList<Integer>();
		list.add(g);
		list.add(s);
		list.add(b);
		list.add(score);
		Collections.sort(list);
		list.remove(0);
		g = list.get(2);
		s = list.get(1);
		b = list.get(0);
		
		hsGoldText.setText("1. " + g);
		hsSilverText.setText("2. " + s);
		hsBronzeText.setText("3. " + b);
		
		// save
		spe.putInt("gold", g);
		spe.putInt("silver", s);
		spe.putInt("bronze", b);
		return spe.commit();
	}
	
	void log(String s) {
		Log.e("MY", s);
	}

	@Override
	public void onAccelerationAccuracyChanged(AccelerationData pAccelerationData) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onAccelerationChanged(AccelerationData pAccelerationData) {
		ax = pAccelerationData.getX();
		ay = pAccelerationData.getY();
		az = pAccelerationData.getZ();
		racket2.setPosition(racket.getX() - ax, racket.getY() - ay);
		handle2.setPosition(handle.getX() - ax * 3, handle.getY() - ay);
	}


}
