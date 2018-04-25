package com.nicmic.gatherhear.utils;

import android.content.Context;
import android.content.Intent;
import android.os.Message;
import android.view.Gravity;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.nicmic.gatherhear.R;
import com.nicmic.gatherhear.activity.ScanMusicActivity;
import com.nicmic.gatherhear.bean.Music;
import com.nicmic.gatherhear.bean.MusicMenu;
import com.nicmic.gatherhear.bean.PlayList;
import com.nicmic.gatherhear.fragment.ContainerActivity;
import com.nicmic.gatherhear.fragment.LocalMusicFragment;
import com.nicmic.gatherhear.fragment.MusicFragment;
import com.nicmic.gatherhear.fragment.MusicMenuFragment;
import com.nicmic.gatherhear.fragment.SongFragment;
import com.nicmic.gatherhear.service.MusicService;
import com.nicmic.gatherhear.widget.LrcFontDialog;
import com.nicmic.gatherhear.widget.MusicInfoDialog;
import com.flyco.animation.BaseAnimatorSet;
import com.flyco.dialog.listener.OnOperItemClickL;
import com.flyco.dialog.widget.NormalListDialog;

import java.util.ArrayList;
import java.util.List;

import cn.pedant.SweetAlert.OptAnimationLoader;
import cn.pedant.SweetAlert.SweetAlertDialog;
import me.drakeet.materialdialog.MaterialDialog;

/**
 * Created by Administrator on 2015/9/21.
 */
public class Dialogs {

    /**
     * 对话框所操作的UI
     * (LocalMusicFragment，SongFragment，MusicMenuFragment)
     */
    public static int currentUI = -1;

