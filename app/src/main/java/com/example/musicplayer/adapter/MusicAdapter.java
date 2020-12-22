package com.example.musicplayer.adapter;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.musicplayer.R;
import com.example.musicplayer.model.Music;

import java.util.ArrayList;
import java.util.List;

/**
 * 音乐播放适配器
 */
public class MusicAdapter extends RecyclerView.Adapter<MusicAdapter.innerHolder> {

    private List<Music> musicList = new ArrayList<>();
    private static final String TAG = "MusicAdapter";
    private onListenItemClickListen onListenItemClickListen = null;

    @NonNull
    @Override
    public MusicAdapter.innerHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.music_item, parent, false);
        return new innerHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MusicAdapter.innerHolder holder, final int position) {
        final Music music = musicList.get(position);
        holder.setData(music);
        //设置每一个item的点击事件
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "click itemView ——> "+music);
                onListenItemClickListen.onClickItem(music, position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return musicList.size();
    }

    /**
     * 从外部传数据进来
     * @param music
     */
    public void setData(List<Music> music){
        musicList.clear();
        musicList.addAll(music);
        notifyDataSetChanged();
    }

    /**
     * 加载布局
     */
    public class innerHolder extends RecyclerView.ViewHolder {

        public ImageView cover;

        public TextView nameText;

        public TextView authorText;

        public innerHolder(@NonNull View itemView) {
            super(itemView);
            cover = itemView.findViewById(R.id.music_cover);
            nameText = itemView.findViewById(R.id.music_name);
            authorText = itemView.findViewById(R.id.music_author);
        }

        public void setData(Music music) {
            nameText.setText(music.getName());
            authorText.setText(music.getAuthor());
            cover.setImageResource(music.getImageId());
        }
    }

    /**
     *item的点击事件的接口
     */
    public interface onListenItemClickListen{
        void onClickItem(Music music, int position);  //点击事件
    }

    public void setOnListenItemClickListen(onListenItemClickListen listener){
        this.onListenItemClickListen = listener;
    }
}
