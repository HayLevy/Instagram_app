package com.example.instagram_app.Fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.instagram_app.Adapter.PostAdapter;
import com.example.instagram_app.Controller.ViewModel;
import com.example.instagram_app.Controller.Server;
import com.example.instagram_app.Model.Post;
import com.example.instagram_app.R;

import java.util.ArrayList;
import java.util.List;


public class HomeFragment extends Fragment {

    private RecyclerView recyclerView;
    private PostAdapter postAdapter;
    private List<Post> postLists;
    private List<String> followingList;

    ProgressBar progressBar;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.fragment_home, container, false);

        recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);
        postLists = new ArrayList<>();
        postAdapter = new PostAdapter(getContext(),postLists);
        recyclerView.setAdapter(postAdapter);

        progressBar = view.findViewById(R.id.progress_circular);

        checkFollowing();
        if(!Server.Database.netIsConnect()) {
            Toast.makeText(getContext(), "Offline network", Toast.LENGTH_SHORT).show();
            readPosts();
        }
        checkUpdate();

        return view;
    }

    private void checkFollowing(){
        followingList = new ArrayList<>();
        Server.Database.getFollow(Server.Auth.getUid(),false,strings -> {
            followingList.clear();
            followingList.addAll(strings);
            readPosts();
        },e -> {});
    }

    private void readPosts(){

        ViewModel.getInstance().getPosts().observe(this, posts -> {

            postLists.clear();
            for(Post post:posts)
            {
                if(post.getPublisher().equals(Server.Auth.getUid())){
                    postLists.add(post);
                }
                for(String id:followingList){
                    if(post.getPublisher().equals(id)){
                        postLists.add(post);
                    }
                }
                postAdapter.notifyDataSetChanged();
                progressBar.setVisibility(View.GONE);
            }
        });


    }

    private void checkUpdate(){
        Server.Database.checkPostsNum(integer -> {
            if (integer!=postLists.size()){
                readPosts();
            }
        },e -> {});
    }
}
