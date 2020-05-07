package com.example.catdog;

        import android.content.Context;
        import android.content.Intent;
        import android.net.Uri;
        import android.view.LayoutInflater;
        import android.view.View;
        import android.view.ViewGroup;
        import android.widget.ImageView;
        import android.widget.Toast;

        import androidx.annotation.NonNull;
        import androidx.recyclerview.widget.RecyclerView;

        import com.squareup.picasso.Picasso;


        import java.net.URI;
        import java.util.List;

public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ImageViewHolder> {

    Context mContext;
    List<Post> mPostList;

    public ImageAdapter(Context context, List<Post> post){
        this.mContext = context;
        this.mPostList =post;

    }
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View v = LayoutInflater.from(mContext).inflate(R.layout.posts_card_view, parent, false);
        return new ImageViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, final int position) {
        Post currentPost = mPostList.get(position);
        Picasso.get()
                .load(currentPost.getUri())
                .into(holder.imageView);

        holder.imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext,PostActivity.class);

                // passing data to the book activity
                intent.putExtra("Time",mPostList.get(position).getTimestamp());
                intent.putExtra("Picture",mPostList.get(position).getUri());
                intent.putExtra("Caption",mPostList.get(position).getCaption());
                intent.putExtra("Hashtags",mPostList.get(position).getHashtags());
                intent.putExtra("Uid",mPostList.get(position).getUid());
                intent.putExtra("Username",mPostList.get(position).getUsername());


                // start the activity
                mContext.startActivity(intent);
            }
        });

    }

    @Override
    public int getItemCount() {
        return mPostList.size();
    }

    public static class ImageViewHolder extends RecyclerView.ViewHolder{

        public ImageView imageView;

        public ImageViewHolder(@NonNull View itemView) {
            super(itemView);

            imageView = itemView.findViewById(R.id.imgID);

        }
    }
}
