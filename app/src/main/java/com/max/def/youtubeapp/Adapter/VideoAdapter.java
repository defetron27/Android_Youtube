package com.max.def.youtubeapp.Adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.max.def.youtubeapp.Model.VideoModel;
import com.max.def.youtubeapp.R;
import com.squareup.picasso.Picasso;

import java.util.List;

public class VideoAdapter extends RecyclerView.Adapter<VideoAdapter.VideoViewHolder>
{
    private List<VideoModel> videoModelList;
    private Context context;

    public VideoAdapter(List<VideoModel> videoModelList, Context context) {
        this.videoModelList = videoModelList;
        this.context = context;
    }

    @NonNull
    @Override
    public VideoViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i)
    {
        View view = LayoutInflater.from(context).inflate(R.layout.video_items,viewGroup,false);

        return new VideoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VideoViewHolder holder, int position)
    {
        VideoModel model = videoModelList.get(position);

        holder.videoTitle.setText("Video of some movies 1");
        holder.setVideoThumb(context,model.getThumb_url());
    }

    @Override
    public int getItemCount()
    {
        return videoModelList.size();
    }

    public class VideoViewHolder extends RecyclerView.ViewHolder
    {
        View view;
        AppCompatTextView videoTitle;

        public VideoViewHolder(@NonNull View itemView)
        {
            super(itemView);

            view = itemView;

            videoTitle = itemView.findViewById(R.id.video_title);
        }

        private void setVideoThumb(Context context,String url)
        {
            AppCompatImageView videoThumb = view.findViewById(R.id.video_thumb);

            Picasso.with(context).load(url).into(videoThumb);
        }
    }
}
