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
	// 指定值
	private int mCompleteSize;
	// 是否已经刮完(超出指定值)
	// 注意多线程同步问题
	private volatile boolean isComplete;
	// 遮罩层画板
	private Canvas mCanvas;
	private Path mPath;
	// 用于绘制刮的画笔
	private Paint mGuaPaint;
	// 挂的宽度
	private int mGuaSize;

	// 遮罩层图片资源ID
	private int mLayerPicId;
	// 底图图片资源ID
	private int mBgPicId;

	// 用于绘制遮罩层的画笔
	private Paint mLayerPaint;

	// 用于绘制文字的画笔
	private Paint mTextPaint;
	// 底层文字
	private String mText;
	// 文字矩形框
	private Rect mTestRect;
	// 文字大小
	private int mTextSize;
	// 文字颜色
	private int mTextColor;
	// 用于创建遮罩层画布的bitmap
	private Bitmap mBitmap;
	// 底图
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

	// 初始化默认值
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

		// 初始化画笔参数
		mGuaPaint.setColor(Color.WHITE);
		// 防锯齿
		mGuaPaint.setAntiAlias(true);
		// 防震抖
		mGuaPaint.setDither(true);
		// 设置圆角
		mGuaPaint.setStrokeJoin(Paint.Join.ROUND);
		mGuaPaint.setStrokeCap(Paint.Cap.ROUND);
		mGuaPaint.setStyle(Paint.Style.STROKE);
		mGuaPaint.setStrokeWidth(mGuaSize);
		// 设置绘制模式,重叠部分会消失
		mGuaPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_OUT));

		// 初始化底图
		if (mBgPicId != 0x00000) {
			mRootBitmap = BitmapFactory
					.decodeResource(getResources(), mBgPicId);
		}
	}

	private void initTextPaint() {
		// 设置文字的颜色
		mTextPaint = new Paint();
		mTextPaint.setStyle(Paint.Style.FILL);
		mTextPaint.setColor(mTextColor);
		mTextPaint.setTextSize(mTextSize);

		mTestRect = new Rect();

		Log.e("GUA", mText);

		// 获取文字所占的矩形框面积
		mTextPaint.getTextBounds(mText, 0, mText.length(), mTestRect);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

		super.onMeasure(widthMeasureSpec, heightMeasureSpec);

		// 获取测量后的长宽
		width = getMeasuredWidth();
		height = getMeasuredHeight();

		// 创建遮罩层画布
		mBitmap = Bitmap.createBitmap(width, height, Config.ARGB_8888);
		mCanvas = new Canvas(mBitmap);
		// 设置遮罩层颜色
		// mCanvas.drawColor(Color.parseColor("#c0c0c0"));
		if (mLayerPicId != 0x00000) {
			mCanvas.drawBitmap(BitmapFactory.decodeResource(getResources(),
					R.drawable.eve_010101), null,
					new Rect(0, 0, width, height), mLayerPaint);
		} else {
			// 灰色遮罩层
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
			// 按下状态
			// Log.e("action", "ACTION_DOWN");
			// 启动计算线程

			mLastX = x;
			mLastY = y;
			mPath.moveTo(x, y);
			break;

		case MotionEvent.ACTION_MOVE:
			// Log.e("action", "ACTION_MOVE");

			// 手指拖动
			// 计算绝对值
			int dx = Math.abs(x - mLastX);
			int dy = Math.abs(y - mLastY);

			if (dx > 3 || dy > 3) {
				mPath.lineTo(x, y);
			}
			mLastX = x;
			mLastY = y;

			break;

		case MotionEvent.ACTION_UP:
			// 有个问题,会不会出现同步问题
			// 目前不准确(一直拖不抬起手指)
			new Thread(mRunnable).start();
			break;
		default:
			break;

		}
		// 通知更新画布
		invalidate();

		return true;
	}

	@Override
	protected void onDraw(Canvas canvas) {

		// 绘制底图
		if (mBgPicId != 0x00000) {
			canvas.drawBitmap(mRootBitmap, null, new Rect(0, 0, width, height),
					mLayerPaint);
		} else {
			// 白色底图
			canvas.drawColor(Color.WHITE);
		}
		// 绘制文字
		if (mText != "") {
			canvas.drawText(mText, getWidth() / 2 - mTestRect.width() / 2,
					getHeight() / 2 + mTestRect.height() / 2, mTextPaint);
		}

		// 把path画在bitmap上
		drawPath();
		// 显示bitmap
		if (!isComplete) {
			canvas.drawBitmap(mBitmap, 0, 0, null);
		} else {
			listener.onComplied();
		}

	}

	private void drawPath() {
		mCanvas.drawPath(mPath, mGuaPaint);
	}

	// 创建子线程,用于计算刮除的 百分比
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
			// 获取所有像素
			bitmap.getPixels(mPixels, 0, w, 0, 0, w, h);
			// 遍历所有像素
			// 统计被清除的区域（被清除的像素为0）
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
					// 子线程更新View
					postInvalidate();
				}
			}
		}
	};

	public void setOnCompliedListener(GuaComplieListener listener) {
		this.listener = listener;
	}

	// 回调接口
	public interface GuaComplieListener {
		public void onComplied();
	}

}
