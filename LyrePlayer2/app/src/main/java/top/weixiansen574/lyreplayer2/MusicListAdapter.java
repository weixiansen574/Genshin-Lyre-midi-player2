package top.weixiansen574.lyreplayer2;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.provider.Settings;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.util.List;

import top.weixiansen574.lyreplayer2.database.MusicBean;
import top.weixiansen574.lyreplayer2.database.MusicDao;
import top.weixiansen574.lyreplayer2.enums.MusicInstrumentType;

public class MusicListAdapter extends RecyclerView.Adapter<MusicListAdapter.MyViewHolder> {
    Activity context;
    MusicDao dao;
    List<MusicBean> musics;

    public MusicListAdapter(Activity context) {
        this.context = context;
        this.dao = new MusicDao(context);
        this.musics = dao.getAllMusics();
    }


    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = View.inflate(context, R.layout.list_item, null);
        return new MyViewHolder(itemView);
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        MusicBean music = musics.get(position);
        if (music.getInstrumentType() == MusicInstrumentType.lyre) {
            holder.img_icon.setImageDrawable(context.getDrawable(R.drawable.lyre_round_icon));
        } else if (music.getInstrumentType() == MusicInstrumentType.oldLyre) {
            holder.img_icon.setImageDrawable(context.getDrawable(R.drawable.old_lyre_round_icon));
        }
        holder.txv_name.setText(music.name);

        holder.itemView.setOnClickListener(v -> {
            AlertDialog dialogStart = new AlertDialog
                    .Builder(context)
                    .setTitle(R.string.querengequ)
                    .setMessage(music.name)
                    .setNeutralButton(R.string.调整配置, (dialog, which) -> {
                        Intent intent = new Intent(context, AdjustAndStartActivity.class);
                        intent.putExtra(AdjustAndStartActivity.EXTRA_NAME,music.name);
                        intent.putExtra(AdjustAndStartActivity.EXTRA_INSTRUMENT,music.instrument);
                        intent.putExtra(AdjustAndStartActivity.EXTRA_TRANSPOSITION,music.transposition);
                        intent.putExtra(AdjustAndStartActivity.EXTRA_INVALID_KEY_SETTING,music.invalid_key_setting);
                        intent.putExtra(AdjustAndStartActivity.EXTRA_MAPPING_1,music.mapping1);
                        intent.putExtra(AdjustAndStartActivity.EXTRA_MAPPING_2,music.mapping2);
                        intent.putExtra(AdjustAndStartActivity.EXTRA_MAPPING_3,music.mapping3);
                        intent.putExtra(AdjustAndStartActivity.EXTRA_MAPPING_4,music.mapping4);
                        intent.putExtra(AdjustAndStartActivity.EXTRA_MAPPING_5,music.mapping5);
                        intent.putExtra(AdjustAndStartActivity.EXTRA_MAPPING_6,music.mapping6);
                        intent.putExtra(AdjustAndStartActivity.EXTRA_MAPPING_7,music.mapping7);
                        intent.setData(Uri.fromFile(new File(music.path)));
                        context.startActivity(intent);
                    })
                    .setNegativeButton(R.string.cancel, (dialog, which) -> {
                    })
                    .setPositiveButton(R.string.ok, null).show();

            dialogStart.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v1 -> {
                //判断是否有悬浮窗权限
                if (!Settings.canDrawOverlays(context)) {
                    new AlertDialog.Builder(context)
                            .setTitle(R.string.floating_window_permission_is_required)
                            .setPositiveButton(R.string.ok, (dialogInterface, i) -> {
                                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
                                intent.setData(Uri.parse("package:" + context.getPackageName()));
                                context.startActivityForResult(intent, 100);
                            }).show();
                } else {
                    //已经有权限，可以直接显示悬浮窗
                    //判断是否有无障碍权限
                    Uri midiUri = Uri.fromFile(new File(music.path));
                    Intent intent = new Intent(context, FloatingWindowService.class);
                    intent.putExtra(FloatingWindowService.EXTRA_NAME, music.name);
                    intent.putExtra(FloatingWindowService.EXTRA_INSTRUMENT, music.instrument);
                    intent.putExtra(FloatingWindowService.EXTRA_GROUP_MAPPING, music.getGroupMapping());
                    intent.putExtra(FloatingWindowService.EXTRA_TRANSPOSITION, music.transposition);
                    intent.putExtra(FloatingWindowService.EXTRA_INVALID_KEY_SETTING, music.invalid_key_setting);
                    intent.setData(midiUri);
                    ShizukuUtil.bindShizukuAndStartService(context,intent,true);
                    dialogStart.dismiss();
                }
            });
        });

        holder.btn_delete.setOnClickListener((View.OnClickListener) v -> {
            new AlertDialog.Builder(context)
                    .setTitle(R.string.qdyscm)
                    .setMessage(music.name)
                    .setNegativeButton(R.string.cancel, null)
                    .setPositiveButton(R.string.ok, (dialogInterface, i) -> {
                        File file = new File(music.path);
                        if (file.exists()) {
                            file.delete();
                        }
                        dao.deleteMusic(music.name);
                        musics.remove(holder.getAdapterPosition());
                        notifyItemRemoved(holder.getAdapterPosition());
                    }).show();
        });
    }

    @Override
    public int getItemCount() {
        return musics.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        View itemView;
        ImageView img_icon;
        TextView txv_name;
        Button btn_delete;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            this.itemView = itemView;
            img_icon = itemView.findViewById(R.id.img_music_icon);
            txv_name = itemView.findViewById(R.id.music_name);
            btn_delete = itemView.findViewById(R.id.delate);
        }
    }
}
