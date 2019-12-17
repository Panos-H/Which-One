package com.panos.oddandroid.activity;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.panos.oddandroid.GameConstants;
import com.panos.oddandroid.R;
import com.panos.oddandroid.customview.LayoutBackground;
import com.panos.oddandroid.model.CompleteChallenge;

import java.util.List;

public class ChallengeListAdapter extends RecyclerView.Adapter<ChallengeListAdapter.ChallengeViewHolder> {

    class ChallengeViewHolder extends RecyclerView.ViewHolder {

        private final LayoutBackground background;
        private final TextView userScoreText;
        private final TextView enemyScoreText;
        //private final ImageView gameTypeIcon;
        private final ImageView userCategoryIcon;
        private final ImageView enemyCategoryIcon;
        private final ImageView enemyImage;

        private final View nameInitialBackground;
        private final TextView nameInitialText;

        public ChallengeViewHolder(@NonNull View itemView) {
            super(itemView);
            background = itemView.findViewById(R.id.layoutBackground);
            userScoreText = itemView.findViewById(R.id.userScoreText);
            enemyScoreText = itemView.findViewById(R.id.enemyScoreText);
            //gameTypeIcon = itemView.findViewById(R.id.gameTypeIcon);
            userCategoryIcon = itemView.findViewById(R.id.userCategoryIcon);
            enemyCategoryIcon = itemView.findViewById(R.id.enemyCategoryIcon);
            enemyImage = itemView.findViewById(R.id.enemyImage);

            nameInitialBackground = itemView.findViewById(R.id.nameInitialBackground);
            nameInitialText = itemView.findViewById(R.id.nameInitialText);
        }
    }

    private final LayoutInflater inflater;
    private List<CompleteChallenge> challenges;

    public ChallengeListAdapter(Context context) {
        inflater = LayoutInflater.from(context);
    }

    @NonNull
    @Override
    public ChallengeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = inflater.inflate(R.layout.recyclerview_challenge, parent,false);
        return new ChallengeViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ChallengeViewHolder holder, int position) {
        if (challenges != null) {
            CompleteChallenge current = challenges.get(position);
            holder.background.setBlurColor(current.getResultColor());
            holder.userScoreText.setText(String.valueOf(current.getUserScore()));
            holder.enemyScoreText.setText(String.valueOf(current.getEnemyScore()));
            //holder.gameTypeIcon.setImageResource(GameConstants.MODE_TYPE_ICONS[current.getModeType()]);
            holder.userCategoryIcon.setImageResource(GameConstants.CATEGORY_TYPE_ICONS[current.getUserCategory()]);
            holder.enemyCategoryIcon.setImageResource(GameConstants.CATEGORY_TYPE_ICONS[current.getEnemyCategory()]);
        } else {
            holder.background.setBlurColor(Color.GRAY);
        }
    }

    public void setChallenges(List<CompleteChallenge> challenges) {
        this.challenges = challenges;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        if (challenges != null) return challenges.size();
        else return 0;
    }
}
