package top.weixiansen574.lyreplayer2;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import top.weixiansen574.lyreplayer2.database.MusicBean;
import top.weixiansen574.lyreplayer2.database.MusicDao;
import top.weixiansen574.lyreplayer2.enums.MusicInstrumentType;


public class FLoatMusicListAdapter extends RecyclerView.Adapter<FLoatMusicListAdapter.MyViewHolder> {
    Context context;
    List<MusicBean> musics;
    OnItemClickListener listener;
    MusicDao musicDao;

    public FLoatMusicListAdapter(Context context) {
        this.context = context;
        this.musicDao = new MusicDao(context);
        this.musics = musicDao.getAllMusics();
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_float_window_music,parent,false);
        return new MyViewHolder(view);
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        MusicBean musicBean = musics.get(position);

        if (musicBean.getInstrumentType() == MusicInstrumentType.lyre){
            holder.imageView.setImageDrawable(context.getDrawable(R.drawable.lyre_round_icon));
        } else if (musicBean.getInstrumentType() == MusicInstrumentType.oldLyre){
            holder.imageView.setImageDrawable(context.getDrawable(R.drawable.old_lyre_round_icon));
        }

        holder.textView.setText(musicBean.name);
        holder.itemView.setOnClickListener(v -> listener.onItemClick(musicBean));
    }

    public void setOnItemClickListener(OnItemClickListener listener){
        this.listener = listener;
    }
    @Override
    public int getItemCount() {
        return musics.size();
    }

    public interface OnItemClickListener {
        void onItemClick(MusicBean musicBean);
    }


    public static class MyViewHolder extends RecyclerView.ViewHolder{
        View itemView;
        ImageView imageView;
        TextView textView;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            this.itemView = itemView;
            this.imageView = itemView.findViewById(R.id.img_music_icon);
            this.textView = itemView.findViewById(R.id.music_name);
        }
    }
}