    /**
     * 删除歌曲的对话框
     *
     * @param context
     * @param music
     */
    public static void deleteMusicDialog(final Context context, final Music music, final int position) {
        new SweetAlertDialog(context, SweetAlertDialog.WARNING_TYPE)
                .setTitleText("删除歌曲")
                .setContentText("歌名：" + music.getTitle() + "\n歌手：" + music.getArtist() +
                        "\n(当前操作不会删除本地歌曲文件)")
                .setCancelText("点错了!")
                .setConfirmText("是的!")
                .showCancelButton(true)
                .setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sDialog) {
                        sDialog.cancel();
                    }
                })
                .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sweetAlertDialog) {
                        //删除当前歌曲的操作
                        boolean b = MusicUtils.deleteMusic(context, music);
                        if (b) {
                            //显示删除成功对话框
                            sweetAlertDialog.cancel();
                            new SweetAlertDialog(context, SweetAlertDialog.SUCCESS_TYPE)
                                    .setTitleText("已删除")
                                    .show();
                            //从播放列表中移除该歌曲
                            MusicUtils.removePlayListMusic(context, music);
                            //更新UI
                            if (currentUI == MusicService.UI_LOCALMUSIC_SONG) {
                                Message msg = new Message();
                                msg.what = LocalMusicFragment.RESET_UI;
                                msg.arg1 = position;
                                LocalMusicFragment.staticHandler.sendMessage(msg);
                            }
                            if (currentUI == MusicService.UI_SONG_FRAGMENT) {
                                Message msg = new Message();
                                msg.what = SongFragment.RESET_UI;
                                msg.arg1 = position;
                                SongFragment.staticHandler.sendMessage(msg);
                            }
                            if (currentUI == MusicService.UI_MUSIC_MENU) {
                                Message msg = new Message();
                                msg.what = MusicMenuFragment.RESET_UI;
                                msg.arg1 = position;
                                MusicMenuFragment.staticHandler.sendMessage(msg);
                            }
                        } else {
                            //显示删除失败对话框
                            sweetAlertDialog.cancel();
                            new SweetAlertDialog(context, SweetAlertDialog.ERROR_TYPE)
                                    .setTitleText("删除失败")
                                    .show();
                        }

                    }
                })
                .show();
    }

    /**
     * 将歌单中的歌曲移出歌单的对话框
     *
     * @param context
     * @param musicMenu
     * @param position
     */
    public static void removeMusicDialog(final Context context, final MusicMenu musicMenu, final int position) {
        new SweetAlertDialog(context, SweetAlertDialog.WARNING_TYPE)
                .setTitleText("移出歌曲?")
                .setContentText("歌名：" + musicMenu.getMusics().get(position).getTitle() +
                        "\n歌手：" + musicMenu.getMusics().get(position).getArtist())
                .setCancelText("点错了!")
                .setConfirmText("是的!")
                .showCancelButton(true)
                .setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sDialog) {
                        sDialog.cancel();
                    }
                })
                .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sweetAlertDialog) {
                        sweetAlertDialog.cancel();

                        MusicUtils.removePlayListMusic(context, musicMenu.getMusics().get(position));
                        //将歌单中的歌曲移出
                        MusicUtils.removeMusic(context, musicMenu, position);
                        //更新歌单UI
                        if (currentUI == MusicService.UI_MUSIC_MENU) {
                            Message msg = new Message();
                            msg.what = MusicMenuFragment.RESET_UI;
                            msg.arg1 = position;
                            MusicMenuFragment.staticHandler.sendMessage(msg);
                        }
                    }
                })
                .show();
    }

    /**
     * 歌词字体管理的对话框
     *
     * @param context
     */
    public static void lrcFontDialog(final Context context) {
        //对话框进入动画
        BaseAnimatorSet bas_in = new BaseAnimatorSet() {
            @Override
            public void setAnimation(View view) {
                AnimationSet mModalInAnim = (AnimationSet) OptAnimationLoader.loadAnimation(context, R.anim.modal_in);
                view.setAnimation(mModalInAnim);
            }
        };
        //对话框退出动画
        BaseAnimatorSet bas_out = new BaseAnimatorSet() {
            @Override
            public void setAnimation(View view) {
                Animation mModalOutAnim = (AnimationSet) OptAnimationLoader.loadAnimation(context, R.anim.modal_out);
                view.setAnimation(mModalOutAnim);
            }
        };
        //对话框
        LrcFontDialog dialog = new LrcFontDialog(context);
        dialog.widthScale(0.8f)
                .showAnim(bas_in)
                .dismissAnim(bas_out);
        dialog.show();
    }

    /**
     * 歌曲信息的对话框
     *
     * @param context
     * @param music
     */
    public static void musicInfoDialog(final Context context, Music music) {
        //对话框进入动画
        BaseAnimatorSet bas_in = new BaseAnimatorSet() {
            @Override
            public void setAnimation(View view) {
                AnimationSet mModalInAnim = (AnimationSet) OptAnimationLoader.loadAnimation(context, R.anim.modal_in);
                view.setAnimation(mModalInAnim);
            }
        };
        //对话框退出动画
        BaseAnimatorSet bas_out = new BaseAnimatorSet() {
            @Override
            public void setAnimation(View view) {
                Animation mModalOutAnim = (AnimationSet) OptAnimationLoader.loadAnimation(context, R.anim.modal_out);
                view.setAnimation(mModalOutAnim);
            }
        };
        //对话框
        MusicInfoDialog dialog = new MusicInfoDialog(context, music);
        dialog.widthScale(0.8f)
                .showAnim(bas_in)
                .dismissAnim(bas_out);
        dialog.show();
    }

    /**
     * 修改歌曲信息（歌名和歌手）
     *
     * @param context
     * @param music
     */
    public static void modifyMusicInfoDialog(final Context context, final Music music) {

        final EditText et_title = new EditText(context);
        et_title.setHint("歌名");
        et_title.setText(music.getTitle());
        et_title.setBackgroundResource(R.drawable.bg_rectangle_custom_outline);

        final EditText et_artist = new EditText(context);
        et_artist.setHint("歌手");
        et_artist.setText(music.getArtist());
        et_artist.setBackgroundResource(R.drawable.bg_rectangle_custom_outline);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        et_artist.setLayoutParams(params);
        params.setMargins(0, 0, 0, 20);
        et_title.setLayoutParams(params);

        LinearLayout contentView = new LinearLayout(context);
        contentView.setOrientation(LinearLayout.VERTICAL);
        contentView.addView(et_title);
        contentView.addView(et_artist);

        final MaterialDialog dialog = new MaterialDialog(context);
        dialog.setContentView(contentView)
                .setTitle("修改歌曲信息")
                .setNegativeButton("取消", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                })
                .setPositiveButton("修改", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String title = et_title.getText().toString().trim();
                        String artist = et_artist.getText().toString().trim();
                        if (title.equals("")) {
                            Toast.makeText(context, "请填写歌名", 0).show();
                            return;
                        }
                        music.setTitle(title);
                        music.setArtist(artist);
                        //修改歌曲信息
                        MusicUtils.modefyMusicInfo(context, music);
                        //更新界面
                        MusicService.updateUI();

                        dialog.dismiss();
                    }
                });
        dialog.show();
    }

    /**
     * 加入歌单的对话框
     *
     * @param context
     * @param music
     */
    public static void add2MusicMenuDialog(final Context context, final Music music) {
        //对话框进入动画
        BaseAnimatorSet bas_in = new BaseAnimatorSet() {
            @Override
            public void setAnimation(View view) {
                AnimationSet mModalInAnim = (AnimationSet) OptAnimationLoader.loadAnimation(context, R.anim.modal_in);
                view.setAnimation(mModalInAnim);
            }
        };
        //对话框退出动画
        BaseAnimatorSet bas_out = new BaseAnimatorSet() {
            @Override
            public void setAnimation(View view) {
                Animation mModalOutAnim = (AnimationSet) OptAnimationLoader.loadAnimation(context, R.anim.modal_out);
                view.setAnimation(mModalOutAnim);
            }
        };
        //获取歌单信息
        final List<MusicMenu> musicMenus = MusicUtils.getAllMusicMenu(context);
        //对话框
        final String[] stringItems = new String[musicMenus.size() + 1];
        for (int i = 0; i < stringItems.length; i++) {
            if (i == 0) {
                stringItems[i] = "新建歌单(我不是歌单o(∩_∩)o)";
            } else {
                stringItems[i] = musicMenus.get(i - 1).getTitle();
            }
        }
//        {"新建歌单","我的专辑1","我的专辑2","我的专辑3","我的专辑4"};
        final NormalListDialog dialog = new NormalListDialog(context, stringItems);
        dialog.title("请选择")
                .titleTextSize_SP(18)
                .titleBgColor(context.getResources().getColor(R.color.custom_color))
                .itemPressColor(context.getResources().getColor(R.color.custom_color))//
                .itemTextColor(context.getResources().getColor(R.color.black))//
                .itemTextSize(14)
                .cornerRadius(5)
                .widthScale(0.8f)
                .showAnim(bas_in)
                .dismissAnim(bas_out)
                .show();

        dialog.setOnOperItemClickL(new OnOperItemClickL() {
            @Override
            public void onOperItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    //弹出新建歌单的对话框
                    Dialogs.createNewMusicMenuDialog(context);
                } else {
                    //执行加入歌单的操作
                    boolean b = MusicUtils.addMusic2MusicMenu(context, musicMenus.get(position - 1), music);
                    if (b) {
                        Toast.makeText(context, "歌曲:" + music.getTitle() + "成功加入了歌单<" + musicMenus.get(position - 1).getTitle() + ">", 1).show();
                        //如果是歌单页面操作添加歌单的话，需要重新刷新页面
                        if (MusicService.CURRENT_UI == MusicService.UI_MUSIC_MENU) {
                            MusicMenuFragment.staticHandler.sendEmptyMessage(MusicMenuFragment.UPDATE_UI);
                        }
                    } else {
                        Toast.makeText(context, "当前歌单中已有这首歌曲", 1).show();
                    }
                }

                dialog.dismiss();
            }
        });
    }

    /**
     * 加入我喜欢的，或取消加入我喜欢的
     *
     * @param context
     * @param music
     * @return 加入或取消我喜欢的，修改之后的状态
     */
    public static int add2LikeDialog(Context context, Music music) {
        //需要先判断歌曲是否已经加入喜欢列表
        int flag = music.getMyLike();
        if (flag == 0) {
            //标记为喜欢
            MusicUtils.addRemoveMyLike(context, music);
            new SweetAlertDialog(context, SweetAlertDialog.SUCCESS_TYPE)
                    .setTitleText("喜欢")
                    .setContentText("已成功标记为喜欢")
                    .show();
            return 1;
        } else {
            //取消标记喜欢
            MusicUtils.addRemoveMyLike(context, music);
            new SweetAlertDialog(context, SweetAlertDialog.SUCCESS_TYPE)
                    .setTitleText("取消喜欢")
                    .setContentText("原来你不爱我了(┬＿┬)")
                    .show();
            return 0;
        }

    }

    /**
     * 新建歌单
     *
     * @param context
     */
    public static void createNewMusicMenuDialog(final Context context) {

        final EditText et_title = new EditText(context);
        et_title.setHint("名称(必填)");
        et_title.setBackgroundResource(R.drawable.bg_rectangle_custom_outline);

        final EditText et_desc = new EditText(context);
        et_desc.setHint("描述(选填)");
        et_desc.setLines(4);
        et_desc.setGravity(Gravity.LEFT | Gravity.TOP);
        et_desc.setBackgroundResource(R.drawable.bg_rectangle_custom_outline);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        et_desc.setLayoutParams(params);
        params.setMargins(0, 0, 0, 20);
        et_title.setLayoutParams(params);

        LinearLayout contentView = new LinearLayout(context);
        contentView.setOrientation(LinearLayout.VERTICAL);
        contentView.addView(et_title);
        contentView.addView(et_desc);

        final MaterialDialog dialog = new MaterialDialog(context);
        dialog.setContentView(contentView)
                .setTitle("新建歌单")
                .setNegativeButton("取消", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                })
                .setPositiveButton("确认", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String title = et_title.getText().toString().trim();
                        String desc = et_desc.getText().toString().trim();
                        if (title.equals("")) {
                            Toast.makeText(context, "请填写歌单名称", 0).show();
                            return;
                        }
                        dialog.dismiss();
                        //TODO:新建歌单
                        MusicMenu musicMenu = new MusicMenu(title, desc);
                        MusicUtils.createMusicMenu(context, musicMenu);
                        if (MusicMenuFragment.staticHandler != null) {
                            MusicMenuFragment.staticHandler.sendEmptyMessage(MusicMenuFragment.UPDATE_UI);
                        }
                    }
                });
        dialog.show();
    }

    /**
     * 修改歌单信息（名称和描述）
     *
     * @param context
     * @param musicMenu
     */
    public static void modifyMusicMenuDialog(final Context context, final MusicMenu musicMenu) {

        final EditText et_title = new EditText(context);
        et_title.setHint("歌单名称");
        et_title.setText(musicMenu.getTitle());
        et_title.setBackgroundResource(R.drawable.bg_rectangle_custom_outline);

        final EditText et_desc = new EditText(context);
        et_desc.setHint("歌单描述");
        et_desc.setText(musicMenu.getDesc());
        et_desc.setLines(4);
        et_desc.setGravity(Gravity.LEFT | Gravity.TOP);
        et_desc.setBackgroundResource(R.drawable.bg_rectangle_custom_outline);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        et_desc.setLayoutParams(params);
        params.setMargins(0, 0, 0, 20);
        et_title.setLayoutParams(params);

        LinearLayout contentView = new LinearLayout(context);
        contentView.setOrientation(LinearLayout.VERTICAL);
        contentView.addView(et_title);
        contentView.addView(et_desc);

        final MaterialDialog dialog = new MaterialDialog(context);
        dialog.setContentView(contentView)
                .setTitle("修改歌单信息")
                .setNegativeButton("取消", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                })
                .setPositiveButton("确认", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String title = et_title.getText().toString().trim();
                        String desc = et_desc.getText().toString().trim();
                        if (title.equals("")) {
                            Toast.makeText(context, "请填写歌单名称", 0).show();
                            return;
                        }
                        musicMenu.setTitle(title);
                        musicMenu.setDesc(desc);
                        dialog.dismiss();
                        //TODO:修改歌单信息
                        MusicUtils.modefyMusicMenu(context, musicMenu);
                        if (MusicMenuFragment.staticHandler != null) {
                            MusicMenuFragment.staticHandler.sendEmptyMessage(MusicMenuFragment.UPDATE_UI);
                        }
                    }
                });
        dialog.show();
    }

    /**
     * 删除当前歌单
     *
     * @param context
     * @param musicMenu
     */
    public static void deleteMusicMenu(final Context context, final MusicMenu musicMenu) {

        new SweetAlertDialog(context, SweetAlertDialog.WARNING_TYPE)
                .setTitleText("确定删除歌单?")
                .setContentText("歌单中的歌曲文件不会被删除!")
                .setCancelText("点错了!")
                .setConfirmText("是的!")
                .showCancelButton(true)
                .setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sDialog) {
                        sDialog.cancel();
                    }
                })
                .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sweetAlertDialog) {
                        //显示删除成功对话框
                        sweetAlertDialog.cancel();
                        new SweetAlertDialog(context, SweetAlertDialog.SUCCESS_TYPE)
                                .setTitleText("已删除")
                                .show();

                        //TODO:删除当前歌单的操作
                        MusicUtils.deleteMusicMenu(context, musicMenu);
                        if (MusicMenuFragment.staticHandler != null) {
                            MusicMenuFragment.staticHandler.sendEmptyMessage(MusicMenuFragment.UPDATE_UI);
                        }
                    }
                })
                .show();
    }

    public static void chooseMusicDialog(final Context context, final MusicMenu musicMenu) {
        //获取歌曲数据
        final List<Music> musics = MusicUtils.getMusic(context);
        String[] items = new String[musics.size()];
        for (int i = 0; i < musics.size(); i++) {
            items[i] = musics.get(i).getTitle() + "—" + musics.get(i).getArtist();
        }

        //显示对话框
        new com.afollestad.materialdialogs.MaterialDialog.Builder(context)
                .title("选择歌曲")
                .items(items)
                .itemsCallbackMultiChoice(null, new com.afollestad.materialdialogs.MaterialDialog.ListCallbackMultiChoice() {
                    @Override
                    public boolean onSelection(com.afollestad.materialdialogs.MaterialDialog materialDialog, Integer[] integers, CharSequence[] charSequences) {
                        List<Music> selectedMusics = new ArrayList<Music>();
                        for (int i = 0; i < integers.length; i++) {
                            selectedMusics.add(musics.get(integers[i]));
                        }
                        int[] results = MusicUtils.addMusics2MusicMenu(context, musicMenu, selectedMusics);
                        Toast.makeText(context, "添加了" + results[1] + "首歌曲\n" + "其中有" + results[0] + "首已存在在歌单中", 1).show();

                        if (MusicMenuFragment.staticHandler != null) {
                            MusicMenuFragment.staticHandler.sendEmptyMessage(MusicMenuFragment.FOLD_Card);
                            MusicMenuFragment.staticHandler.sendEmptyMessage(MusicMenuFragment.UPDATE_UI);
                        }

                        return true;
                    }
                })
                .positiveText("添加")
                .negativeText("取消")
                .show();
    }

    public static final int PLAY_LIST_BOTTOM_MUSIC = 0;
    public static final int PLAY_LIST_MUSIC_FRAGMENT = 1;

    /**
     * 清空播放列表
     *
     * @param context
     * @param tag
     */
    public static void clearPlayList(final Context context, final int tag) {
        new SweetAlertDialog(context, SweetAlertDialog.WARNING_TYPE)
                .setTitleText("清空播放列表?")
                .setCancelText("点错了!")
                .setConfirmText("是的!")
                .showCancelButton(true)
                .setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sDialog) {
                        sDialog.cancel();
                    }
                })
                .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sweetAlertDialog) {
                        sweetAlertDialog.cancel();

                        if (PlayList.musics.size() > 0) {
                            //清空播放列表
                            PlayList.clearPlayList(context);
                            //停止播放
                            PlayList.playStatus = MusicService.PLAYING;
                            MusicService.stop();

                        }
                        //更新音乐界面播放列表的UI
                        if (tag == PLAY_LIST_MUSIC_FRAGMENT && MusicFragment.materialSheetFab != null) {
                            MusicFragment.materialSheetFab.hideSheet();
                        }
                        //更新底部音乐播放列表的UI
                        if (tag == PLAY_LIST_BOTTOM_MUSIC && ContainerActivity.playlistDialog != null) {
                            ContainerActivity.playlistDialog.dismiss();
                        }
                    }
                })
                .show();
    }

    /**
     * 清空我喜欢的歌曲列表
     *
     * @param context
     */
    public static void clearMyLike(final Context context) {
        new SweetAlertDialog(context, SweetAlertDialog.WARNING_TYPE)
                .setTitleText("清空列表?")
                .setCancelText("点错了!")
                .setConfirmText("是的!")
                .showCancelButton(true)
                .setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sDialog) {
                        sDialog.cancel();
                    }
                })
                .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sweetAlertDialog) {
                        sweetAlertDialog.cancel();
                        MusicUtils.clearMyLike();
                        if (SongFragment.staticHandler != null) {
                            SongFragment.musics.clear();
                            SongFragment.staticHandler.sendEmptyMessage(SongFragment.UPDATE_UI);
                        }
                    }
                })
                .show();
    }

    /**
     * 清空最近播放列表
     *
     * @param context
     */
    public static void clearRecentPlay(final Context context) {
        new SweetAlertDialog(context, SweetAlertDialog.WARNING_TYPE)
                .setTitleText("清空列表?")
                .setCancelText("点错了!")
                .setConfirmText("是的!")
                .showCancelButton(true)
                .setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sDialog) {
                        sDialog.cancel();
                    }
                })
                .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sweetAlertDialog) {
                        sweetAlertDialog.cancel();
                        MusicUtils.clearRecentPlay();
                        if (SongFragment.staticHandler != null) {
                            SongFragment.musics.clear();
                            SongFragment.staticHandler.sendEmptyMessage(SongFragment.UPDATE_UI);
                        }
                    }
                })
                .show();
    }

    /**
     * 显示选择播放顺序的对话框
     *
     * @param context
     */
    public static void showOrder(final Context context) {
        //对话框进入动画
        BaseAnimatorSet bas_in = new BaseAnimatorSet() {
            @Override
            public void setAnimation(View view) {
                AnimationSet mModalInAnim = (AnimationSet) OptAnimationLoader.loadAnimation(context, R.anim.modal_in);
                view.setAnimation(mModalInAnim);
            }
        };
        //对话框退出动画
        BaseAnimatorSet bas_out = new BaseAnimatorSet() {
            @Override
            public void setAnimation(View view) {
                Animation mModalOutAnim = (AnimationSet) OptAnimationLoader.loadAnimation(context, R.anim.modal_out);
                view.setAnimation(mModalOutAnim);
            }
        };

        final String[] stringItems = new String[]{"顺序播放", "循环播放", "单曲循环", "随机播放"};
        final NormalListDialog dialog = new NormalListDialog(context, stringItems);
        dialog.title("请选择")
                .titleTextSize_SP(18)
                .titleBgColor(context.getResources().getColor(R.color.custom_color))
                .itemPressColor(context.getResources().getColor(R.color.custom_color))//
                .itemTextColor(context.getResources().getColor(R.color.black))//
                .itemTextSize(14)
                .cornerRadius(5)
                .widthScale(0.8f)
                .showAnim(bas_in)
                .dismissAnim(bas_out)
                .show();
        dialog.setOnOperItemClickL(new OnOperItemClickL() {
            @Override
            public void onOperItemClick(AdapterView<?> parent, View view, int position, long id) {
                dialog.dismiss();

                if (position == MusicUtils.ORDER_SEQUENTIAL) {
                    MusicUtils.setPlayOrder(context, MusicUtils.ORDER_SEQUENTIAL);
                    Toast.makeText(context, "顺序播放", 0).show();
                }

                if (position == MusicUtils.ORDER_LOOP) {
                    MusicUtils.setPlayOrder(context, MusicUtils.ORDER_LOOP);
                    Toast.makeText(context, "循环播放", 0).show();
                }

                if (position == MusicUtils.ORDER_CYCLE) {
                    MusicUtils.setPlayOrder(context, MusicUtils.ORDER_CYCLE);
                    Toast.makeText(context, "单曲循环", 0).show();
                }

                if (position == MusicUtils.ORDER_RANDOM) {
                    MusicUtils.setPlayOrder(context, MusicUtils.ORDER_RANDOM);
                    Toast.makeText(context, "随机播放", 0).show();
                }

            }
        });

    }

    /**
     * 提醒是否去扫描音乐
     *
     * @param context
     */
    public static void tipToScanMusic(final Context context) {
        new SweetAlertDialog(context, SweetAlertDialog.NORMAL_TYPE)
                .setTitleText("扫描歌曲")
                .setContentText("歌曲列表空空的，是否去扫描歌曲？")
                .setCancelText("下次再说")
                .setConfirmText("好的")
                .showCancelButton(true)
                .setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sDialog) {
                        sDialog.cancel();
                    }
                })
                .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sweetAlertDialog) {
                        sweetAlertDialog.cancel();
                        Intent intent = new Intent(context, ScanMusicActivity.class);
                        context.startActivity(intent);
                    }
                })
                .show();
    }
}
