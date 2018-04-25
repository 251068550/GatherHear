package com.nicmic.gatherhear.lrc;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.PointF;
import android.media.ThumbnailUtils;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.nicmic.gatherhear.R;
import com.nicmic.gatherhear.utils.LrcUtils;

/**
 * LrcView can display LRC file and Seek it.
 * @author douzifly
 *
 */
public class LrcView extends View implements ILrcView{

	public final static String TAG = "LrcView";

	/** normal display mode*/
	public final static int DISPLAY_MODE_NORMAL = 0;
	/** seek display mode */
	public final static int DISPLAY_MODE_SEEK = 1;
	/** scale display mode ,scale font size*/
	public final static int DISPLAY_MODE_SCALE = 2;

	private List<LrcRow> mLrcRows; 	// all lrc rows of one lrc file
	private int mMinSeekFiredOffset = 10; // min offset for fire seek action, px;
	private int mHignlightRow = 0;   // current singing row , should be highlighted.
	public static int mHignlightRowColor = Color.YELLOW;
	private int mNormalRowColor = Color.WHITE;
	private int mSeekLineColor = Color.WHITE;
	private int mSeekLineTextColor = Color.WHITE;
	private int mSeekLineTextSize = 23;
	private int mMinSeekLineTextSize = 13;
	private int mMaxSeekLineTextSize = 18;
	public static int mLrcFontSize = 23; 	// font size of lrc
    private int mLrcAddFontSize = 5;
	private int mMinLrcFontSize = 15;
	private int mMaxLrcFontSize = 35;
	private int mPaddingY = 30;		// padding of each row
	private int mSeekLinePaddingX = 100; // Seek line padding x
	//播放按钮相关(播放按钮点击事件的范围)
	private int mStartX = 0;
	private float mStartY = 0;
	private int mEndX = 0;
	private float mEndY = 0;
	//歌词正在播放的时间
	private long timePassed = 0;

	private int mDisplayMode = DISPLAY_MODE_NORMAL;
	private LrcViewListener mLrcViewListener;

	private String mLoadingLrcTip = "暂无歌词信息";

	private Paint mPaint;

