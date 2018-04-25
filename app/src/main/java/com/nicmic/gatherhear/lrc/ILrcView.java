/**
 * douzifly @Aug 10, 2013
 * github.com/douzifly
 * douzifly@gmail.com
 */
package com.nicmic.gatherhear.lrc;

import java.util.List;

/**
 * use ILrcView to display lyric, seek and scale.
 * @author douzifly
 *
 */
public interface ILrcView {

    /**
     * set the lyric rows to display
     */
    void setLrc(List<LrcRow> lrcRows);

    /**
     * seek lyric row to special time
     * @time time to be seek
     *
     */
    List<LrcRow> seekLrcToTime(long time);

    void setListener(LrcViewListener l);

    public static interface LrcViewListener {

        /**
         * when lyric line was seeked by user
         */
        void onLrcSeeked(int newPosition, LrcRow row);
    }
}
