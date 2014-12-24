package com.example.guaguacardpoj;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap.Config;
import android.graphics.*;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;

public class GuaGuaView extends View {

	private int width;
	private int height;

	private GuaComplieListener listener;
	// ָ��ֵ
	private int mCompleteSize;
	// �Ƿ��Ѿ�����(����ָ��ֵ)
	// ע����߳�ͬ������
	private volatile boolean isComplete;
	// ���ֲ㻭��
	private Canvas mCanvas;
	private Path mPath;
	// ���ڻ��ƹεĻ���
	private Paint mGuaPaint;
	// �ҵĿ��
	private int mGuaSize;

	// ���ֲ�ͼƬ��ԴID
	private int mLayerPicId;
	// ��ͼͼƬ��ԴID
	private int mBgPicId;

	// ���ڻ������ֲ�Ļ���
	private Paint mLayerPaint;

	// ���ڻ������ֵĻ���
	private Paint mTextPaint;
	// �ײ�����
	private String mText;
	// ���־��ο�
	private Rect mTestRect;
	// ���ִ�С
	private int mTextSize;
	// ������ɫ
	private int mTextColor;
	// ���ڴ������ֲ㻭����bitmap
	private Bitmap mBitmap;
	// ��ͼ
	private Bitmap mRootBitmap;
	private int mLastX;
	private int mLastY;

