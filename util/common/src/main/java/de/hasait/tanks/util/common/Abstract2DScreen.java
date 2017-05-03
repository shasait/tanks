/*
 * Copyright (C) 2017 by Sebastian Hasait (sebastian at hasait dot de)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.hasait.tanks.util.common;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.Scaling;
import com.badlogic.gdx.utils.TimeUtils;
import com.badlogic.gdx.utils.viewport.ScalingViewport;

/**
 *
 */
public abstract class Abstract2DScreen<C extends AbstractScreenContext> implements Screen {

	private final C _context;

	private final int _viewportW, _viewportH;
	private final Rectangle _viewportR;


	private final List<Disposable> _disposables = new ArrayList<>();

	private final OrthographicCamera _camera;

	private final InputMultiplexer _inputMultiplexer = new InputMultiplexer();

	private final Stage _stage;

	private boolean _visible;
	private Color _backgroundColor;
	private Music _backgroundMusic;
	private boolean _backgroundMusicPlaying = true;
	private float _textMargin;
	private int _textLine;
	private Skin _skin;
	private long _timeMillis;

	protected Abstract2DScreen(final C pContext, final int pViewportW, final int pViewportH) {
		super();

		_context = pContext;

		_viewportW = pViewportW;
		_viewportH = pViewportH;
		_viewportR = new Rectangle(0, 0, _viewportW, _viewportH);

		_camera = new OrthographicCamera();
		_backgroundColor = Color.BLACK;
		_stage = new Stage(new ScalingViewport(Scaling.fit, _viewportW, _viewportH, _camera), _context.getBatch());
		_inputMultiplexer.addProcessor(_stage);
	}

	@Override
	public void dispose() {
		_disposables.forEach(Disposable::dispose);
		_disposables.clear();
	}

	public final C getContext() {
		return _context;
	}

	public final int getViewportH() {
		return _viewportH;
	}

	public final int getViewportW() {
		return _viewportW;
	}

	@Override
	public final void hide() {
		_visible = false;
		Gdx.input.setInputProcessor(null);
		if (_backgroundMusic != null) {
			_backgroundMusic.stop();
		}
	}

	public final boolean isVisible() {
		return _visible;
	}

	@Override
	public final void pause() {

	}

