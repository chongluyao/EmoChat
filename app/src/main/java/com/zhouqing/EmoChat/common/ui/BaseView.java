package com.zhouqing.EmoChat.common.ui;


public interface BaseView<T extends BasePresenter> {

    public void setPresenter(T presenter);
}