	public GuaGuaView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);

		init();
		// initTextPaint();

		TypedArray array = context.getTheme().obtainStyledAttributes(attrs,
				R.styleable.guaguaCard, defStyleAttr, 0);
		int count = array.getIndexCount();
		for (int i = 0; i < count; i++) {
			int attr = array.getIndex(i);
			switch (attr) {
			case R.styleable.guaguaCard_text:
				mText = array.getString(attr);
				break;

			case R.styleable.guaguaCard_textColor:
				mTextColor = array.getColor(attr, 0x000000);
				break;
			case R.styleable.guaguaCard_textSize:
				mTextSize = (int) array.getDimension(attr, TypedValue
						.applyDimension(TypedValue.COMPLEX_UNIT_SP, 22,
								getResources().getDisplayMetrics()));
				break;
			case R.styleable.guaguaCard_guaSize:
				mGuaSize = array.getInt(attr, 20);
				break;
			case R.styleable.guaguaCard_percent:
				mCompleteSize = array.getInt(attr, 70);
				break;
			case R.styleable.guaguaCard_layerPic:
				mLayerPicId = array.getResourceId(attr, 0x00000);
				break;
			case R.styleable.guaguaCard_bgPic:
				mBgPicId = array.getResourceId(attr, 0x00000);
				break;

			default:
				break;
			}
			array.recycle();
			initGuaPaint();
			initTextPaint();

			Log.w("GUA", "" + mCompleteSize);
		}

	}

	public GuaGuaView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public GuaGuaView(Context context) {
		this(context, null);
	}

	// ��ʼ��Ĭ��ֵ
	private void init() {

		mCompleteSize = 70;
		mGuaSize = 20;
		mText = "";
		mTextColor = Color.BLACK;
		mTextSize = 22;

	}

	private void initGuaPaint() {
		mLastX = 0;
		mLastY = 0;

		mGuaPaint = new Paint();
		mPath = new Path();

		// ��ʼ�����ʲ���
		mGuaPaint.setColor(Color.WHITE);
		// �����
		mGuaPaint.setAntiAlias(true);
		// ����
		mGuaPaint.setDither(true);
		// ����Բ��
		mGuaPaint.setStrokeJoin(Paint.Join.ROUND);
		mGuaPaint.setStrokeCap(Paint.Cap.ROUND);
		mGuaPaint.setStyle(Paint.Style.STROKE);
		mGuaPaint.setStrokeWidth(mGuaSize);
		// ���û���ģʽ,�ص����ֻ���ʧ
		mGuaPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_OUT));

		// ��ʼ����ͼ
		if (mBgPicId != 0x00000) {
			mRootBitmap = BitmapFactory
					.decodeResource(getResources(), mBgPicId);
		}
	}

	private void initTextPaint() {
		// �������ֵ���ɫ
		mTextPaint = new Paint();
		mTextPaint.setStyle(Paint.Style.FILL);
		mTextPaint.setColor(mTextColor);
		mTextPaint.setTextSize(mTextSize);

		mTestRect = new Rect();

		Log.e("GUA", mText);

		// ��ȡ������ռ�ľ��ο����
		mTextPaint.getTextBounds(mText, 0, mText.length(), mTestRect);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

		super.onMeasure(widthMeasureSpec, heightMeasureSpec);

		// ��ȡ������ĳ���
		width = getMeasuredWidth();
		height = getMeasuredHeight();

		// �������ֲ㻭��
		mBitmap = Bitmap.createBitmap(width, height, Config.ARGB_8888);
		mCanvas = new Canvas(mBitmap);
		// �������ֲ���ɫ
		// mCanvas.drawColor(Color.parseColor("#c0c0c0"));
		if (mLayerPicId != 0x00000) {
			mCanvas.drawBitmap(BitmapFactory.decodeResource(getResources(),
					R.drawable.eve_010101), null,
					new Rect(0, 0, width, height), mLayerPaint);
		} else {
			// ��ɫ���ֲ�
			mCanvas.drawColor(Color.parseColor("#c0c0c0"));
		}
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {

		int x = (int) event.getX();
		int y = (int) event.getY();
		// Log.w("x_y", "x:" + x + "_y:" + y);

		int action = event.getAction();
		switch (action) {

		case MotionEvent.ACTION_DOWN:
			// ����״̬
			// Log.e("action", "ACTION_DOWN");
			// ���������߳�

			mLastX = x;
			mLastY = y;
			mPath.moveTo(x, y);
			break;

		case MotionEvent.ACTION_MOVE:
			// Log.e("action", "ACTION_MOVE");

			// ��ָ�϶�
			// �������ֵ
			int dx = Math.abs(x - mLastX);
			int dy = Math.abs(y - mLastY);

			if (dx > 3 || dy > 3) {
				mPath.lineTo(x, y);
			}
			mLastX = x;
			mLastY = y;

			break;

		case MotionEvent.ACTION_UP:
			// �и�����,�᲻�����ͬ������
			// Ŀǰ��׼ȷ(һֱ�ϲ�̧����ָ)
			new Thread(mRunnable).start();
			break;
		default:
			break;

		}
		// ֪ͨ���»���
		invalidate();

		return true;
	}

	@Override
	protected void onDraw(Canvas canvas) {

		// ���Ƶ�ͼ
		if (mBgPicId != 0x00000) {
			canvas.drawBitmap(mRootBitmap, null, new Rect(0, 0, width, height),
					mLayerPaint);
		} else {
			// ��ɫ��ͼ
			canvas.drawColor(Color.WHITE);
		}
		// ��������
		if (mText != "") {
			canvas.drawText(mText, getWidth() / 2 - mTestRect.width() / 2,
					getHeight() / 2 + mTestRect.height() / 2, mTextPaint);
		}

		// ��path����bitmap��
		drawPath();
		// ��ʾbitmap
		if (!isComplete) {
			canvas.drawBitmap(mBitmap, 0, 0, null);
		} else {
			listener.onComplied();
		}

	}

	private void drawPath() {
		mCanvas.drawPath(mPath, mGuaPaint);
	}

	// �������߳�,���ڼ���γ��� �ٷֱ�
	private Runnable mRunnable = new Runnable() {

		private int[] mPixels;

		@Override
		public void run() {
			Log.w("TAG", "start");

			int w = getWidth();
			int h = getHeight();

			mPixels = new int[w * h];
			float totalArea = w * h;
			float guaArea = 0;

			Bitmap bitmap = mBitmap;
			// ��ȡ��������
			bitmap.getPixels(mPixels, 0, w, 0, 0, w, h);
			// ������������
			// ͳ�Ʊ���������򣨱����������Ϊ0��
			for (int i = 0; i < w; i++) {
				for (int j = 0; j < h; j++) {
					int index = i + j * w;
					if (mPixels[index] == 0) {
						guaArea++;
					}
				}
			}

			if (guaArea > 0 && totalArea > 0) {
				int percent = (int) (guaArea * 100 / totalArea);
				Log.e("TAG", percent + "");

				if (percent > mCompleteSize) {
					isComplete = true;
					// ���̸߳���View
					postInvalidate();
				}
			}
		}
	};

	public void setOnCompliedListener(GuaComplieListener listener) {
		this.listener = listener;
	}

	// �ص��ӿ�
	public interface GuaComplieListener {
		public void onComplied();
	}

}
