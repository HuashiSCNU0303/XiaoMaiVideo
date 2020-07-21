/**
 * Author: 张俊杰、李季东
 * Create Time: 2020/7/16
 * Update Time: 2020/7/21
 */

package com.edu.whu.xiaomaivideo.ui.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import android.os.Bundle;
import com.edu.whu.xiaomaivideo.R;
import com.edu.whu.xiaomaivideo.model.User;
import com.edu.whu.xiaomaivideo.ui.fragment.FollowersFragment;
import com.edu.whu.xiaomaivideo.ui.fragment.FollowingFragment;
import com.edu.whu.xiaomaivideo.util.Constant;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import org.parceler.Parcels;

public class FollowActivity extends AppCompatActivity {
    ViewPager2 viewPage;
    TabLayout tabLayout;
    User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        user = Parcels.unwrap(getIntent().getParcelableExtra("user"));
        setContentView(R.layout.activity_follow);
        viewPage=findViewById(R.id.viewPage);
        tabLayout=findViewById(R.id.tabLayout2);
        viewPage.setAdapter(new FragmentStateAdapter(this) {
            private int count = 2;

            @NonNull
            @Override
            public Fragment createFragment(int position) {
                Fragment fragment;
                switch (position) {
                    default:
                    case 0:
                        fragment = new FollowingFragment(user);
                        break;
                    case 1:
                        fragment = new FollowersFragment(user);
                        break;
                }
                return fragment;
            }

            @Override
            public int getItemCount() {
                return count;
            }
        });
        new TabLayoutMediator(tabLayout, viewPage, true,new TabLayoutMediator.TabConfigurationStrategy() {
            @Override
            public void onConfigureTab(@NonNull TabLayout.Tab tab, int position) {
                // 这里需要根据position修改tab的样式和文字等
                switch (position) {
                    default:
                    case 0:
                        tab.setText("关注 "+ user.getFollowing().size());
                        break;
                    case 1:
                        tab.setText("粉丝 "+ user.getFollowers().size());
                        break;
                }
            }
        }).attach();
    }
}