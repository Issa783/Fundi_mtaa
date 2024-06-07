package com.example.fundimtaa;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class UsersAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_SECTION_HEADER = 0;
    private static final int TYPE_USER_ITEM = 1;

    private List<User> clientsList;
    private List<User> workersList;
    private OnUserItemClickListener onUserItemClickListener;

    public UsersAdapter(List<User> clientsList, List<User> workersList, OnUserItemClickListener onUserItemClickListener) {
        this.clientsList = clientsList;
        this.workersList = workersList;
        this.onUserItemClickListener = onUserItemClickListener;
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0 || position == clientsList.size() + 1) {
            return TYPE_SECTION_HEADER;
        } else {
            return TYPE_USER_ITEM;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_SECTION_HEADER) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_section_header, parent, false);
            return new SectionHeaderViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user, parent, false);
            return new UserViewHolder(view, onUserItemClickListener);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof SectionHeaderViewHolder) {
            SectionHeaderViewHolder sectionHeaderViewHolder = (SectionHeaderViewHolder) holder;
            if (position == 0) {
                sectionHeaderViewHolder.textViewSectionHeader.setText("Clients");
            } else {
                sectionHeaderViewHolder.textViewSectionHeader.setText("Workers");
            }
        } else {
            UserViewHolder userViewHolder = (UserViewHolder) holder;
            User user;
            if (position <= clientsList.size()) {
                user = clientsList.get(position - 1);
            } else {
                user = workersList.get(position - clientsList.size() - 2);
            }
            userViewHolder.textViewName.setText("Name:" + user.getName());
            userViewHolder.textViewEmail.setText("Email:" + user.getEmail());
            userViewHolder.textViewRole.setText("Role: " + user.getRole());
            userViewHolder.itemView.setTag(user); // Set user as tag for the item view
        }
    }


    @Override
    public int getItemCount() {
        return clientsList.size() + workersList.size() + 2; // +2 for the section headers
    }

    public static class UserViewHolder extends RecyclerView.ViewHolder {
        TextView textViewName, textViewEmail, textViewRole;

        public UserViewHolder(@NonNull View itemView, OnUserItemClickListener onUserItemClickListener) {
            super(itemView);
            textViewName = itemView.findViewById(R.id.textViewName);
            textViewEmail = itemView.findViewById(R.id.textViewEmail);
            textViewRole = itemView.findViewById(R.id.textViewRole);

            itemView.setOnClickListener(v -> {
                if (onUserItemClickListener != null) {
                    onUserItemClickListener.onUserItemClick((User) itemView.getTag());
                }
            });
        }
    }

    public static class SectionHeaderViewHolder extends RecyclerView.ViewHolder {
        TextView textViewSectionHeader;

        public SectionHeaderViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewSectionHeader = itemView.findViewById(R.id.textViewSectionHeader);
        }
    }

    public interface OnUserItemClickListener {
        void onUserItemClick(User user);
    }
}
