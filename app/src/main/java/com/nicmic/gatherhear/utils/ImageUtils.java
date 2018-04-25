package com.nicmic.gatherhear.utils;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.net.Uri;
import android.os.ParcelFileDescriptor;

import com.nicmic.gatherhear.App;
import com.nicmic.gatherhear.R;

import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

/**
 * liteplayer by loader
 * @author qibin
 */
public class ImageUtils {
	/**
	 * 缩放图片
	 * @param bmp
	 * @return
	 */
	public static Bitmap scaleBitmap(Bitmap bmp) {
		return scaleBitmap(bmp, (int) (App.sScreenWidth * 0.13));
	}
	
	/**
	 * 缩放图片
	 * @param bmp
	 * @param size
	 * @return
	 */
	public static Bitmap scaleBitmap(Bitmap bmp, int size) {
		return Bitmap.createScaledBitmap(bmp, size, size, true);
	}
	
	/**
	 * 根据文件uri缩放图片
	 * @param uri
	 * @return
	 */
	public static Bitmap scaleBitmap(String uri, int size) {
		return scaleBitmap(BitmapFactory.decodeFile(uri), size);
	}
	
	/**
	 * 根据文件uri缩放图片
	 * @param uri
	 * @return
	 */
	public static Bitmap scaleBitmap(String uri) {
		return scaleBitmap(BitmapFactory.decodeFile(uri));
	}
	
	/**
	 * 缩放资源图片
	 * @param res
	 * @return
	 */
	public static Bitmap scaleBitmap(int res) {
		return scaleBitmap(BitmapFactory.decodeResource(App.sContext.getResources(), res));
	}
	
	/**
	 * 创建圆形图片
	 * @deprecated
	 * @param src
	 * @return
	 */
	public static Bitmap createCircleBitmap(Bitmap src) {
		int size = (int) (App.sScreenWidth * 0.13);
		
		Paint paint = new Paint();
		paint.setAntiAlias(true);
		paint.setARGB(255, 241, 239, 229);
		
		Bitmap target = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(target);
		canvas.drawCircle(size / 2, size / 2, size / 2, paint);
		
		paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_ATOP));
		canvas.drawBitmap(src, 0, 0, paint);
		
		return target;
	}
	
	/**
	 * @deprecated
	 * @param uri
	 * @return
	 */
	public static Bitmap createCircleBitmap(String uri) {
		return createCircleBitmap(BitmapFactory.decodeFile(uri));
	}
	
	/**
	 * @deprecated
	 * @param res
	 * @return
	 */
	public static Bitmap createCircleBitmap(int res) {
		return createCircleBitmap(BitmapFactory.decodeResource(App.sContext.getResources(), res));
	}

	//--------------------------获取音乐内置图片相关begin-------------------------
	private static final Uri sArtworkUri = Uri.parse("content://media/external/audio/albumart");
	private static final BitmapFactory.Options sBitmapOptions = new BitmapFactory.Options();

    /**
     * 获取音乐插图
     * @param context
     * @param song_id
     * @param album_id
     * @param allowdefault
     * @return
     */
	public static Bitmap getArtwork(Context context, long song_id, long album_id,
									boolean allowdefault) {
		if (album_id < 0) {
			if (song_id >= 0) {
				Bitmap bm = getArtworkFromFile(context, song_id, -1);
				if (bm != null) {
					return bm;
				}
			}
			if (allowdefault) {
				return getDefaultArtwork(context);
			}
			return null;
		}
		ContentResolver res = context.getContentResolver();
		Uri uri = ContentUris.withAppendedId(sArtworkUri, album_id);
		if (uri != null) {
			InputStream in = null;
			try {
				in = res.openInputStream(uri);
				Bitmap bmp = BitmapFactory.decodeStream(in, null, sBitmapOptions);
				if (bmp == null) {
					bmp = getDefaultArtwork(context);
				}
				return bmp;
			} catch (FileNotFoundException ex) {
				Bitmap bm = getArtworkFromFile(context, song_id, album_id);
				if (bm != null) {
					if (bm.getConfig() == null) {
						bm = bm.copy(Bitmap.Config.RGB_565, false);
						if (bm == null && allowdefault) {
							return getDefaultArtwork(context);
						}
					}
				} else if (allowdefault) {
					bm = getDefaultArtwork(context);
				}
				return bm;
			} finally {
				try {
					if (in != null) {
						in.close();
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return null;
	}

	private static Bitmap getArtworkFromFile(Context context, long songid, long albumid) {
		Bitmap bm = null;
		if (albumid < 0 && songid < 0) {
			throw new IllegalArgumentException("Must specify an album or a song id");
		}
		try {
			if (albumid < 0) {
				Uri uri = Uri.parse("content://media/external/audio/media/" + songid + "/albumart");
				ParcelFileDescriptor pfd = context.getContentResolver()
						.openFileDescriptor(uri, "r");
				if (pfd != null) {
					FileDescriptor fd = pfd.getFileDescriptor();
					bm = BitmapFactory.decodeFileDescriptor(fd);
				}
			} else {
				Uri uri = ContentUris.withAppendedId(sArtworkUri, albumid);
				ParcelFileDescriptor pfd = context.getContentResolver()
						.openFileDescriptor(uri, "r");
				if (pfd != null) {
					FileDescriptor fd = pfd.getFileDescriptor();
					bm = BitmapFactory.decodeFileDescriptor(fd);
				}
			}
		} catch (FileNotFoundException ex) {

		}
		return bm;
	}

	private static Bitmap getDefaultArtwork(Context context) {
        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inPreferredConfig = Bitmap.Config.RGB_565;
        return BitmapFactory.decodeResource(context.getResources(), R.drawable.default_cd_cover);
    }

    /**
     * 获取音乐插图的Uri
     * @param context
     * @param song_id
     * @param album_id
     * @param allowdefault
     * @return
     */
    public static String getArtworkUri(Context context, long song_id, long album_id,
                                 boolean allowdefault) {
        if (album_id < 0) {
            if (song_id >= 0) {
                String uri = getArtworkUriFromFile(context, song_id, -1);
                if (uri != null) {
                    return uri;
                }
            }
            if (allowdefault) {
                return null;
            }
            return null;
        }
        String uri = "content://media/external/audio/albumart/" + album_id;
        if (uri != null) {
            return uri;
        }
        return null;
    }

    private static String getArtworkUriFromFile(Context context, long songid, long albumid) {
        Bitmap bm = null;
        if (albumid < 0 && songid < 0) {
            throw new IllegalArgumentException("Must specify an album or a song id");
        }
        try {
            if (albumid < 0) {
                return "content://media/external/audio/media/" + songid + "/albumart";
            } else {
                return "content://media/external/audio/albumart/" + albumid;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

	//--------------------------获取音乐内置图片相关end-------------------------
}
