/**
 * Author: 叶俊豪
 * Create Time: 2020/7/11
 * Update Time: 2020/7/11
 */

package com.edu.whu.xiaomaivideo.restcallback;

import com.edu.whu.xiaomaivideo.model.User;

public interface UserRestCallback {
    public void onSuccess(int resultCode, User user);
}