	@Override
	public final void render(final float pDelta) {
		_timeMillis = TimeUtils.millis();
		Gdx.gl.glClearColor(_backgroundColor.r, _backgroundColor.g, _backgroundColor.b, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		_stage.act(pDelta);
		_stage.draw();
		_camera.update();
		final SpriteBatch batch = _context.getBatch();
		batch.setProjectionMatrix(_camera.combined);
		batch.begin();
		_textLine = 0;
		renderInternal(pDelta);
		batch.end();
	}

	@Override
	public final void resize(final int pWidth, final int pHeight) {
		_stage.getViewport().update(pWidth, pHeight, true);
	}

	@Override
	public final void resume() {

	}

	@Override
	public final void show() {
		_visible = true;
		Gdx.input.setInputProcessor(_inputMultiplexer);
		if (_backgroundMusic != null) {
			_backgroundMusic.play();
		}
	}

	public final boolean viewportContains(final float pX, final float pY) {
		return _viewportR.contains(pX, pY);
	}

	protected final void addDisposable(final Disposable pDisposable) {
		_disposables.add(pDisposable);
	}

	protected final void addInputProcessor(final InputProcessor pInputProcessor) {
		_inputMultiplexer.addProcessor(pInputProcessor);
	}

	protected final Table addLayout() {
		final Table layout = new Table(getSkin());
		_stage.addActor(layout);
		return layout;
	}

	protected final Label createLabel(final String pText) {
		return createLabel(pText, 1.0f);
	}

	protected final Label createLabel(final String pText, final float pScale) {
		final Label label = new Label(pText, getSkin());
		label.setFontScale(pScale);
		return label;
	}

	protected final TextButton createTextButton(final String pText) {
		final TextButton textButton = new TextButton(pText, getSkin());
		return textButton;
	}

	protected final TextField createTextField() {
		return createTextField(Util.EMPTY);
	}

	protected final TextField createTextField(final String pText) {
		final TextField textField = new TextField(pText, getSkin());
		return textField;
	}

	protected final void drawText(final CharSequence pText) {
		final BitmapFont font = _context.getFont();
		font.draw(_context.getBatch(), pText, _textMargin, _viewportH - _textMargin - _textLine * font.getLineHeight());
		_textLine++;
	}

	protected final void drawText(final int pLine, final CharSequence pText) {
		drawText(pLine, pText, AlignH.CENTER);
	}

	protected final void drawText(final int pLine, final CharSequence pText, final AlignH pAlignH) {
		drawText(pLine, pText, pAlignH, AlignV.CENTER);
	}

	protected final void drawText(final int pLine, final CharSequence pText, final AlignH pAlignH, final AlignV pAlignV) {
		final BitmapFont font = _context.getFont();
		final float y = pAlignV.getY(_viewportH, font.getLineHeight(), _textMargin) + pLine * font.getLineHeight();
		final int alignH = pAlignH.getAlign();
		font.draw(_context.getBatch(), pText, _textMargin, y, _viewportW - 2 * _textMargin, alignH, false);
	}

	protected final void drawTexture(final Texture pTexture, final float pCX, final float pCY, final int pW, final int pH, final float pR) {
		_context.getBatch().draw(pTexture, pCX - pW / 2, pCY - pH / 2, pW / 2, pH / 2, pW, pH, 1, 1, pR, 0, 0, pTexture.getWidth(),
								 pTexture.getHeight(), false, false
		);
	}

	protected final Color getBackgroundColor() {
		return _backgroundColor;
	}

	protected final Music getBackgroundMusic() {
		return _backgroundMusic;
	}

	protected final float getTextMargin() {
		return _textMargin;
	}

	protected final long getTimeMillis() {
		return _timeMillis;
	}

	protected final Vector2 getTouchPosition() {
		if (Gdx.input.isTouched()) {
			final Vector3 touchPos = new Vector3();
			touchPos.set(Gdx.input.getX(), Gdx.input.getY(), 0);
			_camera.unproject(touchPos);
			return new Vector2(touchPos.x, touchPos.y);
		}
		return null;
	}

	protected final boolean isBackgroundMusicPlaying() {
		return _backgroundMusicPlaying;
	}

	protected final void removeInputProcessor(final InputProcessor pInputProcessor) {
		_inputMultiplexer.removeProcessor(pInputProcessor);
	}

	protected abstract void renderInternal(final float pDelta);

	protected final void setBackgroundColor(final Color pBackgroundColor) {
		_backgroundColor = pBackgroundColor;
	}

	protected final void setBackgroundMusic(final String pBackgroundMusicFile) {
		final Music music = Gdx.audio.newMusic(Gdx.files.internal(pBackgroundMusicFile));
		addDisposable(music);
		setBackgroundMusic(music);
	}

	protected final void setBackgroundMusic(final Music pBackgroundMusic) {
		if (_backgroundMusic == pBackgroundMusic) {
			return;
		}
		if (_backgroundMusic != null) {
			_backgroundMusic.stop();
		}
		_backgroundMusic = pBackgroundMusic;
		if (_backgroundMusic != null) {
			_backgroundMusic.setLooping(true);
			if (_visible && _backgroundMusicPlaying) {
				_backgroundMusic.play();
			}
		}
	}

	protected final void setScreen(final Screen pScreen) {
		Objects.requireNonNull(pScreen);

		_context.getGame().setScreen(pScreen);
	}

	protected final void setTextMargin(final float pTextMargin) {
		_textMargin = pTextMargin;
	}

	protected final void toggleBackgroundMusic() {
		_backgroundMusicPlaying = !_backgroundMusicPlaying;
		if (_backgroundMusic != null) {
			if (_backgroundMusicPlaying) {
				_backgroundMusic.play();
			} else {
				_backgroundMusic.stop();
			}
		}
	}

	private Skin getSkin() {
		if (_skin == null) {
			_skin = new Skin(Gdx.files.classpath("uiskin.json"));
			addDisposable(_skin);
		}
		return _skin;
	}

	public enum AlignH {
		LEFT(Align.left),
		CENTER(Align.center),
		RIGHT(Align.right);

		private final int _align;

		AlignH(final int pAlign) {
			_align = pAlign;
		}

		public int getAlign() {
			return _align;
		}
	}

	public enum AlignV {
		TOP(0, 0, 1),
		CENTER(0.5f, 0, 0),
		BOTTOM(1, -1, -1);

		private final float _vhFac, _lhFac, _mFac;

		AlignV(final float pVhFac, final float pLhFac, final float pMFac) {
			_vhFac = pVhFac;
			_lhFac = pLhFac;
			_mFac = pMFac;
		}

		public float getY(final float pViewportH, final float pLineH, final float pMargin) {
			return pViewportH * _vhFac + pLineH * _lhFac + pMargin * _mFac;
		}
	}

}