    public final static int SEEK_LRC = 0;
	public final static int CHANGE_MODE_NORMAL = 1;
    public final static int UPDATE_FONT_SIZE = 2;
    public final static int UPDATE_FONT_COLOR = 3;
    public static Handler staticHandler;
    Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case SEEK_LRC:
                    int mHignlightRow = msg.arg1;
                    seekLrc(mHignlightRow, true);
                    invalidate();
					mDisplayMode = DISPLAY_MODE_NORMAL;
                    break;
				case CHANGE_MODE_NORMAL:
					mDisplayMode = DISPLAY_MODE_NORMAL;
					break;
                case UPDATE_FONT_SIZE:
                    int size = msg.arg1;
                    mLrcFontSize = size;
                    break;
                case UPDATE_FONT_COLOR:
                    int color = msg.arg1;
                    mHignlightRowColor = color;
                    break;
            }
        }
    };

	public LrcView(Context context,AttributeSet attr){
		super(context,attr);
		mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		mPaint.setTextSize(mLrcFontSize);

        staticHandler = mHandler;
		//加载歌词参数
		mLrcFontSize = LrcUtils.getFontSize();
		mHignlightRowColor = LrcUtils.getFontColor();
	}

	public void setListener(LrcViewListener l){
		mLrcViewListener = l;
	}

	public void setLoadingTipText(String text){
		mLoadingLrcTip = text;
	}

	@Override
	protected void onDraw(Canvas canvas) {
		final int height = getHeight(); // height of this view
		final int width = getWidth() ; // width of this view
		if(mLrcRows == null || mLrcRows.size() == 0){
			if(mLoadingLrcTip != null){
				// draw tip when no lrc.
				mPaint.setColor(mHignlightRowColor);
				mPaint.setTextSize(mLrcFontSize);
				mPaint.setTextAlign(Align.CENTER);
				canvas.drawText(mLoadingLrcTip, width / 2, height / 2 - mLrcFontSize, mPaint);
			}
			return;
		}

		float rowY = 0; // vertical point of each row.
		final int rowX = width / 2;
		int rowNum = 0;

		// 1, draw highlight row at center.
		// 2, draw rows above highlight row.
		// 3, draw rows below highlight row.

//		long time = mLrcRows.get(mHignlightRow).time;
//		Log.e("LrcView", timePassed + " -- " + time);
//		long offsetY = timePassed - time;

		// 1 highlight row
		String highlightText = mLrcRows.get(mHignlightRow).content;
		float highlightRowY = height / 2;
		mPaint.setColor(mHignlightRowColor);
		mPaint.setTextSize(mLrcFontSize + mLrcAddFontSize);
		mPaint.setTextAlign(Align.CENTER);
		canvas.drawText(highlightText, rowX, highlightRowY, mPaint);

		if(mDisplayMode == DISPLAY_MODE_SEEK){
			// draw Seek line and current time when moving.
			mPaint.setColor(mSeekLineColor);
            //左边横线
            canvas.drawLine(mSeekLinePaddingX, highlightRowY - mLrcAddFontSize,
                    mSeekLinePaddingX * 2, highlightRowY - mLrcAddFontSize, mPaint);
            //右边横线
			canvas.drawLine(width - mSeekLinePaddingX * 2, highlightRowY - mLrcAddFontSize,
					width - mSeekLinePaddingX, highlightRowY - mLrcAddFontSize, mPaint);
            //左边歌词时间
			mPaint.setColor(mSeekLineTextColor);
			mPaint.setTextSize(mSeekLineTextSize);
			mPaint.setTextAlign(Align.LEFT);
			canvas.drawText(mLrcRows.get(mHignlightRow).strTime, 0, highlightRowY, mPaint);
			//右边播放按钮
			Bitmap source = BitmapFactory.decodeResource(getResources(), R.drawable.ic_play_circle_fill_white);
			Bitmap bitmap = ThumbnailUtils.extractThumbnail(source, 48, 48);
			//设置播放按钮点击事件的范围
			mStartX = width - mSeekLinePaddingX / 4 * 3;
			mStartY = highlightRowY - mLrcFontSize - mLrcAddFontSize;
			mEndX = mStartX + bitmap.getWidth();
			mEndY = mStartY + bitmap.getHeight();
			canvas.drawBitmap(bitmap, mStartX, mStartY, mPaint);
		}

		// 2 above rows
		mPaint.setColor(mNormalRowColor);
		mPaint.setTextSize(mLrcFontSize);
		mPaint.setTextAlign(Align.CENTER);
		rowNum = mHignlightRow - 1;
		rowY = highlightRowY - mPaddingY - mLrcFontSize;
		while( rowY > -mLrcFontSize && rowNum >= 0){
			String text = mLrcRows.get(rowNum).content;
			canvas.drawText(text, rowX, rowY, mPaint);
			rowY -=  (mPaddingY + mLrcFontSize);
			rowNum --;
		}

		// 3 below rows
		rowNum = mHignlightRow + 1;
		rowY = highlightRowY + mPaddingY + mLrcFontSize;
		while( rowY < height && rowNum < mLrcRows.size()){
			String text = mLrcRows.get(rowNum).content;
			canvas.drawText(text, rowX, rowY, mPaint);
			rowY += (mPaddingY + mLrcFontSize);
			rowNum ++;
		}
	}

	public void seekLrc(int position, boolean cb) {
	    if(mLrcRows == null || position < 0 || position > mLrcRows.size()) {
	        return;
	    }
		LrcRow lrcRow = mLrcRows.get(position);
		mHignlightRow = position;
		invalidate();
		if(mLrcViewListener != null && cb){
			mLrcViewListener.onLrcSeeked(position, lrcRow);
		}
	}

	private float mLastMotionY;
	private PointF mPointerOneLastMotion = new PointF();
	private PointF mPointerTwoLastMotion = new PointF();
	private boolean mIsFirstMove = false; // whether is first move , some events can't not detected in touch down,
										  // such as two pointer touch, so it's good place to detect it in first move

	@Override
	public boolean onTouchEvent(MotionEvent event) {

		if(mLrcRows == null || mLrcRows.size() == 0){
			return super.onTouchEvent(event);
		}

		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			Log.d(TAG,"down,mLastMotionY:"+mLastMotionY);
			mLastMotionY = event.getY();
			mIsFirstMove = true;
			invalidate();

			//判断是否有3秒后CHANGE_MODE_NORMAL的状态，有的话取消，否则可能一按下歌词就定位到播放位置
			if (mHandler.obtainMessage(CHANGE_MODE_NORMAL) != null) {
				mHandler.removeMessages(CHANGE_MODE_NORMAL);
			}
			//判断是否处于SEEK状态并按下了播放按钮
			if (mDisplayMode == DISPLAY_MODE_SEEK) {
				if (event.getX() > mStartX && event.getX() < mEndX
						&& event.getY() > mStartY && event.getY() < mEndY) {
					Message msg = Message.obtain();
					msg.what = SEEK_LRC;
					msg.arg1 = mHignlightRow;
					mHandler.sendMessage(msg);
				}
			}
			break;
		case MotionEvent.ACTION_MOVE:

			if(event.getPointerCount() == 2){
				Log.d(TAG, "two move");
				doScale(event);
				return true;
			}
			Log.d(TAG, "one move");
			// single pointer mode ,seek
			if(mDisplayMode == DISPLAY_MODE_SCALE){
				 //if scaling but pointer become not two ,do nothing.
				return true;
			}

			doSeek(event);
			break;
		case MotionEvent.ACTION_CANCEL:
		case MotionEvent.ACTION_UP:
			if(mDisplayMode == DISPLAY_MODE_SEEK) {
				//显示歌词拖动到的地方3秒，之后改变为正常模式，歌词自动定位到播放的位置
				mHandler.sendEmptyMessageDelayed(CHANGE_MODE_NORMAL, 3000);
			}
			if (mDisplayMode == DISPLAY_MODE_SCALE) {
				mDisplayMode = DISPLAY_MODE_NORMAL;
			}
			break;
		}
		return true;
	}

	private void doScale(MotionEvent event) {
		if(mDisplayMode == DISPLAY_MODE_SEEK){
			// if Seeking but pointer become two, become to scale mode
			mDisplayMode = DISPLAY_MODE_SCALE;
			Log.d(TAG, "two move but teaking ...change mode");
			return;
		}
		// two pointer mode , scale font
		if(mIsFirstMove){
			mDisplayMode = DISPLAY_MODE_SCALE;
			invalidate();
			mIsFirstMove = false;
			setTwoPointerLocation(event);
		}
		int scaleSize = getScale(event);
		Log.d(TAG,"scaleSize:" + scaleSize);
		if(scaleSize != 0){
			setNewFontSize(scaleSize);
			invalidate();
		}
		setTwoPointerLocation(event);
	}

	private void doSeek(MotionEvent event) {
		float y = event.getY();
		float offsetY = y - mLastMotionY; // touch offset.
		if(Math.abs(offsetY) < mMinSeekFiredOffset){
			// move to short ,do not fire seek action
			return;
		}
		mDisplayMode = DISPLAY_MODE_SEEK;
		int rowOffset = Math.abs((int) offsetY / mLrcFontSize); // highlight row offset.
		Log.d(TAG, "move new hightlightrow : " + mHignlightRow + " offsetY: " + offsetY + " rowOffset:" + rowOffset);
		if(offsetY < 0){
			// finger move up
			mHignlightRow += rowOffset;
		}else if(offsetY > 0){
			// finger move down
			mHignlightRow -= rowOffset;
		}
		mHignlightRow = Math.max(0, mHignlightRow);
		mHignlightRow = Math.min(mHignlightRow, mLrcRows.size() - 1);

		if(rowOffset > 0){
			mLastMotionY = y;
			invalidate();
		}
	}

	private void setTwoPointerLocation(MotionEvent event) {
		mPointerOneLastMotion.x = event.getX(0);
		mPointerOneLastMotion.y = event.getY(0);
		mPointerTwoLastMotion.x = event.getX(1);
		mPointerTwoLastMotion.y = event.getY(1);
	}

	private void setNewFontSize(int scaleSize){
		mLrcFontSize += scaleSize;
		mSeekLineTextSize += scaleSize;
		mLrcFontSize = Math.max(mLrcFontSize, mMinLrcFontSize);
		mLrcFontSize = Math.min(mLrcFontSize, mMaxLrcFontSize);
		mSeekLineTextSize = Math.max(mSeekLineTextSize, mMinSeekLineTextSize);
		mSeekLineTextSize = Math.min(mSeekLineTextSize, mMaxSeekLineTextSize);
	}

	// get font scale offset
	private int getScale(MotionEvent event){
		Log.d(TAG,"scaleSize getScale");
		float x0 = event.getX(0);
		float y0 = event.getY(0);
		float x1 = event.getX(1);
		float y1 = event.getY(1);
		float maxOffset =  0; // max offset between x or y axis,used to decide scale size

		boolean zoomin = false;

		float oldXOffset = Math.abs(mPointerOneLastMotion.x - mPointerTwoLastMotion.x);
		float newXoffset = Math.abs(x1 - x0);

		float oldYOffset = Math.abs(mPointerOneLastMotion.y - mPointerTwoLastMotion.y);
		float newYoffset = Math.abs(y1 - y0);

		maxOffset = Math.max(Math.abs(newXoffset - oldXOffset), Math.abs(newYoffset - oldYOffset));
		if(maxOffset == Math.abs(newXoffset - oldXOffset)){
			zoomin = newXoffset > oldXOffset ? true : false;
		}else{
			zoomin = newYoffset > oldYOffset ? true : false;
		}

		Log.d(TAG,"scaleSize maxOffset:" + maxOffset);

		if(zoomin)
			return (int)(maxOffset / 10);
		else
			return -(int)(maxOffset / 10);
	}

    public void setLrc(List<LrcRow> lrcRows) {
		timePassed = 0;
        mLrcRows = lrcRows;
		mHignlightRow = 0;//这句话防止前一首歌词还在重绘时，这时候设置了新的歌词，可能会导致数组越界
        invalidate();
    }

    public List<LrcRow> seekLrcToTime(long time) {
        if(mLrcRows == null || mLrcRows.size() == 0) {
            return null;
        }

        if(mDisplayMode != DISPLAY_MODE_NORMAL) {
            // touching
            return null;
        }
		timePassed = time;
        Log.d(TAG, "seekLrcToTime:" + time);
        // find row
        for(int i = 0; i < mLrcRows.size(); i++) {
            LrcRow current = mLrcRows.get(i);
            LrcRow next = i + 1 == mLrcRows.size() ? null : mLrcRows.get(i + 1);

            if((time >= current.time && next != null && time < next.time)
                    || (time > current.time && next == null)) {
                seekLrc(i, false);

				//返回高亮的歌词和下一句歌词（用户更新MainFragment的歌词界面）
				List<LrcRow> lrcRows = new ArrayList<>();
				current.setIndex(i);
				lrcRows.add(current);
				lrcRows.add(next);
				return lrcRows;
            }
        }
		return null;
    }
}
