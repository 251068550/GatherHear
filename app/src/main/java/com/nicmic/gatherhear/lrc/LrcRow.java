/**
 * douzifly @Aug 10, 2013
 * github.com/douzifly
 * douzifly@gmail.com
 */
package com.nicmic.gatherhear.lrc;

import java.util.ArrayList;
import java.util.List;

import android.util.Log;

/**
 * 
 * describe the lyric line
 * @author douzifly
 *
 */
public class LrcRow implements Comparable<LrcRow>{
    public final static String TAG = "LrcRow";
    
    /** begin time of this lrc row */
    public long time;
    /** content of this lrc */
    public String content;
    
    public String strTime;
    /** 歌词在歌词集合中的位置 */
    public int index;
    
    public LrcRow(){}
    
    public LrcRow(String strTime,long time,String content){
        this.strTime = strTime;
        this.time = time;
        this.content = content;
        Log.d(TAG,"strTime:" + strTime + " time:" + time + " content:" + content);
    }
    
    /**
     *  create LrcRows by standard Lrc Line , if not standard lrc line,
     *  return false<br />
     *  [00:00:20] balabalabalabala
     */
    public static List<LrcRow> createRows(String standardLrcLine, long offset){

        try{
            //判断offset
//            if (standardLrcLine.substring(1, 7).equals("offset")){
//                String offsetStr = standardLrcLine.substring(8, standardLrcLine.lastIndexOf("]"));
//                try {
//                    offset = Long.parseLong(offsetStr);
//                } catch (Exception e) {
//                    offset = 0;
//                    e.printStackTrace();
//                }
//                return null;
//            }

            if(standardLrcLine.indexOf("[") != 0 || standardLrcLine.indexOf("]") != 9 ){
                return null;
            }
            int lastIndexOfRightBracket = standardLrcLine.lastIndexOf("]");
            String content = standardLrcLine.substring(lastIndexOfRightBracket + 1, standardLrcLine.length());
            //把没有歌词内容的行去掉
            if (content.equals("")) {
                return null;
            }
            
            // times [mm:ss.SS][mm:ss.SS] -> *mm:ss.SS**mm:ss.SS*
            String times = standardLrcLine.substring(0,lastIndexOfRightBracket + 1).replace("[", "-").replace("]", "-");
            String arrTimes[] = times.split("-");
            List<LrcRow> listTimes = new ArrayList<LrcRow>();
            for(String temp : arrTimes){
                if(temp.trim().length() == 0){
                    continue;
                }
                LrcRow lrcRow = new LrcRow(temp, timeConvert(temp, offset), content);
                listTimes.add(lrcRow);
            }
            return listTimes;
        }catch(Exception e){
            Log.e(TAG,"createRows exception:" + e.getMessage());
            return null;
        }
    }
    
    private static long timeConvert(String timeString, long offset){
        timeString = timeString.replace('.', ':');
        String[] times = timeString.split(":");
        // mm:ss:SS
        long time = Integer.valueOf(times[0]) * 60 * 1000 +
                Integer.valueOf(times[1]) * 1000 +
                Integer.valueOf(times[2]) + offset;
        if (time < 0) {
            time = 0;
        }
        return time;
    }

    public int compareTo(LrcRow another) {
        return (int)(this.time - another.time);
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }
}