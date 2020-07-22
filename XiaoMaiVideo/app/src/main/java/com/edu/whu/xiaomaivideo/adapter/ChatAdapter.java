/**
 * Author: 张俊杰
 * Create Time: 2020/7/20
 * Update Time: 2020/7/22
 */

package com.edu.whu.xiaomaivideo.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.request.RequestOptions;
import com.edu.whu.xiaomaivideo.R;
import com.edu.whu.xiaomaivideo.model.Message;
import com.edu.whu.xiaomaivideo.model.MessageVO;
import com.edu.whu.xiaomaivideo.util.Constant;

import org.litepal.crud.callback.UpdateOrDeleteCallback;

import java.util.ArrayList;
import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private Context mContext;
    private List<Message> msgs;

    public ChatAdapter(Context context)
    {
        this.mContext=context;
        msgs=new ArrayList<>();
    }

    public void setMsgs(List<Message> msgs) {
        this.msgs = msgs;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType%2==0) {
            Log.e("viewType", String.valueOf(viewType));
            return new ChatAdapterViewHolder(LayoutInflater.from(mContext).inflate(R.layout.my_msg, parent, false));

        } else {
            return new ChatAdapterViewHolder(LayoutInflater.from(mContext).inflate(R.layout.others_msg, parent, false));
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ((ChatAdapterViewHolder)holder).movieImg.setImageResource(R.drawable.ic_launcher_background);
        //((ChatAdapterViewHolder)holder).msgText.setText(msgs.get(position).getText());
        ((ChatAdapterViewHolder)holder).msgText.setText("sssssssssssssssssssssssssss");
        //((ChatAdapterViewHolder)holder).dateText.setText(msgs.get(position).getTime().toString());
        ((ChatAdapterViewHolder)holder).dateText.setText("07-10 16:37");
//        //自己发的
//        if (msgs.get(position).getSenderId()==Constant.CurrentUser.getUserId()){
//            Glide.with(mContext)
//                    .load(Constant.CurrentUser.getAvatar())
//                    .apply(RequestOptions.bitmapTransform(new CircleCrop()))
//                    .diskCacheStrategy(DiskCacheStrategy.NONE)
//                    .into(((ChatAdapterViewHolder) holder).avatar);
//
//        }else {
//            //别人发的
//            Glide.with(mContext)
//                    .load(Constant.CurrentUser.getAvatar())
//                    .apply(RequestOptions.bitmapTransform(new CircleCrop()))
//                    .diskCacheStrategy(DiskCacheStrategy.NONE)
//                    .into(((ChatAdapterViewHolder) holder).avatar);
//        }

//        if (msgs.get(position).getMsgType().equals("msg")){
//            ((ChatAdapterViewHolder)holder).movieCard.setVisibility(View.GONE);
//        }else if (msgs.get(position).getMsgType().equals("at")){
//            ((ChatAdapterViewHolder)holder).movieImg.setImageResource(R.drawable.ic_launcher_background);
//        }
        Glide.with(mContext)
                .load(Constant.CurrentUser.getAvatar())
                .apply(RequestOptions.bitmapTransform(new CircleCrop()))
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .into(((ChatAdapterViewHolder) holder).avatar);
    }

    @Override
    public int getItemCount() {
        //return msgs.size();
        return 10;
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    class ChatAdapterViewHolder extends RecyclerView.ViewHolder
    {

        private TextView msgText;
        private CardView movieCard;
        private ImageView avatar;
        private ImageView movieImg;
        private TextView movieDescription;
        private TextView dateText;

        public ChatAdapterViewHolder(@NonNull View itemView) {
            super(itemView);
            msgText = itemView.findViewById(R.id.msg_text);
            movieCard =itemView.findViewById(R.id.movie_card);
            avatar =itemView.findViewById(R.id.avatar);
            movieImg = itemView.findViewById(R.id.movie_img);
            movieDescription = itemView.findViewById(R.id.movie_description);
            dateText=itemView.findViewById(R.id.date_text);
        }
    }
}
