package com.example.catdog;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Picasso;


import org.w3c.dom.Text;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static android.app.Activity.RESULT_OK;

public class CommentsImageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_HEADER = 0;
    private static final int TYPE_ITEM = 1;

    Context context;
    List<Comment> commentList;
    Post post;


    FirebaseAuth firebaseAuth;

    public CommentsImageAdapter(Context context, List<Comment> commentList, Post post){
        this.context = context;
        this.commentList = commentList;
        this.post = post;
    }



    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_HEADER) {
            //inflate your layout and pass it to view holder
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.post_header_view, parent, false);
            HeaderHolder headerHolder = new HeaderHolder(v);
            return headerHolder;

        }
        else if (viewType == TYPE_ITEM) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.comments_card_view, parent, false);
            CommentHolder commentHolder = new CommentHolder(v);
            return commentHolder;
        }
        throw new RuntimeException("ERROR");
    }

    @Override
    public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof HeaderHolder) {

            Picasso.get().load(post.getUri()).into(((HeaderHolder) holder).postImageView);

            ((HeaderHolder) holder).timeLabel.setText(post.getTimestamp());
            ((HeaderHolder) holder).usernameLabel.setText(post.getUsername());
            ((HeaderHolder) holder).captionLabel.setText(post.getCaption());
            ((HeaderHolder) holder).hashtagsLabel.setText(post.getHashtags());
            firebaseAuth = FirebaseAuth.getInstance();

            if(firebaseAuth.getCurrentUser().getUid().equals(post.getUid())){
                ((HeaderHolder) holder).deleteButton.setVisibility(View.VISIBLE);
            }

        } else if (holder instanceof CommentHolder) {

            Comment currentComment = commentList.get(position-1);

            Picasso.get()
                    .load(currentComment.getProfilePhoto())
                    .into(((CommentHolder) holder).idComment);
            ((CommentHolder) holder).commentText.setText(currentComment.getCommentText());
            ((CommentHolder) holder).usernameComments.setText(currentComment.getUsername());


        }

    }

    @Override
    public int getItemCount() {
        return commentList.size()+1;
    }



    @Override
    public int getItemViewType(int position) {
        if (isPositionHeader(position))
            return TYPE_HEADER;

        return TYPE_ITEM;
    }


    private boolean isPositionHeader(int position) {
        return position == 0;
    }

    public static class HeaderHolder extends RecyclerView.ViewHolder{

        public ImageView postImageView;
        public TextView timeLabel;
        public TextView usernameLabel;
        public TextView captionLabel;
        public TextView hashtagsLabel;
        public Button deleteButton;

        public HeaderHolder(@NonNull View itemView) {
            super(itemView);

            postImageView = itemView.findViewById(R.id.postImageView);
            timeLabel = itemView.findViewById(R.id.timeLabel);
            usernameLabel = itemView.findViewById(R.id.usernameLabel);
            captionLabel = itemView.findViewById(R.id.captionLabel);
            hashtagsLabel = itemView.findViewById(R.id.hashtagsLabel);
            deleteButton = itemView.findViewById(R.id.deletePostButton);
        }
    }

    public static class CommentHolder extends RecyclerView.ViewHolder{
        public TextView commentText;
        public TextView usernameComments;
        public ImageView idComment;

        public CommentHolder(@NonNull View itemView) {
            super(itemView);
            usernameComments = itemView.findViewById(R.id.usernameComments);
            commentText = itemView.findViewById(R.id.commentsText);
            idComment = itemView.findViewById(R.id.idComments);

        }
    }


}